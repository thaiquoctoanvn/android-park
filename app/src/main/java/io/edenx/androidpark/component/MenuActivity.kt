package io.edenx.androidpark.component

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.AndroidEntryPoint
import io.edenx.androidpark.R
import io.edenx.androidpark.component.animation.AnimationActivity
import io.edenx.androidpark.component.base.BaseActivity
import io.edenx.androidpark.component.camera.CameraActivity
import io.edenx.androidpark.component.connectivity.BluetoothConnectingActivity
import io.edenx.androidpark.component.connectivity.FileTransferActivity
import io.edenx.androidpark.component.nav.NavigationActivity
import io.edenx.androidpark.component.paging.PagingActivity
import io.edenx.androidpark.data.model.MenuItem
import io.edenx.androidpark.data.model.TypeMenu
import io.edenx.androidpark.databinding.ActivityMenuBinding
import io.edenx.androidpark.databinding.ItemMenuBinding
import io.edenx.androidpark.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MenuActivity : BaseActivity<ActivityMenuBinding>(ActivityMenuBinding::inflate) {

    @Inject
    lateinit var sharedPrefUtil: SharedPrefUtil

    @Inject
    lateinit var adUtil: AdUtil

    @Inject
    lateinit var billingUtil: BillingUtil

    private var bannerAdView: AdView? = null
    private var interstitialAd: InterstitialAd? = null
    private val purchaseLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

        }

    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated() {
        runUpdateProcess()
    }

    override fun setListener() {
        binding.btVip.setOnClickListener {
            purchaseLauncher.launch(Intent(this, PurchaseActivity::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                runUpdateProcess()
            }
        }
    }

    private fun runUpdateProcess() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(IMMEDIATE)
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    IMMEDIATE,
                    this,
                    UPDATE_REQUEST_CODE
                )
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_NOT_AVAILABLE) {
                loadScreen()

            } else finishAndRemoveTask()
        }.addOnFailureListener {
            Log.d("xxx", "Update exception: ${it.message}")
            loadScreen()
        }
    }

    private fun loadScreen() {
        binding.rvMenu.apply {
            adapter = MyMenuAdapter(mItems = provideMenuItems()) { item, view ->
                openSample(item)
            }
            layoutManager = GridLayoutManager(this.context, 2, RecyclerView.VERTICAL, false)
            addItemDecoration(DmitrysGridItemDecoration(24, 2))
        }

    }

    private fun openSample(item: MenuItem) {
        when (item.type) {
            TypeMenu.BILLING -> {}
            TypeMenu.PAGING -> {
                startActivity(Intent(this, PagingActivity::class.java))
            }
            TypeMenu.NAV -> {
                startActivity(Intent(this, NavigationActivity::class.java))
            }
            TypeMenu.ANIMATION -> {
                startActivity(Intent(this, AnimationActivity::class.java))
            }
            TypeMenu.IMG_LABELING -> {
                startActivity(Intent(this, CameraActivity::class.java).putExtra("type", CameraActivity.CameraScreenType.IMG_LABELING.name))
            }
            TypeMenu.QR_DETECTING -> {
                startActivity(Intent(this, CameraActivity::class.java).putExtra("type", CameraActivity.CameraScreenType.QR_DETECTING.name))
            }
            TypeMenu.FILE_TRANSFERRING -> {
                startActivity(Intent(this, FileTransferActivity::class.java))
            }
            TypeMenu.BLUETOOTH_DISCOVERY -> {
                startActivity(Intent(this, BluetoothConnectingActivity::class.java))
            }
            else -> {}
        }
    }

    private fun provideMenuItems() = listOf(
        MenuItem(1, type = TypeMenu.BILLING),
        MenuItem(2, type = TypeMenu.PAGING),
        MenuItem(3, type = TypeMenu.NAV),
        MenuItem(4, type = TypeMenu.ANIMATION),
        MenuItem(5, type = TypeMenu.IMG_LABELING),
        MenuItem(6, type = TypeMenu.QR_DETECTING),
        MenuItem(7, type = TypeMenu.FILE_TRANSFERRING),
        MenuItem(8, type = TypeMenu.BLUETOOTH_DISCOVERY),
    )
}

class MyMenuAdapter(
    mItems: List<MenuItem> = listOf(),
    private val onItemClick: (MenuItem, View) -> Unit = { _, _ -> }
) : ListAdapter<MenuItem, MyMenuAdapter.ItemHolder>(AdapterDiff()) {
    init {
        submitList(mItems)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(
            ItemMenuBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.bindData(getItem(position))
    }

    inner class ItemHolder(private val binding: ItemMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: MenuItem) {
            with(binding) {
                Glide.with(this.root.context)
                    .load(R.drawable.park)
                    .error(R.drawable.ic_baseline_error_outline_24)
                    .into(imgMenu)

                txtMenuName.text = item.name.trim().ifBlank { item.type.prompt }

                root.setOnClickListener {
                    onItemClick(item, it)
                }
            }
        }
    }

    // Diff adapter does not replace an element if a new element is recognized as the same.
    // We need to return false in both of checking funcs to ensure that the adapter always binds all data in case of duplication
    class AdapterDiff : DiffUtil.ItemCallback<MenuItem>() {
        override fun areItemsTheSame(oldItem: MenuItem, newItem: MenuItem): Boolean {
            return false
        }

        override fun areContentsTheSame(oldItem: MenuItem, newItem: MenuItem): Boolean {
            return false
        }
    }
}