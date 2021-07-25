package com.mobiversa.ezy2pay.ui.settings

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.gson.Gson
import com.mobiversa.ezy2pay.MainActivity
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.base.BaseFragment
import com.mobiversa.ezy2pay.network.ApiService
import com.mobiversa.ezy2pay.network.response.LoginResponse
import com.mobiversa.ezy2pay.network.response.SuccessModel
import com.mobiversa.ezy2pay.ui.contact.ContactActivity
import com.mobiversa.ezy2pay.ui.ezyMoto.EzyMotoViewModel
import com.mobiversa.ezy2pay.ui.settings.applicationUpdate.ApplicationUpdateFragment
import com.mobiversa.ezy2pay.ui.settings.profileProductList.KeyboardFragment
import com.mobiversa.ezy2pay.ui.settings.profileProductList.ProfileProductListFragment
import com.mobiversa.ezy2pay.ui.settings.profileUpdate.ProfileUpdateFragment
import com.mobiversa.ezy2pay.ui.tutorial.TutorialActivity
import com.mobiversa.ezy2pay.utils.Constants
import com.mobiversa.ezy2pay.utils.Constants.Companion.ProductResponse
import com.mobiversa.ezy2pay.utils.Fields
import com.mobiversa.ezy2pay.utils.Fields.Companion.username
import com.mobiversa.ezy2pay.utils.PreferenceHelper
import com.mobiversa.ezy2pay.utils.PreferenceHelper.set
import kotlinx.android.synthetic.main.settings_fragment.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingsFragment : BaseFragment(), View.OnClickListener {

    var bundle = Bundle()

    companion object {
        fun newInstance() = SettingsFragment()
    }

    lateinit var mAlertDialog: AlertDialog

    private lateinit var viewModel: SettingsViewModel
    private lateinit var motoViewModel: EzyMotoViewModel
    private lateinit var prefs: SharedPreferences
    private var balance = 0.00
    private var percent : Double = 0.0
    private val deliveryApiService = ApiService.serviceRequest()
    lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.settings_fragment, container, false)
        viewModel = ViewModelProviders.of(this).get(SettingsViewModel::class.java)
        motoViewModel = ViewModelProviders.of(this).get(EzyMotoViewModel::class.java)

        setTitle("Profile", true)
        prefs = PreferenceHelper.defaultPrefs(context!!)

        rootView.txt_profile_name.text = getSharedString(Constants.UserName)
        clearRegData()

        rootView.prof_product_linear.setOnClickListener(this)
        rootView.prof_contact_us_linear.setOnClickListener(this)
        rootView.prof_update_linear.setOnClickListener(this)
        rootView.prof_refer_linear.setOnClickListener(this)
        rootView.upgrade_btn.setOnClickListener(this)

        if (getLoginResponse().hostType.equals("U", true) && Constants.EzyMoto.equals(
                "EZYLINK",
                true
            ) && getProductList()[0].isEnable
        ) {
            rootView.prof_refer_linear.visibility = View.VISIBLE
        }
        else {
            rootView.prof_refer_linear.visibility = View.GONE
        }

        rootView.txt_logout.setOnClickListener(this)

        val productParams = HashMap<String, String>()
        productParams[Fields.Service] = Fields.ProductList
        productParams[username] = getSharedString(Constants.UserName)
        productParams[Fields.sessionId] = getLoginResponse().sessionId
        productParams[username] = getSharedString(Constants.UserName)
        jsonProductList(productParams)

        if (getLoginResponse().type.equals(Constants.LITE)) {
            rootView.lite_linear.visibility = View.VISIBLE
            rootView.upgrade_btn.setText(getLoginResponse().liteUpdate)
            val balance = prefs.getString(Constants.Balance, "0.0")?.toFloat()
            getBalance()
        } else {
            rootView.lite_linear.visibility = View.GONE
        }

        val liteUpdate = prefs.getString(Constants.UpgradeStatus, "UPGRADE")?.toString()

        if (liteUpdate.equals(Constants.processing)) {
            rootView.upgrade_btn.isEnabled = false
            rootView.upgrade_btn.text = "Processing"
        } else {
            rootView.upgrade_btn.isEnabled = true
            rootView.upgrade_btn.text = "Upgrade"
        }

        return rootView
    }

    private fun getBalance() {
//        showDialog("Loading...")
        val paymentParams = HashMap<String, String>()
        paymentParams[Fields.liteMid] = getLoginResponse().liteMid
        paymentParams[Fields.Service] = Fields.LITE_DTL
        paymentParams[Fields.txnAmount] = "0"
        motoViewModel.ezyMotoPaymentCheck(paymentParams)
        motoViewModel.ezyPayment.observe(viewLifecycleOwner, Observer {
//            cancelDialog()
            if (it.responseCode.equals("0000", true)) {
                prefs[Constants.Balance] = it.responseData.dtlLimit
                balance = it.responseData.dtlLimit.toDouble()
                percent = (((500 - balance )/ 500) * 100)
                var data = (((500 - balance )/ 500) * 100).toInt().toString()
                data = (100 - data.toInt()).toString()
                rootView.balance_percent_txt.text = "$data\t%"
                rootView.balance_amt_txt.text = it.responseData.dtlLimit

                showLog("Percent", ""+percent)
                    when(data.toInt()){
                        in 0..20 -> {
                           rootView.graph_img.setImageDrawable(resources.getDrawable(R.drawable.ic_0_graph))
                        }
                        in 20..41 -> {
                            rootView.graph_img.setImageDrawable(resources.getDrawable(R.drawable.ic_25_graph))
                        }
                        in 40..65 -> {
                            rootView.graph_img.setImageDrawable(resources.getDrawable(R.drawable.ic_50_graph))
                        }
                        in 65..85 -> {
                            rootView.graph_img.setImageDrawable(resources.getDrawable(R.drawable.ic_75_graph))
                        }
                        in 85..100 -> {
                            rootView.graph_img.setImageDrawable(resources.getDrawable(R.drawable.ic_100_graph))
                        }
                    }

            } else {
                shortToast(it.responseDescription)
            }

        })
    }

    private fun showUpgradePrompt() {

        val inflater = getActivity()!!.layoutInflater
        val alertLayout: View = inflater.inflate(R.layout.alert_upgrade, null)
        val mBuilder = context.let {
            AlertDialog.Builder(it)
                .setView(alertLayout)
        }
        val updateImg = alertLayout.findViewById<View>(R.id.update_img) as AppCompatImageView
        val cancel_txt = alertLayout.findViewById<View>(R.id.cancel_txt) as AppCompatTextView
        val upgrade_txt = alertLayout.findViewById<View>(R.id.upgrade_txt) as AppCompatTextView
        val desc_txt = alertLayout.findViewById<View>(R.id.desc_txt) as AppCompatTextView
        val alert: AlertDialog.Builder = AlertDialog.Builder(getActivity(),R.style.AlertDialogTheme)
        alert.setView(alertLayout)
        alert.setCancelable(true)

        val liteUpdate = prefs.getString(Constants.UpgradeStatus, "UPGRADE")?.toString()

        if (liteUpdate.equals(Constants.processing)) {
            upgrade_txt.isEnabled = false
            upgrade_txt.text = "Processing"
        } else {
            upgrade_txt.isEnabled = true
            upgrade_txt.text = "Upgrade"
        }

        upgrade_txt.text = "Yes, I'm Sure"
        updateImg.setImageDrawable(resources.getDrawable(R.drawable.ic_upgrade))
        desc_txt.text = "You will need to submit documents like Business Registration, Bank statement and a couple more to upgrade and increase the daily limit.\n Are you sure you want to Upgrade?"

        upgrade_txt.setOnClickListener {
            upgradeUser()
        }

        cancel_txt.setOnClickListener {

            mAlertDialog.dismiss()
        }

        mAlertDialog = mBuilder.show()
        mAlertDialog.setCancelable(false)
        mAlertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        mAlertDialog.window?.setBackgroundDrawable(ColorDrawable(this.resources.getColor(android.R.color.transparent)))
        val dialogWindow = mAlertDialog.window
        dialogWindow?.setGravity(Gravity.CENTER)
    }

    private fun upgradeUser() {
        showDialog("Loading...")
        val paymentParams = HashMap<String, String>()
        paymentParams[Fields.merchantId] = getLoginResponse().merchantId
        paymentParams[Fields.Service] = Fields.LITE_MERCHANT_UPGRADE
        paymentParams[Fields.email] = getSharedString(Constants.UserName)
        motoViewModel.upgradeEzymoto(paymentParams)
        motoViewModel.upgradeData.observe(viewLifecycleOwner, Observer {
            cancelDialog()
            if (it.responseCode.equals("0000")) {

                getLoginResponse().liteUpdate = Constants.processing
                rootView.upgrade_btn.isEnabled = false
                rootView.upgrade_btn.text = "Processing"

                showLog("Pro", getLoginResponse().liteUpdate)

                shortToast(it.responseDescription)
                showLog("Status ", getLoginResponse().liteUpdate)
            } else {
                shortToast(it.responseDescription)
            }

        })
        mAlertDialog.dismiss()
    }

    override fun onResume() {
        super.onResume()
        clearRegData()
        setTitle("Profile", true)
        (activity as MainActivity).supportActionBar?.show()
    }

    private fun jsonProductList(boostParams: HashMap<String, String>) {
        showDialog("Processing...")
        viewModel.profileProductList(boostParams)
        viewModel.settingsModel.observe(viewLifecycleOwner, Observer {
            cancelDialog()
            if (it.responseCode.equals("0000", true)) {
                val gson = Gson()
                val json = gson.toJson(it.responseData)
                putSharedString(ProductResponse, json)
            }
        })
    }

    //Login User
    private fun jsonLogout(loginMap: HashMap<String, String>) {
        showDialog("Logout User...")
        deliveryApiService.logoutUser(loginMap).enqueue(object : Callback<SuccessModel> {
            override fun onFailure(call: Call<SuccessModel>, t: Throwable) {
                showLog("LoginModel Fail", "" + t.message)
                shortToast(t.message!!)
            }

            override fun onResponse(call: Call<SuccessModel>, response: Response<SuccessModel>) {
                if (response.isSuccessful) {
                    cancelDialog()
                    logoutUser()
                } else {
                    showLog("LoginModel Fail", "" + response.message())
                    cancelDialog()
                }
                shortToast(response.message())
            }
        })
    }

    private fun showLogoutPrompt() {
        lateinit var mAlertDialog: AlertDialog

        val inflater = getActivity()!!.layoutInflater
        val alertLayout: View = inflater.inflate(R.layout.alert_logout, null)
        val positiveBtn = alertLayout.findViewById<View>(R.id.positive_btn) as Button
        val negativeBtn = alertLayout.findViewById<View>(R.id.negative_btn) as Button
        val alert: AlertDialog.Builder = AlertDialog.Builder(getActivity(),R.style.AlertDialogTheme)
        alert.setView(alertLayout)
        alert.setCancelable(false)

        positiveBtn.setOnClickListener {
            mAlertDialog.dismiss()
            val reqParam = HashMap<String, String>()
            reqParam[Fields.Service] = Fields.LOGOUT_MOB
            reqParam[Fields.sessionId] = getLoginResponse().sessionId
            jsonLogout(reqParam)
        }

        negativeBtn.setOnClickListener {
            mAlertDialog.dismiss()
        }

        mAlertDialog = alert.create()
        mAlertDialog.show()
        //startActivity(new Intent(GoogleMaps.this, VoidAuthActivity.class).putExtra("trensID", transId));
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SettingsViewModel::class.java)
    }

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.txt_logout -> {
                showLogoutPrompt()
            }
            R.id.prof_update_linear -> {
                val fragment = ProfileUpdateFragment()
                addFragment(fragment, bundle, Fields.Profile)
            }
            R.id.prof_product_linear -> {
                val fragment = ProfileProductListFragment()
                addFragment(fragment, bundle, Fields.Profile)
            }
            R.id.prof_contact_us_linear -> {
                startActivity(Intent(getActivity(), ContactActivity::class.java))
            }
            R.id.prof_refer_linear -> {
                showReferralPrompt()
            }
            R.id.upgrade_btn -> {
                showUpgradePrompt()
            }
        }
    }

    private fun showReferralPrompt() {
        lateinit var mAlertDialog: AlertDialog

        val inflater = getActivity()!!.layoutInflater
        val alertLayout: View = inflater.inflate(R.layout.alert_referal_main, null)
        val positiveBtn = alertLayout.findViewById<View>(R.id.referal_txt) as TextView
        val negativeBtn = alertLayout.findViewById<View>(R.id.alert_close_button) as ImageView
        val mBuilder = context.let {
            AlertDialog.Builder(it)
                .setView(alertLayout)
        }

        positiveBtn.setOnClickListener {
            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data =
                Uri.parse("https://gomobi.io/referral/?mid=${getLoginResponse().motoMid}")
            startActivity(openURL)
            mAlertDialog.dismiss()
        }

        negativeBtn.setOnClickListener {
            mAlertDialog.dismiss()
        }

        mAlertDialog = mBuilder.show()
        mAlertDialog.setCancelable(false)
        mAlertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        mAlertDialog.window?.setBackgroundDrawable(ColorDrawable(this.resources.getColor(android.R.color.transparent)))
        val dialogWindow = mAlertDialog.window
        dialogWindow?.setGravity(Gravity.CENTER)
    }

}
