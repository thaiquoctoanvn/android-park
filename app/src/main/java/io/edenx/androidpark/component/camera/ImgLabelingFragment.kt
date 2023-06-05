package io.edenx.androidpark.component.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import io.edenx.androidpark.R
import io.edenx.androidpark.component.base.BaseFragment
import io.edenx.androidpark.databinding.FragmentImgLabelingBinding
import io.edenx.androidpark.databinding.ViewCameraUiBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.lang.IllegalStateException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class ImgLabelingFragment : BaseFragment<FragmentImgLabelingBinding>(FragmentImgLabelingBinding::inflate) {
    private val permissionsRequired = arrayOf(Manifest.permission.CAMERA, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE)
    private val imageTypeNeed = arrayOf("JPG", "PNG")

    private val multiplePermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            val isAllPermissionGrant = it.entries.all { permission ->
                permission.value
            }
            if (isAllPermissionGrant) {
                updateCameraUi()
            }
        }

    private val displayManager by lazy { requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager }

    // Update configuration when the screen orientation changes
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) {
            view?.let {
                if (displayId == this@ImgLabelingFragment.displayId) {
                    imageCapture?.targetRotation = it.display.rotation
                    imageAnalysis?.targetRotation = it.display.rotation
                }
            }
        }
    }

    private var cameraUiContainerBinding: ViewCameraUiBinding? = null
    private var preview: Preview? = null
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private lateinit var outputDir: File
    private lateinit var cameraExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var displayId = -1


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayManager.registerDisplayListener(displayListener, null)
        requestPermissions()
    }

    override fun onDestroyView() {
        cameraUiContainerBinding = null
        super.onDestroyView()
        // Shutdown camera
        cameraExecutor.shutdown()
        displayManager.unregisterDisplayListener(displayListener)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        bindCameraUseCase()
        updateCameraSwitchButton()
    }

    private fun requestPermissions() {
        val result = permissionsRequired.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
        if (result) {
            updateCameraUi()
        } else multiplePermissionRequest.launch(permissionsRequired)
    }

    private fun setUpCamera() {
        cameraExecutor = Executors.newSingleThreadExecutor()

        ProcessCameraProvider.getInstance(requireContext()).apply {
            addListener(
                {
                    cameraProvider = this.get()
                    lensFacing = when {
                        hasBackCamera() -> CameraSelector.LENS_FACING_BACK
                        hasFrontCamera() -> CameraSelector.LENS_FACING_FRONT
                        else -> throw IllegalStateException("Back and front camera are unavailable")
                    }

                    // Update enabled state of camera switching button
                    updateCameraSwitchButton()
                    // Init image capture operation and analyzer operations and connect them to preview view
                    bindCameraUseCase()
                },
                ContextCompat.getMainExecutor(requireContext())
            )

        }

        binding.ibClosePreview.setOnClickListener {
            binding.sivPreview.setImageDrawable(null)
            binding.groupPreview.visibility = View.GONE
        }
    }

    private fun bindCameraUseCase() {
        val screenAspectRatio = getPreviewAspectRatio()
        val rotation = binding.pvCamera.display.rotation
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        preview = Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            // Set ratio but resolution. Let CX optimize resolution best fits the specific use case. The ratio should match the one of preview config
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        imageAnalysis = ImageAnalysis.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        // Unbind before set new elements
        cameraProvider?.unbindAll()

        kotlin.runCatching {
            // Important, Connect preview view, camera controller and image operations together
            camera = cameraProvider?.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageCapture, imageAnalysis)
            preview?.setSurfaceProvider(binding.pvCamera.surfaceProvider)
            observeCameraState(camera?.cameraInfo)
        }.onFailure {
            it.printStackTrace()
        }
    }

    private fun observeCameraState(cameraInfo: CameraInfo?) {
        cameraInfo?.cameraState?.observe(viewLifecycleOwner) {
            when (it.type) {
                CameraState.Type.PENDING_OPEN -> {} // Other apps are using camera, we might request user to close them
                CameraState.Type.OPENING -> {}
                CameraState.Type.OPEN -> {}
                CameraState.Type.CLOSING -> {}
                CameraState.Type.CLOSED -> {}
                else -> {
                    val a = 0
                }
            }

            it.error?.let { error ->
                when (error.code) {
                    CameraState.ERROR_STREAM_CONFIG -> {} // Ur config get wrong
                    CameraState.ERROR_CAMERA_IN_USE -> {} // Other apps are taking the camera
                    CameraState.ERROR_OTHER_RECOVERABLE_ERROR -> {}
                    CameraState.ERROR_CAMERA_DISABLED -> {}
                    CameraState.ERROR_MAX_CAMERAS_IN_USE -> {}
                    CameraState.ERROR_CAMERA_FATAL_ERROR -> {} // Request user to reboot the device to fix error
                    CameraState.ERROR_DO_NOT_DISTURB_MODE_ENABLED -> {} // Request user to turn off do not disturb mode
                    else -> {
                        val a = 0
                    }
                }
            }
        }
    }

    private fun updateCameraUi() {
        binding.pvCamera.post {
            displayId = binding.pvCamera.display.displayId

            cameraUiContainerBinding?.root?.let {
                binding.root.removeView(it)
            }

            cameraUiContainerBinding = ViewCameraUiBinding.inflate(
                LayoutInflater.from(requireContext()),
                binding.root,
                true)

            lifecycleScope.launch(Dispatchers.IO) {
                outputDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: File(requireContext().filesDir, "Core Components")
                outputDir.listFiles { file ->
                    imageTypeNeed.contains(file.extension.uppercase(Locale.ROOT))
                }?.maxOrNull()?.let {
                    // Set the latest image to thumbnail
                    setGalleryThumbnail(Uri.fromFile(it))
                }
            }

            cameraUiContainerBinding?.cameraCaptureButton?.setOnClickListener {
                imageCapture?.let {
                    val photoFile = File(
                        outputDir,
                        SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US).format(System.currentTimeMillis()) + ".JPG")

                    // Mirror image captured by front cam if need
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                    it.takePicture(
                        outputOptions,
                        cameraExecutor,
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                val savedUri = outputFileResults.savedUri ?: Uri.fromFile(photoFile)
                                setGalleryThumbnail(savedUri)
                                // set latest image to thumbnail
                                lifecycleScope.launch(Dispatchers.Main) {
                                    binding.pvCamera.visibility = View.GONE
                                    binding.groupPreview.visibility = View.VISIBLE
                                    Glide.with(requireContext())
                                        .load(outputFileResults.savedUri)
                                        .into(binding.sivPreview)
                                }

                                processImageLabelingFromCustomCamera()
                            }

                            override fun onError(exception: ImageCaptureException) {
                                exception.printStackTrace()
                            }
                        }
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        binding.root.postDelayed({
                            binding.root.foreground = ColorDrawable(Color.WHITE)
                            binding.root.postDelayed({binding.root.foreground = null}, 50L)
                        }, 50L)
                    }
                }
            }

            cameraUiContainerBinding?.cameraSwitchButton?.setOnClickListener {
                lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) CameraSelector.LENS_FACING_BACK
                else CameraSelector.LENS_FACING_FRONT

                bindCameraUseCase()
            }
            //binding.pvCamera.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            setUpCamera()
        }
    }

    private fun setGalleryThumbnail(uri: Uri) {
        cameraUiContainerBinding?.photoViewButton?.apply {
            post {
                setPadding(resources.getDimension(R.dimen.dp_4).toInt())
                Glide.with(this)
                    .load(uri)
                    .apply(RequestOptions.circleCropTransform())
                    .into(this)
            }
        }
    }

    private fun updateCameraSwitchButton() {
        cameraUiContainerBinding?.cameraSwitchButton?.isEnabled = hasBackCamera() && hasFrontCamera()
    }

    private fun hasFrontCamera() = cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false

    private fun hasBackCamera() = cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false

    private fun getPreviewAspectRatio(): Int {
        var myWidth = 0
        var myHeight = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            with(requireActivity().windowManager.currentWindowMetrics.bounds) {
                myWidth = width()
                myHeight = height()
            }
        } else {
            with (resources.displayMetrics) {
                myWidth = widthPixels
                myHeight = heightPixels
            }
        }

        val previewRatio = max(myWidth, myHeight).toDouble() / min(myWidth, myHeight)
        if (abs(previewRatio - (4.0 / 3.0)) <= abs(previewRatio - (16.0 / 9.0))) return AspectRatio.RATIO_4_3
        return AspectRatio.RATIO_16_9
    }

    // Image labeling
    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageLabelingFromCustomCamera() {
        imageAnalysis?.let { mImageAnalysis ->
            mImageAnalysis.setAnalyzer(cameraExecutor) { mImageProxy ->
                mImageProxy.image?.let { image ->
                    val input = InputImage.fromMediaImage(image, mImageProxy.imageInfo.rotationDegrees)
                    ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
                        .process(input)
                        .addOnCompleteListener { labels ->
                            Toast.makeText(
                                requireContext(),
                                "Labels detected: ${labels.result.joinToString(separator = ", ") { it.text }}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        .addOnFailureListener { e -> e.printStackTrace() }
                }
            }
        }
    }
}