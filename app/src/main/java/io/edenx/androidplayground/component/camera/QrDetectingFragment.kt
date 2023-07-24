package io.edenx.androidplayground.component.camera

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import io.edenx.androidplayground.component.base.BaseFragment
import io.edenx.androidplayground.component.connectivity.BluetoothConnectingService
import io.edenx.androidplayground.databinding.FragmentQrDetectingBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.lang.IllegalStateException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class QrDetectingFragment : BaseFragment<FragmentQrDetectingBinding>(FragmentQrDetectingBinding::inflate) {

    private var previewUseCase: Preview? = null
    private var camera: Camera? = null
    private var analysisUseCase: ImageAnalysis? = null
    private lateinit var cameraExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var displayId = -1

    private val qrScanner by lazy {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_AZTEC)
                .enableAllPotentialBarcodes()
                .build())
    }

    private val multiplePermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            val isAllPermissionGrant = it.entries.all { permission ->
                permission.value
            }
            if (isAllPermissionGrant) {
                setUpCamera()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermissions()
    }

    @SuppressLint("MissingPermission")
    override fun setListener() {
        binding.btDetect.setOnClickListener {
            detectQrCode()
        }
    }

    private fun requestPermissions() {
        val result = arrayOf(Manifest.permission.CAMERA).all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
        if (result) {
            setUpCamera()
        } else multiplePermissionRequest.launch(arrayOf(Manifest.permission.CAMERA))
    }

    private fun setUpCamera() {
        cameraExecutor = Executors.newSingleThreadExecutor()

        ProcessCameraProvider.getInstance(requireContext()).apply {
            addListener(
                {
                    cameraProvider = this.get()
                    lensFacing = when {
                        cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) == true -> CameraSelector.LENS_FACING_BACK
                        else -> throw IllegalStateException("Back and front camera are unavailable")
                    }

                    // Init image capture operation and analyzer operations and connect them to preview view
                    bindCameraUseCase()
                },
                ContextCompat.getMainExecutor(requireContext())
            )

        }
    }

    private fun bindCameraUseCase() {
        val screenAspectRatio = getPreviewAspectRatio()
        val rotation = binding.pvCamera.display.rotation
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        previewUseCase = Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        analysisUseCase = ImageAnalysis.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        // Unbind before set new elements
        cameraProvider?.unbindAll()

        kotlin.runCatching {
            // Important, Connect preview view, camera controller and image operations together
            camera = cameraProvider?.bindToLifecycle(viewLifecycleOwner, cameraSelector, previewUseCase, analysisUseCase)
            previewUseCase?.setSurfaceProvider(binding.pvCamera.surfaceProvider)
        }.onFailure {
            it.printStackTrace()
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun detectQrCode() {
        analysisUseCase?.setAnalyzer(cameraExecutor) {
            it.image?.let { img ->
                val qr = InputImage.fromMediaImage(img, it.imageInfo.rotationDegrees)
                qrScanner.process(qr)
                    .addOnSuccessListener {
                        readQrCode(it.first())
                    }
                    .addOnCompleteListener {
                        it.exception?.printStackTrace()
                    }
            }
        }
    }

    private fun readQrCode(item: Barcode) {
        if (item.valueType == Barcode.TYPE_TEXT) {
            Log.d("xxxx", "Text from QR: ${item.rawValue}")
            Gson().fromJson("", Map::class.java)?.let {
                if (it.get("type") == "bluetooth") {

                }
            }
        }
    }

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
}

