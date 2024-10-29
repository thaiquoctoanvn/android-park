package io.edenx.androidplayground.component.payment.googlepay

import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import io.edenx.androidplayground.component.base.BaseActivity
import io.edenx.androidplayground.databinding.ActivityGooglePayBinding
import io.edenx.androidplayground.util.GooglePayUtil
import javax.inject.Inject

@AndroidEntryPoint
class GooglePayActivity :
    BaseActivity<ActivityGooglePayBinding>(ActivityGooglePayBinding::inflate) {
    @Inject
    lateinit var googlePayUtil: GooglePayUtil

    override fun onViewCreated() {
        googlePayUtil.setUpRequiredParameters()
        setUpPayButton()
        possiblyShowGooglePayButton()
    }

    override fun setListener() {
        binding.btnGooglePay.setOnClickListener {
            googlePayUtil.requestPayment(
                priceCents = 1000L,
                activity = this,
                onRequestCompleted = {},
            )
        }
    }

    private fun setUpPayButton() {
        binding.btnGooglePay.initialize(googlePayUtil.buildPayButtonOptions())
    }


    private fun possiblyShowGooglePayButton() {
        googlePayUtil.checkIfGooglePayAvailable {
            binding.btnGooglePay.visibility =
                if (it) View.VISIBLE else View.GONE
        }
    }
}