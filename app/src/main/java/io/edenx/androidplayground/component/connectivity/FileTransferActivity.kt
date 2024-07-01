package io.edenx.androidplayground.component.connectivity

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import io.edenx.androidplayground.component.base.BaseActivity
import io.edenx.androidplayground.databinding.ActivityFileTransferBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FileTransferActivity : BaseActivity<ActivityFileTransferBinding>(ActivityFileTransferBinding::inflate) {

    private var bluetoothServerThread: BluetoothConnectingService.AcceptThread? = null
    private val uniqueUuid = "e4ffd149-ae09-4c9a-bb99-87e4ea267c39"
    private val permissionRequired = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH
        )
    }

    private val enableBluetoothLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            checkBluetoothIsAvailable {

            }
        }
    }
    private val multiplePermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            val isAllPermissionGrant = it.entries.all { permission ->
                permission.value
            }
            if (isAllPermissionGrant) {
                checkBluetoothIsAvailable() {
                    startBluetoothListener()
                }
            }
        }
    private val bluetoothAdapter by lazy {getSystemService(BluetoothManager::class.java).adapter }

    override fun onViewCreated() {

    }


    override fun setListener() {
        binding.btTransferBluetooth.setOnClickListener {

        }
        binding.btTransferWifi.setOnClickListener {  }
        binding.btTransferQrCode.setOnClickListener {
            binding.imgQr.setImageBitmap(generateQrCodeInfo())
            // receiver will scan the qr code, filter the bluetooth device that matches with the info in qr code. Then auto send a pairing request to listener
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val result = permissionRequired.all {
                checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
            }
            if (result) {
                checkBluetoothIsAvailable {
                    // start bluetooth listener
                }
            } else multiplePermissionRequest.launch(permissionRequired)
        } else {

        }
    }

    private fun checkBluetoothIsAvailable(onCheckOk: () -> Unit = {}) {
        val isBluetoothSupported = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
        if (!isBluetoothSupported) {
            Toast.makeText(this, "Bluetooth is not supported", Toast.LENGTH_SHORT).show()
        } else {
            if (bluetoothAdapter.isEnabled) onCheckOk()
            else enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }

    private fun startBluetoothListener() {
        lifecycleScope.launch(Dispatchers.IO) {
            bluetoothServerThread = BluetoothConnectingService.AcceptThread(bluetoothAdapter, "Listener", uniqueUuid)
            bluetoothServerThread?.start()
        }
    }

    // generate qr code with text type content
    private fun generateQrCodeInfo(): Bitmap {
        val size = 512
        val currentDeviceName = Build.MODEL
        val content = Gson().toJson(mapOf(
            "type" to "bluetooth",
            "deviceName" to currentDeviceName,
            "uuid" to uniqueUuid
        ))
        val hints = hashMapOf<EncodeHintType, Int>().also { it[EncodeHintType.MARGIN] = 1 } // Make the QR code buffer border narrower
        val bits = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
    }
}