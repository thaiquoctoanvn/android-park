package io.edenx.androidplayground.component.payment.googlepay

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import com.google.android.gms.wallet.button.ButtonOptions
import com.google.android.gms.wallet.contract.TaskResultContracts
import io.edenx.androidplayground.component.base.BaseActivity
import io.edenx.androidplayground.databinding.ActivityGooglePayBinding
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode

class GooglePayActivity :
    BaseActivity<ActivityGooglePayBinding>(ActivityGooglePayBinding::inflate) {
    private val cents = BigDecimal(100)
    private var paymentsClient: PaymentsClient? = null
    private val baseRequest = JSONObject().apply {
        put("apiVersion", 2)
        put("apiVersionMinor", 0)
    }
    private val allowedCardNetworks = JSONArray(
        listOf(
            "AMEX",
            "DISCOVER",
            "INTERAC",
            "JCB",
            "MASTERCARD",
            "VISA",
        )
    )
    private val allowedCardAuthMethods = JSONArray(
        listOf(
            "PAN_ONLY",
            "CRYPTOGRAM_3DS",
        )
    )
    private val merchantInfo: JSONObject =
        JSONObject().put(
            "merchantName",
            "If this is null a Pay Unverified Merchant will be used by default"
        )
    private val paymentDataLauncher =
        registerForActivityResult(TaskResultContracts.GetPaymentDataResult()) { taskResult ->
            when (taskResult.status.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    taskResult.result!!.let {
                        Log.i("Google Pay result:", it.toJson())

                    }
                }

                CommonStatusCodes.CANCELED -> {
                    // The user canceled
                }

                CommonStatusCodes.DEVELOPER_ERROR -> {
                    // The API returned an error(it.status: Status)
                }

                else -> {
                    // Handle internal and other unexpected errors
                }
            }
        }

    override fun onViewCreated() {
        setUpPayButton()
        possiblyShowGooglePayButton()
    }

    override fun setListener() {
        binding.btnGooglePay.setOnClickListener {
            requestPayment(1000L)
        }
    }

    private fun setUpPayButton() {
        binding.btnGooglePay.initialize(
            ButtonOptions.newBuilder()
                .setAllowedPaymentMethods(buildAllowedPaymentMethods().toString()).build()
        )
    }

    private fun gatewayTokenizationSpecification(): JSONObject {
        return JSONObject().apply {
            put("type", "PAYMENT_GATEWAY")
            put(
                "parameters",
                JSONObject(
                    mapOf(
                        "gateway" to "example",
                        "gatewayMerchantId" to "exampleGatewayMerchantId"
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
                    .put("allowedAuthMethods", allowedCardAuthMethods)
                    .put("allowedCardNetworks", allowedCardNetworks)
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

    private fun createPaymentsClient(context: Context): PaymentsClient {
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
            .build()

        return Wallet.getPaymentsClient(context, walletOptions)
    }

    private fun possiblyShowGooglePayButton() {
        buildAllowedPaymentRequest()?.let {
            val payRequest = IsReadyToPayRequest.fromJson(it.toString())
            if (paymentsClient == null) {
                paymentsClient = createPaymentsClient(this)
            }
            paymentsClient!!.isReadyToPay(payRequest).addOnCompleteListener { task ->
                kotlin.runCatching {
                    binding.btnGooglePay.visibility =
                        if (task.isSuccessful) View.VISIBLE else View.GONE

                }.onFailure {
                    Log.e("xxx", "Google Pay is not available")
                    Toast.makeText(this, "Google Pay is not available", Toast.LENGTH_SHORT)
                }
            }
        }
    }

    private fun getTransactionInfo(price: String): JSONObject =
        JSONObject()
            .put("totalPrice", price)
            .put("totalPriceStatus", "FINAL")
            .put("countryCode", "US")
            .put("currencyCode", "USD")

    private fun buildPaymentDataRequest(priceCents: Long): JSONObject {
        return baseRequest
            .put("allowedPaymentMethods", buildAllowedPaymentMethods())
            .put(
                "transactionInfo", getTransactionInfo(
                    BigDecimal(priceCents)
                        .divide(cents)
                        .setScale(2, RoundingMode.HALF_EVEN)
                        .toString()
                )
            )
            .put("merchantInfo", merchantInfo)
            .put("shippingAddressRequired", true)
            .put(
                "shippingAddressParameters", JSONObject()
                    .put("phoneNumberRequired", false)
                    .put("allowedCountryCodes", JSONArray(listOf("US", "GB")))
            );
    }

    private fun requestPayment(priceCents: Long) {
        paymentsClient?.loadPaymentData(
            PaymentDataRequest.fromJson(
                buildPaymentDataRequest(
                    priceCents
                ).toString()
            )
        )?.addOnCompleteListener {
            paymentDataLauncher.launch(it)
        }
    }
}