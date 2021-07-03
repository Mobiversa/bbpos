package com.mobiversa.ezy2pay.ui.home

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.mobiversa.ezy2pay.MainActivity
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.base.BaseFragment
import com.mobiversa.ezy2pay.ui.bbpos.PaymentOptionsFragment
import com.mobiversa.ezy2pay.ui.ezyCash.EzyCashViewModel
import com.mobiversa.ezy2pay.ui.ezyMoto.EzyMotoViewModel
import com.mobiversa.ezy2pay.utils.Constants
import com.mobiversa.ezy2pay.utils.Fields
import com.mobiversa.ezy2pay.utils.PreferenceHelper
import kotlinx.android.synthetic.main.fragment_home.view.*

class HomeFragment : BaseFragment(), View.OnClickListener {

    lateinit var amtEdt: EditText

    private lateinit var homeViewModel: EzyCashViewModel
    private lateinit var viewModel: EzyMotoViewModel
    private lateinit var prefs: SharedPreferences
    private lateinit var customPrefs: SharedPreferences

    var bundle = Bundle()
    lateinit var mAlertDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(EzyCashViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        viewModel = ViewModelProvider(this).get(EzyMotoViewModel::class.java)

        prefs = PreferenceHelper.defaultPrefs(context!!)
        customPrefs = PreferenceHelper.customPrefs(context!!, "REMEMBER")

        setTitle("Home", true)

        amtEdt = root.edt_home_amount

        root.clear_txt.setOnClickListener(this)
        root.delete_txt.setOnClickListener(this)
        root.one_txt.setOnClickListener(this)
        root.two_txt.setOnClickListener(this)
        root.three_txt.setOnClickListener(this)
        root.four_txt.setOnClickListener(this)
        root.five_txt.setOnClickListener(this)
        root.six_txt.setOnClickListener(this)
        root.seven_txt.setOnClickListener(this)
        root.eight_txt.setOnClickListener(this)
        root.nine_txt.setOnClickListener(this)
        root.zero_txt.setOnClickListener(this)
        root.continuePayment.setOnClickListener {
            productDetails()
        }
        checkAndRequestPermissions()
        if (checkAndRequestPermissions())
            if (isGPSEnabled())
                getLocation()

        return root
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).supportActionBar?.show()
        amtEdt?.setText("0.00")
        setTitle("Mobi", true)
    }

    fun productDetails() {
        val amount = amtEdt.text.toString()
        val totalPrice = amount.toDouble()

        if (totalPrice < 5) {
            shortToast("Enter Amount more than 5 RM")
            return
        }

        if (!checkAndRequestPermissions()) {
            shortToast("Enable Location Permission From Settings")
            return
        }/* else if (!isLocationEnabled(this.context!!)) {
            isGPSEnabled()
            shortToast("Enable GPS to Start")
            return
        }*/

        ((getActivity() as MainActivity).getLocation())

        val fragment = PaymentOptionsFragment()
        bundle.clear()
        bundle.putString(Fields.Amount, amount)
        bundle.putString(Fields.InvoiceId, "")
        addFragment(fragment, bundle, Constants.BB_POS)
    }

    override fun onClick(view: View?) {

        if (view != null) {

            if (view.id == R.id.clear_txt) {
                amtEdt.setText("0.00")
            } else {

                var amount = amtEdt.text.toString().replace(".", "")
                val pressedVal = (view as TextView).text.toString()

                if (view.id == R.id.delete_txt) {
                    amount = if (amount.length == 1) "0" else amount.substring(0, amount.length - 1)
                } else {
                    if (amount.length < 12) {
                        amount += pressedVal
                    }
                }
                val totalAmount = java.lang.Long.parseLong(amount)
                var costDisplay = "0.00"
                when {
                    totalAmount < 10 -> costDisplay = "0.0$totalAmount"
                    totalAmount in 10..99 -> costDisplay = "0.$totalAmount"
                    else -> {
                        costDisplay = totalAmount.toString()
                        costDisplay = costDisplay.substring(
                            0,
                            costDisplay.length - 2
                        ) + "." + costDisplay.substring(costDisplay.length - 2)
                    }
                }
                amtEdt.setText(costDisplay)
            }
        }
    }
}
