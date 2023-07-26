package io.edenx.androidplayground.util

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BillingUtil @Inject constructor(
    private val context: Context,
    private val purchasesUpdatedListener: PurchasesUpdatedListenerImpl
) {

    companion object {
        const val tag = "BillingUtil"
    }

    var billingClient: BillingClient? = null

    init {
        initBillingClient(context)
    }

    private fun initBillingClient(
        context: Context
    ): BillingClient? {
        if (billingClient == null) {
            billingClient = BillingClient.newBuilder(context)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build()
        }
        return billingClient
    }

    fun connectToPlayStore(
        onConnected: (BillingResult) -> Unit = {},
        onDisconnected: () -> Unit = {}
    ) {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                Log.d(tag, "onBillingServiceDisconnected")
                onDisconnected()
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(tag, "onBillingSetupFinished")
                    onConnected(billingResult)
                }
            }
        })
    }

    fun queryProductDetails(
        productIdAndTypeList: Map<String, String>,
        onQueryResponded: (List<ProductDetails>) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            combine(flows = productIdAndTypeList.entries.groupBy { it.value }.map {
                callbackFlow<List<ProductDetails>> {
                    val queryProductDetailsParams =
                        QueryProductDetailsParams.newBuilder()
                            .setProductList(it.value.map {
                                QueryProductDetailsParams.Product.newBuilder()
                                    .setProductId(it.key)
                                    .setProductType(it.value)
                                    .build()
                            })
                            .build()
                    billingClient?.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
                        Log.d(tag, "queryProductDetailsAsync: ${billingResult.responseCode}")
                        trySend(productDetailsList)
                    }
                    awaitClose()
                }.catch {
                    it.printStackTrace()
                }
            }, transform = {
                it.flatMap { it }
            }).collectLatest {
                withContext(Dispatchers.Main) {
                    onQueryResponded(it)
                }
            }
        }


//        val queryProductDetailsParams =
//            QueryProductDetailsParams.newBuilder()
//                .setProductList(productIdAndTypeList.map {
//                    QueryProductDetailsParams.Product.newBuilder()
//                        .setProductId(it.key)
//                        .setProductType(it.value)
//                        .build()
//                })
//                .build()
//        billingClient?.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
//            Log.d(tag, "queryProductDetailsAsync: ${billingResult.responseCode}")
//            Handler(Looper.getMainLooper()).post {
//                onQueryResponded(billingResult, productDetailsList)
//            }
//        }
    }

    fun queryPurchases(
        vararg productType: String,
        onQueryResponded: (Map<String, List<Purchase>>) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            combine(flows = productType.map {
                callbackFlow<Pair<String, List<Purchase>>> {
                    billingClient?.queryPurchasesAsync(
                        QueryPurchasesParams.newBuilder().setProductType(it).build()
                    ) { billingResult, purchaseList ->
                        Log.d(tag, "queryProductDetailsAsync: ${billingResult.responseCode}")
                        trySend(Pair(it, purchaseList))
                    }
                    awaitClose()
                }.catch {
                    it.printStackTrace()
                }
            }, transform = {
                it.associate { it.first to it.second }
            }).collectLatest {
                withContext(Dispatchers.Main) {
                    onQueryResponded(it)
                }
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails, oldPurchase: Purchase?): BillingResult? {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(productDetails.subscriptionOfferDetails?.first()?.offerToken ?: "")
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
        oldPurchase?.let {
            val subscriptionUpdateParams = BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                .setOldPurchaseToken(oldPurchase.purchaseToken)
                .setSubscriptionReplacementMode(BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.CHARGE_FULL_PRICE)
                .build()
            billingFlowParams.setSubscriptionUpdateParams(subscriptionUpdateParams)
        }

        return billingClient?.launchBillingFlow(activity, billingFlowParams.build())
    }

    // For product
    fun consumePurchase(
        purchase: Purchase,
        onPurchaseSucceed: (BillingResult, String) -> Unit = { _, _ -> },
        onPurchaseFailed: (BillingResult) -> Unit = {},
        onNotPurchasedYet: (Purchase) -> Unit = {},
        onConsumed: (Purchase) -> Unit = {}
    ) {
        val consumeParams =
            ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        billingClient?.consumeAsync(consumeParams) { billingResult, purchaseToken ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) onPurchaseSucceed(
                billingResult, purchaseToken
            )
            else onPurchaseFailed(billingResult)
        }
    }

    // For subs
    fun acknowledgePurchase(
        purchase: Purchase,
        onPurchaseSucceed: (BillingResult) -> Unit = {},
        onPurchaseFailed: (BillingResult) -> Unit = {},
        onNotPurchasedYet: (Purchase) -> Unit = {},
        onAcknowledged: (Purchase) -> Unit = {}
    ) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient?.acknowledgePurchase(acknowledgePurchaseParams) {
                    if (it.responseCode == BillingClient.BillingResponseCode.OK) onPurchaseSucceed(
                        it
                    )
                    else onPurchaseFailed(it)
                }
            } else onAcknowledged(purchase)
        } else onNotPurchasedYet(purchase)
    }
}

class PurchasesUpdatedListenerImpl : PurchasesUpdatedListener {
    private val receivers = mutableMapOf<String, (BillingResult, MutableList<Purchase>?) -> Unit>()

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        receivers.forEach {
            it.value(billingResult, purchases)
        }
    }

    fun registerCallback(tag: String, callback: (BillingResult, MutableList<Purchase>?) -> Unit) {
        if (receivers.contains(tag)) receivers.replace(tag, callback)
        else receivers[tag] = callback
    }

    fun unregisterCallback(tag: String) {
        receivers.remove(tag)
    }
}