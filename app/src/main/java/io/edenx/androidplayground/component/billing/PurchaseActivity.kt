package io.edenx.androidplayground.component.billing

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.*
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import io.edenx.androidplayground.R
import io.edenx.androidplayground.data.model.PurchasePlanItem
import io.edenx.androidplayground.data.model.SimpleProductItem
import io.edenx.androidplayground.databinding.ActivityPurchaseBinding
import io.edenx.androidplayground.databinding.ItemPurchasePlanBinding
import io.edenx.androidplayground.component.base.BaseActivity
import io.edenx.androidplayground.util.*
import javax.inject.Inject

@AndroidEntryPoint
class PurchaseActivity : BaseActivity<ActivityPurchaseBinding>(ActivityPurchaseBinding::inflate) {

    @Inject
    lateinit var billingUtil: BillingUtil

    @Inject
    lateinit var purchasesUpdatedListenerImpl: PurchasesUpdatedListenerImpl

    @Inject
    lateinit var sharedPrefUtil: SharedPrefUtil
    private lateinit var purchasePlanAdapter: PurchasePlanAdapter

    private val productItems = mutableListOf<ProductDetails>()
    private var oldPurchase: Purchase? = null
    private var selectedPlan: ProductDetails? = null

    override fun onViewCreated() {
        binding.rvPurchasePlan.apply {
            purchasePlanAdapter = PurchasePlanAdapter(
                onItemClick = { item, _ ->
                    productItems.find { it.productId == item.id }?.let {
                        selectedPlan = it
                        billingUtil.launchPurchaseFlow(this@PurchaseActivity, it, oldPurchase)
                    }
                }
            )
            adapter = purchasePlanAdapter
            layoutManager = LinearLayoutManager(this.context)
            addItemDecoration(
                DividerItemDecoration(
                    this.context,
                    DividerItemDecoration.VERTICAL
                ).apply {
                    setDrawable(
                        ContextCompat.getDrawable(
                            this@PurchaseActivity,
                            R.drawable.list_spacing_12
                        )!!
                    )
                })
        }
        purchasesUpdatedListenerImpl.registerCallback(this::class.java.simpleName) { billingResult, purchases ->
            when {
                billingResult.responseCode == BillingClient.BillingResponseCode.OK && !purchases.isNullOrEmpty() -> {
                    purchases.forEach {
                        if (Gson().fromJson(it.originalJson, Map::class.java)["productId"] == selectedPlan?.productId) {
                            if (selectedPlan?.productType == BillingClient.ProductType.INAPP) {
                                billingUtil.consumePurchase(
                                    purchase = it,
                                    onPurchaseSucceed = { _, _ ->
                                        oldPurchase = it
                                        sharedPrefUtil.setIsBillingPurchased(true)
                                    },
                                    onPurchaseFailed = {
                                        sharedPrefUtil.setIsBillingPurchased(false)
                                    },
                                )
                            } else {
                                billingUtil.acknowledgePurchase(
                                    purchase = it,
                                    onPurchaseSucceed = { _ ->
                                        oldPurchase = it
                                        sharedPrefUtil.setIsBillingPurchased(true)
                                    },
                                    onPurchaseFailed = {
                                        sharedPrefUtil.setIsBillingPurchased(false)
                                    },
                                    onAcknowledged = {
                                        sharedPrefUtil.setIsBillingPurchased(true)
                                    }
                                )
                            }
                        }
                    }
                }

                billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED -> {
                    Log.d(BillingUtil.tag, "Purchase flow error cancelled by user")
                    sharedPrefUtil.setIsBillingPurchased(false)
                }

                else -> {
                    Log.d(BillingUtil.tag, "Purchase flow other error")
                }
            }
        }
        if (billingUtil.billingClient?.connectionState in listOf(
                BillingClient.ConnectionState.DISCONNECTED,
                BillingClient.ConnectionState.CLOSED
            )
        ) {
            billingUtil.connectToPlayStore(
                onConnected = {
                    showPurchasePlans()
                    checkPurchases()
                },
                onDisconnected = {}
            )
        } else {
            showPurchasePlans()
            checkPurchases()
        }
    }

    override fun setListener() {
        binding.btBack.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }
    }

    override fun onBackPressed() {
        binding.btBack.performClick()
    }

    private fun showPurchasePlans() {
        kotlin.runCatching {
            Gson().fromJson(
                Firebase.remoteConfig.getString(INAPP_PRODUCTS_KEY),
                SimpleProductItem::class.java
            )?.products?.let {
                billingUtil.queryProductDetails(it.associate {
                    it.id!! to it.type!!
                }) { list ->
                    productItems.clear()
                    if (list.isNotEmpty()) productItems.addAll(list)
                    purchasePlanAdapter.submitList(productItems.map {
                        PurchasePlanItem(
                            id = it.productId,
                            title = it.name,
                            price = (if (it.productType == BillingClient.ProductType.SUBS) it.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.find { it.priceAmountMicros != 0L }?.formattedPrice
                            else it.oneTimePurchaseOfferDetails?.formattedPrice)
                                ?: "Error to load price",
                            description = it.description
                        )
                    })
                }
            }
        }.onFailure {
            it.printStackTrace()
        }
    }

    private fun checkPurchases() {
        billingUtil.queryPurchases(
            BillingClient.ProductType.INAPP,
            BillingClient.ProductType.SUBS
        ) { purchaseList ->
            if (purchaseList.isEmpty()) sharedPrefUtil.setIsBillingPurchased(false)
            purchaseList.forEach {
                if (it.key == BillingClient.ProductType.SUBS) {
                    it.value.forEach {
                        if (it.purchaseState == Purchase.PurchaseState.PURCHASED && it.isAcknowledged) oldPurchase =
                            it
                    }
                } else {
                    // Still not return the purchased in-app products, only subs are returned. It was known a bug. For this reason we couldn't check if user bought the lifetime product to prevent them purchasing it again
                }
            }
        }
    }
}

class PurchasePlanAdapter(
    mItems: List<PurchasePlanItem> = listOf(),
    private val onItemClick: (PurchasePlanItem, View) -> Unit = { _, _ -> }
) : androidx.recyclerview.widget.ListAdapter<PurchasePlanItem, PurchasePlanAdapter.ItemHolder>(
    AdapterDiff()
) {

    var currentSelection: PurchasePlanItem? = null

    init {
        submitList(mItems)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(
            ItemPurchasePlanBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.bindData(getItem(position))
    }

    inner class ItemHolder(private val binding: ItemPurchasePlanBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: PurchasePlanItem) {
            with(binding) {
                txtTitle.text = "${item.price}/${item.title}"
                txtPrice.text = item.description
                root.setOnClickListener {
                    currentSelection?.isSelected = false
                    currentList.indexOf(currentSelection).takeIf { it >= 0 }
                        ?.let { notifyItemChanged(it) }
                    item.isSelected = true
                    notifyItemChanged(adapterPosition)
                    currentSelection = item
                    onItemClick(item, it)
                }
            }
        }
    }

    class AdapterDiff : DiffUtil.ItemCallback<PurchasePlanItem>() {
        override fun areItemsTheSame(
            oldItem: PurchasePlanItem,
            newItem: PurchasePlanItem
        ): Boolean {
            return false
        }

        override fun areContentsTheSame(
            oldItem: PurchasePlanItem,
            newItem: PurchasePlanItem
        ): Boolean {
            return false
        }
    }
}