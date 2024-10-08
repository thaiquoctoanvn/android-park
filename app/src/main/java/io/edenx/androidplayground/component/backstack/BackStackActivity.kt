package io.edenx.androidplayground.component.backstack

import android.content.Intent
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import io.edenx.androidplayground.component.base.BaseActivity
import io.edenx.androidplayground.data.ActivityLaunchFlag
import io.edenx.androidplayground.databinding.ActivityBackStackBinding

class BackStackActivity :
    BaseActivity<ActivityBackStackBinding>(ActivityBackStackBinding::inflate) {
    private val launchActivityIntentFlags = listOf(
        Intent.FLAG_ACTIVITY_CLEAR_TOP,
        Intent.FLAG_ACTIVITY_NEW_TASK,
        Intent.FLAG_ACTIVITY_SINGLE_TOP,
    )
    private var selectedLaunchMode: ActivityLaunchFlag = ActivityLaunchFlag.SINGLE_TOP

    override fun onViewCreated() {
        setUpLaunchModeSpinner()
        binding.btnLaunchActivity.setOnClickListener {
            startActivity(Intent(this, TestLaunchFlagActivity::class.java).apply {
                flags = getIntentFlag()
            })
        }
    }

    private fun setUpLaunchModeSpinner() {
        ArrayAdapter(this, android.R.layout.simple_spinner_item, ActivityLaunchFlag.values()).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerLaunchMode.adapter = it
            binding.spinnerLaunchMode.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {

                    if (parent.getItemAtPosition(pos) is ActivityLaunchFlag) {
                        selectedLaunchMode = ActivityLaunchFlag.values()[pos];
                        binding.btnLaunchActivity.text = "Start Activity with flag #${selectedLaunchMode.name}"
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
        }
    }

    private fun getIntentFlag(): Int {
        return when (selectedLaunchMode) {
            ActivityLaunchFlag.SINGLE_TOP -> Intent.FLAG_ACTIVITY_SINGLE_TOP
            ActivityLaunchFlag.NEW_TASK -> Intent.FLAG_ACTIVITY_NEW_TASK
            ActivityLaunchFlag.CLEAR_TOP -> Intent.FLAG_ACTIVITY_CLEAR_TOP
            else -> Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
    }
}