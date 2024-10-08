package io.edenx.androidplayground.component

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
import io.edenx.androidplayground.BuildConfig
import io.edenx.androidplayground.R
import io.edenx.androidplayground.component.base.BaseActivity
import io.edenx.androidplayground.component.camera.CameraActivity
import io.edenx.androidplayground.data.model.MenuItem
import io.edenx.androidplayground.data.TypeMenu
import io.edenx.androidplayground.databinding.ActivityMenuBinding
import io.edenx.androidplayground.databinding.ItemMenuBinding
import io.edenx.androidplayground.util.*
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
        binding.txtTitle.text = "${if (BuildConfig.DEBUG) "Debug" else ""} Android Playground"
        Glide.with(this)
            .load("https://i.seadn.io/gae/TrLc8DM_fNZkyGU5XSLZ4rlYauAX7HmxjSXzBsstP17M6hAPZ1OIIwXI02KnPrrDskKqrqqRUY9klB5kcT9ulJIjrrN-_tfBawjbBw?auto=format&dpr=1&w=1000")
            .into(binding.btVip)
        binding.rvMenu.apply {
            adapter = MyMenuAdapter(mItems = provideMenuItems()) { item, view ->
                openSample(item)
            }
            layoutManager = GridLayoutManager(this.context, 2, RecyclerView.VERTICAL, false)
            addItemDecoration(DmitrysGridItemDecoration(32, 2))
        }
        //startActivity(Intent(this, PlaylistActivity::class.java))
    }

    private fun openSample(item: MenuItem) {
        when (item.type) {
            TypeMenu.IMG_LABELING -> {
                startActivity(Intent(this, item.type.screen).putExtra("type", CameraActivity.CameraScreenType.IMG_LABELING.name))
            }
            TypeMenu.QR_DETECTING -> {
                startActivity(Intent(this, item.type.screen).putExtra("type", CameraActivity.CameraScreenType.QR_DETECTING.name))
            }
            else -> {
                item.type.screen?.let {
                    startActivity(Intent(this, it))
                }
            }
        }
    }

    private fun provideMenuItems(): List<MenuItem> {
        return TypeMenu.values().mapIndexed { index, typeMenu ->
            MenuItem(index, type = typeMenu, bgColor = generateRandomColor())
        }
    }
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
                item.bgColor?.let { cv.setCardBackgroundColor(it) }
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