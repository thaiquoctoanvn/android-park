package io.edenx.androidpark.component.connectivity

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.edenx.androidpark.component.base.BaseActivity
import io.edenx.androidpark.databinding.ActivityBluetoothConnectingBinding
import io.edenx.androidpark.databinding.ItemTextBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BluetoothConnectingActivity :
    BaseActivity<ActivityBluetoothConnectingBinding>(ActivityBluetoothConnectingBinding::inflate) {

    private lateinit var bluetoothDeviceAdapter: BluetoothDeviceAdapter

    private val permissionRequired = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
    private var connectThread: BluetoothConnectingService.ConnectThread? = null
    private var listenThread: BluetoothConnectingService.AcceptThread? = null
    private val uniqueUuid = "e4ffd149-ae09-4c9a-bb99-87e4ea267c39"
    private var type = "listen"
    private val bluetoothDevices = mutableListOf<BluetoothDevice>()

    private val bluetoothAdapter by lazy { getSystemService(BluetoothManager::class.java).adapter }
    private val bluetoothFindReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Toast.makeText(this@BluetoothConnectingActivity, "Discovering bluetooth devices", Toast.LENGTH_SHORT).show()
                    Log.d("xxxx", "Discovery started")
                    bluetoothDevices.clear()
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d("xxxx", "Discovery finished, found: ${bluetoothDevices.size}")
                    bluetoothDeviceAdapter.submitList(bluetoothDevices)
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                        BluetoothAdapter.STATE_CONNECTING -> {
                            Toast.makeText(this@BluetoothConnectingActivity, "Connecting to bluetooth listener", Toast.LENGTH_SHORT).show()
                        }
                        BluetoothAdapter.STATE_CONNECTED -> {
                            Toast.makeText(this@BluetoothConnectingActivity, "Connected to bluetooth listener", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Toast.makeText(this@BluetoothConnectingActivity, "Unknown", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    bluetoothDevices.add(device!!)
                    Log.d("xxxx", "Found device name: ${device?.name}, MAC: ${device?.address}, found: ${bluetoothDevices.size}")
                }
            }
        }
    }
    @SuppressLint("MissingPermission")
    private val multiplePermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            val isAllPermissionGrant = it.entries.all { permission ->
                permission.value
            }
            if (isAllPermissionGrant) {
                doBluetoothAction()
            }
        }

    private val enableBluetoothLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            doBluetoothAction()
        }
    }

    override fun onViewCreated() {
        binding.rv.apply {
            bluetoothDeviceAdapter = BluetoothDeviceAdapter() {
                if (bluetoothAdapter.state == BluetoothAdapter.STATE_CONNECTED) return@BluetoothDeviceAdapter
                Toast.makeText(this@BluetoothConnectingActivity, "Start to connect", Toast.LENGTH_SHORT).show()
                lifecycleScope.launch(Dispatchers.IO) {
                    connectThread = BluetoothConnectingService.ConnectThread(bluetoothAdapter, it, uniqueUuid)
                    connectThread?.start()
                }
            }
            adapter = bluetoothDeviceAdapter
            layoutManager = LinearLayoutManager(this.context)
        }
        registerReceiver(bluetoothFindReceiver, IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        })
    }

    override fun onDestroy() {
        unregisterReceiver(bluetoothFindReceiver)
        listenThread?.cancel()
        connectThread?.cancel()
        super.onDestroy()
    }

    override fun setListener() {
        binding.btDiscovery.setOnClickListener {
            type = "discovery"
            binding.btStartListener.isEnabled = false
            requestPermissions()
            Toast.makeText(this, "Select a bluetooth device to connect", Toast.LENGTH_SHORT).show()
        }
        binding.btStartListener.setOnClickListener {
            type = "listen"
            binding.btDiscovery.isEnabled = false
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        val result = permissionRequired.all {
            checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
        }
        if (result) {
            doBluetoothAction()
        } else multiplePermissionRequest.launch(
            permissionRequired
        )
    }

    @SuppressLint("MissingPermission")
    private fun doBluetoothAction() {
        val isBluetoothSupported = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
        if (!isBluetoothSupported) {
            Toast.makeText(this, "Bluetooth is not supported", Toast.LENGTH_SHORT).show()
        } else {
            if (bluetoothAdapter.isEnabled) {
                if (type == "listen") {
                    startBluetoothListener()
                } else {
                    bluetoothAdapter?.startDiscovery()
                }
            }
            else enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }

    private fun startBluetoothListener() {
        lifecycleScope.launch(Dispatchers.IO) {
            listenThread = BluetoothConnectingService.AcceptThread(bluetoothAdapter, "Listener", uniqueUuid) {
                lifecycleScope.launch(Dispatchers.Main) {
                    Toast.makeText(this@BluetoothConnectingActivity, it, Toast.LENGTH_SHORT).show()
                }
            }
            listenThread?.start()
        }
    }

    class BluetoothDeviceAdapter(
        private val onItemClicked: (BluetoothDevice) -> Unit = {}
    ) : ListAdapter<BluetoothDevice, BluetoothDeviceAdapter.ItemHolder>(AdapterDiff()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
            return ItemHolder(ItemTextBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ))
        }

        override fun onBindViewHolder(holder: ItemHolder, position: Int) {
            holder.bindData(getItem(position))
        }

        inner class ItemHolder(private val binding: ItemTextBinding) :
            RecyclerView.ViewHolder(binding.root) {
            @SuppressLint("MissingPermission")
            fun bindData(item: BluetoothDevice) {
                binding.txt.text = "${item.name} - ${item.address}"
                binding.root.setOnClickListener {
                    onItemClicked(item)
                }
            }
        }
        class AdapterDiff : DiffUtil.ItemCallback<BluetoothDevice>() {
            override fun areItemsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean {
                return oldItem.address == newItem.address
            }

            override fun areContentsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }
        }
    }
}