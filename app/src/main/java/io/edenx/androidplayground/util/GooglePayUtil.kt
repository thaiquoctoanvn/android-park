package io.edenx.androidplayground.util

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import com.google.android.gms.wallet.button.ButtonOptions
import com.google.android.gms.wallet.contract.ApiTaskResult
import com.google.android.gms.wallet.contract.TaskResultContracts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID
import javax.inject.Inject

class GooglePayUtil @Inject constructor(
    private val context: Context,
    private val merchantInfo: JSONObject? = null,
) {
    companion object {
        const val tag = "GooglePayUtil"
    }

    private lateinit var smallestDenomination: BigDecimal
    private lateinit var allowedCardNetworks: List<String>
    private lateinit var allowedCardAuthMethods: List<String>
    private lateinit var defaultMerchantInfo: JSONObject
    private lateinit var gatewayName: String
    private lateinit var gatewayMerchantId: String
    private lateinit var countryCode: String
    private lateinit var currencyCode: String
    private lateinit var allowedCountryCodes: List<String>


    private var paymentsClient: PaymentsClient? = null
    private val baseRequest = JSONObject().apply {
        put("apiVersion", 2)
        put("apiVersionMinor", 0)
    }

    init {
        createPaymentsClient(this.context)
    }

    fun setUpRequiredParameters(
        smallestDenomination: Int = 100,
        allowedCardNetworks: List<String> = listOf(
            "AMEX",
            "DISCOVER",
            "INTERAC",
            "JCB",
            "MASTERCARD",
            "VISA",
        ),
        allowedCardAuthMethods: List<String> = listOf(
            "PAN_ONLY",
            "CRYPTOGRAM_3DS",
        ),
        merchantName: String? = null,
        gatewayName: String = "example",
        gatewayMerchantId: String = "exampleGatewayMerchantId",
        countryCode: String = "US",
        currencyCode: String = "USD",
        allowedCountryCodes: List<String> = listOf("US", "GB"),
    ) {
        // If this is null a Pay Unverified Merchant will be used by default
        defaultMerchantInfo = JSONObject().put(
            "merchantName",
            merchantName,
        )
        this.smallestDenomination = BigDecimal(smallestDenomination)
        this.allowedCardNetworks = allowedCardNetworks
        this.allowedCardAuthMethods = allowedCardAuthMethods
        this.gatewayName = gatewayName
        this.gatewayMerchantId = gatewayMerchantId
        this.countryCode = countryCode
        this.currencyCode = currencyCode
        this.allowedCountryCodes = allowedCountryCodes
    }

    private fun checkRequiredParameters() {
        val errorMessage = "Be sure you have run setUpRequiredParameters yet"
        check(::defaultMerchantInfo.isInitialized) {
            errorMessage
        }
        check(::smallestDenomination.isInitialized) {
            errorMessage
        }
        check(::allowedCardNetworks.isInitialized) {
            errorMessage
        }
        check(::allowedCardAuthMethods.isInitialized) {
            errorMessage
        }
        check(::gatewayName.isInitialized) {
            errorMessage
        }
        check(::gatewayMerchantId.isInitialized) {
            errorMessage
        }
        check(::countryCode.isInitialized) {
            errorMessage
        }
        check(::currencyCode.isInitialized) {
            errorMessage
        }
        check(::allowedCountryCodes.isInitialized) {
            errorMessage
        }
    }

    private fun createPaymentsClient(context: Context): PaymentsClient {
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
            .build()
        return Wallet.getPaymentsClient(context, walletOptions)
    }

    private fun gatewayTokenizationSpecification(): JSONObject {
        return JSONObject().apply {
            put("type", "PAYMENT_GATEWAY")
            put(
                "parameters",
                JSONObject(
                    mapOf(
                        "gateway" to "example",
                        "gatewayMerchantId" to "exampleGatewayMerchantId",
                    ),
                ),
            )
        }
    }

    private fun buildBaseCardPaymentMethod(): JSONObject =
        JSONObject()
            .put("type", "CARD")
            .put(
                "parameters", JSONObject()
                    .put("allowedAuthMethods", JSONArray(allowedCardAuthMethods))
                    .put("allowedCardNetworks", JSONArray(allowedCardNetworks))
                    .put("billingAddressRequired", true)
                    .put(
                        "billingAddressParameters", JSONObject()
                            .put("format", "FULL")
                    )
            )

    private fun buildCardPaymentMethod(): JSONObject = buildBaseCardPaymentMethod()
        .put("tokenizationSpecification", gatewayTokenizationSpecification())

    private fun buildAllowedPaymentMethods(): JSONArray {
        return JSONArray().put(buildCardPaymentMethod())
    }

    // Request to check if payment allowed on device
    private fun buildAllowedPaymentRequest(): JSONObject? =
        try {
            baseRequest
                .put("allowedPaymentMethods", buildAllowedPaymentMethods())
        } catch (e: JSONException) {
            null
        }

    fun buildPayButtonOptions(): ButtonOptions {
        checkRequiredParameters()
        val a =  ButtonOptions.newBuilder()
            .setAllowedPaymentMethods(buildAllowedPaymentMethods().toString()).build()
        return a
    }

    fun checkIfGooglePayAvailable(
        onResult: (Boolean) -> Unit,
    ) {
        checkRequiredParameters()
        buildAllowedPaymentRequest()?.let {
            val payRequest = IsReadyToPayRequest.fromJson(it.toString())
            if (paymentsClient == null) {
                paymentsClient = createPaymentsClient(context)
            }
            paymentsClient!!.isReadyToPay(payRequest).addOnCompleteListener { task ->
                kotlin.runCatching {
                    onResult(task.isSuccessful)
                }.onFailure {
                    Log.e(tag, "checkIfGooglePayAvailable: ${it.message}")
                    onResult(false)
                }
            }
        }
    }

    private fun getTransactionInfo(price: String): JSONObject =
        JSONObject()
            .put("totalPrice", price)
            .put("totalPriceStatus", "FINAL")
            .put("countryCode", countryCode)
            .put("currencyCode", currencyCode)

    private fun buildPaymentDataRequest(priceCents: Long): JSONObject {
        return baseRequest
            .put("allowedPaymentMethods", buildAllowedPaymentMethods())
            .put(
                "transactionInfo", getTransactionInfo(
                    BigDecimal(priceCents)
                        .divide(smallestDenomination)
                        .setScale(2, RoundingMode.HALF_EVEN)
                        .toString()
                )
            )
            .put("merchantInfo", merchantInfo)
            .put("shippingAddressRequired", true)
            .put(
                "shippingAddressParameters", JSONObject()
                    .put("phoneNumberRequired", false)
                    .put("allowedCountryCodes", JSONArray(allowedCountryCodes))
            );
    }

    fun requestPayment(
        priceCents: Long,
        activity: ComponentActivity,
        onRequestCompleted: (ApiTaskResult<PaymentData>) -> Unit,
    ) {
        paymentsClient?.loadPaymentData(
            PaymentDataRequest.fromJson(
                buildPaymentDataRequest(
                    priceCents
                ).toString()
            )
        )?.addOnCompleteListener { task ->
            CoroutineScope(Dispatchers.Main).launch {
                callbackFlow<ApiTaskResult<PaymentData>> {
                    val paymentDataLauncher = activity.activityResultRegistry.register(
                        UUID.randomUUID().toString(), TaskResultContracts.GetPaymentDataResult()
                    ) { taskResult ->
                        trySend(taskResult)
                    }
                    paymentDataLauncher.launch(task)
                    awaitClose {
                        paymentDataLauncher.unregister()
                    }
                }.collectLatest {
                    onRequestCompleted(it)
                }
            }
        }
    }
}