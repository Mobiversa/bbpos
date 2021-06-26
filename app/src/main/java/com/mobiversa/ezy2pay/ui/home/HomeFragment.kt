package com.mobiversa.ezy2pay.ui.home

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobiversa.ezy2pay.MainActivity
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.adapter.ProductListAdapter
import com.mobiversa.ezy2pay.base.BaseFragment
import com.mobiversa.ezy2pay.network.ApiService
import com.mobiversa.ezy2pay.network.response.UserValidation
import com.mobiversa.ezy2pay.ui.ezyCash.EzyCashViewModel
import com.mobiversa.ezy2pay.ui.ezyMoto.EzyMotoFragment
import com.mobiversa.ezy2pay.ui.ezyMoto.EzyMotoViewModel
import com.mobiversa.ezy2pay.ui.ezyWire.EzyWireActivity
import com.mobiversa.ezy2pay.ui.qrCode.QRFragment
import com.mobiversa.ezy2pay.ui.receipt.PrintReceiptFragment
import com.mobiversa.ezy2pay.utils.Constants
import com.mobiversa.ezy2pay.utils.Constants.Companion.Boost
import com.mobiversa.ezy2pay.utils.Constants.Companion.EzyAuth
import com.mobiversa.ezy2pay.utils.Constants.Companion.EzyMoto
import com.mobiversa.ezy2pay.utils.Constants.Companion.EzyRec
import com.mobiversa.ezy2pay.utils.Constants.Companion.EzySplit
import com.mobiversa.ezy2pay.utils.Constants.Companion.Ezywire
import com.mobiversa.ezy2pay.utils.Constants.Companion.GrabPay
import com.mobiversa.ezy2pay.utils.Constants.Companion.MobiCash
import com.mobiversa.ezy2pay.utils.Constants.Companion.MobiPass
import com.mobiversa.ezy2pay.utils.Constants.Companion.PREMOTO
import com.mobiversa.ezy2pay.utils.Constants.Companion.PREWIRE
import com.mobiversa.ezy2pay.utils.Fields
import com.mobiversa.ezy2pay.utils.Fields.Companion.CASH_TXN
import com.mobiversa.ezy2pay.utils.PreferenceHelper
import com.mobiversa.ezy2pay.utils.PreferenceHelper.get
import com.mobiversa.ezy2pay.utils.PreferenceHelper.set
import kotlinx.android.synthetic.main.fragment_home.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern


class HomeFragment : BaseFragment(), View.OnClickListener {

    lateinit var amtEdt: EditText
    lateinit var invoiceEdt: EditText

    private lateinit var homeViewModel: EzyCashViewModel
    private lateinit var productListAdapter: ProductListAdapter
    private lateinit var viewModel: EzyMotoViewModel
    private lateinit var prefs: SharedPreferences
    private lateinit var customPrefs: SharedPreferences
    private val apiService = ApiService.serviceRequest()

    var bundle = Bundle()
    lateinit var mAlertDialog: AlertDialog
    private var validateStr: String = ""
    private var invalidDescStr: String = ""

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

//        Log.e("REG_DATA",  ""+getRegisterUserDetail())

        setTitle("Home", true)



        amtEdt = root.edt_home_amount
        invoiceEdt = root.invoice_edt

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

        val productList = getProductList()

        root.rcy_home_product.layoutManager =
            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        productListAdapter = ProductListAdapter(productList, context!!, this)
        root.rcy_home_product.adapter = productListAdapter

        checkAndRequestPermissions()
        if (checkAndRequestPermissions())
            if (isGPSEnabled())
                getLocation()

        if (getLoginResponse().type.equals("LITE", true)) {
            getBalance()
        } else {

        }

        return root
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).supportActionBar?.show()
        setTitle("Home", true)
    }

    fun productDetails(productName: String) {

        val validateaMap = HashMap<String, String>()
        validateaMap[Fields.Service] = Fields.VALIDATE_TERMINAL
        validateaMap[Fields.username] = customPrefs[Constants.UserName]!!
        when (productName) {
            Ezywire -> {
                validateaMap[Fields.tid] = getLoginResponse().tid
                jsonValidate(validateaMap, productName)
//                sendPayment(productName)
            }
            EzyMoto -> {
                validateaMap[Fields.tid] = getLoginResponse().motoTid
                jsonValidate(validateaMap, productName)
            }
            EzyRec -> {
                validateaMap[Fields.tid] = getLoginResponse().ezyrecTid
                jsonValidate(validateaMap, productName)
//                sendPayment(productName)
            }
            EzySplit -> {
                validateaMap[Fields.tid] = getLoginResponse().ezysplitTid
                jsonValidate(validateaMap, productName)
            }
            EzyAuth -> {
                if (checkAndRequestPermissions())
                    showAuthPrompt()
            }
            PREMOTO -> {
                validateaMap[Fields.tid] = getLoginResponse().motoTid
                jsonValidate(validateaMap, productName)
            }
            PREWIRE -> {
                validateaMap[Fields.tid] = getLoginResponse().tid
                jsonValidate(validateaMap, productName)
            }
            GrabPay, MobiCash, MobiPass, Boost -> {
                sendPayment(productName)
            }
        }


    }


    fun sendPayment(productName: String) {

        val amount = amtEdt.text.toString()
        val totalPrice = amount.toDouble()

        when(productName){
            GrabPay, MobiCash, MobiPass, Boost -> {
                if (totalPrice < 0.10) {
                    shortToast("Enter Amount more than 10 cents")
                    return
                }
            }
            else -> {
                if (totalPrice < 5) {
                    shortToast("Enter Amount more than 5 RM")
                    return
                }
            }
        }



        if (validateStr.equals("SUSPENDED")) {
            shortToast(invalidDescStr)
            return
        }

        if (!checkAndRequestPermissions()) {
            shortToast("Enable Location Permission From Settings")
            return
        } else if (!isLocationEnabled(this.context!!)) {
            isGPSEnabled()
            shortToast("Enable GPS to Start")
            return
        }

        ((getActivity() as MainActivity).getLocation())

        when (productName) {
            Ezywire -> {
                if (checkAndRequestPermissions()) {
                    val intent = Intent(getActivity(), EzyWireActivity::class.java)
                    intent.putExtra(Fields.Service, Fields.START_PAY)
                    intent.putExtra(Fields.Amount, amount)
                    intent.putExtra(Fields.InvoiceId, invoiceEdt.text.toString())
                    context?.startActivity(intent)
                    amtEdt.setText("0.00")
                    invoiceEdt.setText("")
                }
            }
            EzyMoto -> {

                if (getLoginResponse().type.equals("LITE", true)) {

                    val balance = prefs.getString(Constants.Balance, "0")?.toFloat()
                    val currentBalance = balance?.minus(amount.toFloat())

                    if (currentBalance != null) {
                        when {
                            currentBalance < 0 -> {
                                showUpgradePrompt(amount)
                            }
                            currentBalance < 100 && currentBalance > 0 -> {
                                showUpgradePrompt(amount)
                            }
                            else -> {
                                navigateMoto(amount)
                            }
                        }
                    }


                } else {
                    navigateMoto(amount)
                }

            }
            EzyRec -> {
                val fragment = EzyMotoFragment()
                bundle.clear()
                bundle.putString(Fields.Service, Fields.EzyRec)
                bundle.putString(Fields.Amount, amount)
                bundle.putString(Fields.InvoiceId, invoiceEdt.text.toString())
                addFragment(fragment, bundle, Fields.EzyRec)
                amtEdt.setText("0.00")
                invoiceEdt.setText("")
            }
            EzySplit -> {
                val fragment = EzyMotoFragment()
                bundle.clear()
                bundle.putString(Fields.Service, Fields.EzySplit)
                bundle.putString(Fields.Amount, amount)
                bundle.putString(Fields.InvoiceId, invoiceEdt.text.toString())
                addFragment(fragment, bundle, Fields.EzySplit)
                amtEdt.setText("0.00")
                invoiceEdt.setText("")
            }
            PREWIRE -> {
                if (checkAndRequestPermissions()) {
                    val intent = Intent(getActivity(), EzyWireActivity::class.java)
                    intent.putExtra(Fields.Service, Fields.PRE_AUTH)
                    intent.putExtra(Fields.Amount, amtEdt.text.toString())
                    intent.putExtra(Fields.InvoiceId, invoiceEdt.text.toString())
                    context?.startActivity(intent)
                    amtEdt.setText("0.00")
                    invoiceEdt.setText("")
                }
            }
            PREMOTO -> {
                val fragment = EzyMotoFragment()
                bundle.clear()
                bundle.putString(Fields.Service, Fields.PreAuthMoto)
                bundle.putString(Fields.Amount, amtEdt.text.toString())
                bundle.putString(Fields.InvoiceId, invoiceEdt.text.toString())
                addFragment(fragment, bundle, Fields.EzyMoto)
                amtEdt.setText("0.00")
                invoiceEdt.setText("")
            }
            Boost -> {
                val fragment = QRFragment()
                bundle.clear()
                bundle.putString(Fields.Service, Fields.BoostQR)
                bundle.putString(Fields.Amount, amount)
                bundle.putString(Fields.InvoiceId, invoiceEdt.text.toString())
                addFragment(fragment, bundle, Boost)
                amtEdt.setText("0.00")
                invoiceEdt.setText("")
            }
            GrabPay -> {
                val fragment = QRFragment()
                bundle.clear()
                bundle.putString(Fields.Service, Fields.GPayQR)
                bundle.putString(Fields.Amount, amount)
                bundle.putString(Fields.InvoiceId, invoiceEdt.text.toString())
                addFragment(fragment, bundle, GrabPay)
                amtEdt.setText("0.00")
                invoiceEdt.setText("")
            }
            MobiPass -> {
                val fragment = QRFragment()
                bundle.clear()
                bundle.putString(Fields.Service, Fields.MobiPassQR)
                bundle.putString(Fields.Amount, amount)
                bundle.putString(Fields.InvoiceId, invoiceEdt.text.toString())
                addFragment(fragment, bundle, MobiPass)
                amtEdt.setText("0.00")
                invoiceEdt.setText("")
            }
            MobiCash -> {
                getEzyCashTrx(amount)
                amtEdt.setText("0.00")
                invoiceEdt.setText("")

//                val fragment = EzyCashFragment()
//                bundle.clear()
//                bundle.putString(Fields.Service, CASH_TXN)
//                bundle.putString(Fields.Amount, amount)
//                bundle.putString(Fields.InvoiceId, invoiceEdt.text.toString())
//                addFragment(fragment, bundle, MobiCash)
            }
        }
    }


    fun navigateMoto(amount: String) {
        val fragment = EzyMotoFragment()
        bundle.clear()
        if (getLoginResponse().type.equals("LITE", true)) {
            bundle.putString(Fields.Service, Fields.EzyMotoLite)
        } else {
            bundle.putString(Fields.Service, Fields.EzyMoto)
        }
        bundle.putString(Fields.Amount, amount)
        bundle.putString(Fields.InvoiceId, invoiceEdt.text.toString())
        addFragment(fragment, bundle, Fields.EzyMoto)
        amtEdt.setText("0.00")
        invoiceEdt.setText("")
    }

    private fun showAuthPrompt() {
        lateinit var mAlertDialog: AlertDialog

        val inflater = getActivity()!!.layoutInflater
        val alertLayout: View = inflater.inflate(R.layout.alert_ezyauth, null)
        val digitalImg = alertLayout.findViewById<View>(R.id.ezydigital_img) as ImageView
        val ezywireImg = alertLayout.findViewById<View>(R.id.ezywire_img) as ImageView
        val alert: AlertDialog.Builder =
            AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme)
        alert.setView(alertLayout)
        alert.setCancelable(true)

        if (getProductList()[1].isEnable)
            ezywireImg.setImageResource(R.drawable.ezyauth)
        else
            ezywireImg.setImageResource(R.drawable.auth_wire_disable)

        if (getLoginResponse().auth3DS.equals("Yes", true)) {
            if (getProductList()[0].isEnable)
                digitalImg.setImageResource(R.drawable.auth_link_enable)
            else
                digitalImg.setImageResource(R.drawable.auth_link_disable)
        } else {
            if (getProductList()[0].isEnable)
                digitalImg.setImageResource(R.drawable.auth_digital_enable)
            else
                digitalImg.setImageResource(R.drawable.auth_digital_disable)
        }

        ezywireImg.setOnClickListener {
            if (getProductList()[1].isEnable) {
                productDetails(PREWIRE)
            } else {
                shortToast("You are not Subscribed for ${getProductList()[1].productName}")
            }
            mAlertDialog.dismiss()

        }

        digitalImg.setOnClickListener {
            if (getProductList()[0].isEnable) {
                productDetails(PREMOTO)
            } else {
                shortToast("You are not Subscribed for ${getProductList()[0].productName}")
            }
            mAlertDialog.dismiss()
        }

        mAlertDialog = alert.create()
        mAlertDialog.show()
    }

    private fun showUpgradePrompt(amount: String) {

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
        val alert: AlertDialog.Builder =
            AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme)
        alert.setView(alertLayout)
        alert.setCancelable(true)

        val balance = prefs.getString(Constants.Balance, "0")?.toFloat()
        val currentBalance = balance?.minus(amount.toFloat())

        val liteUpdate = prefs.getString(Constants.UpgradeStatus, "UPGRADE")?.toString()

        if (liteUpdate.equals(Constants.processing)) {
            upgrade_txt.isEnabled = false
            upgrade_txt.text = "Processing"
        } else {
            upgrade_txt.isEnabled = true
            upgrade_txt.text = "Upgrade"
        }

//        val percent = (currentBalance?.div(500.00))?.times(100)

        if (currentBalance != null) {
            when {
                currentBalance < 0 -> {
                    updateImg.setImageResource(R.drawable.ic_100_completed)
                    desc_txt.text = "You have reached the Maximum Limit!\n" +
                            "Upgrade to increase your Limit!"
                }
                currentBalance < 100 && currentBalance > 0 -> {
                    updateImg.setImageResource(R.drawable.ic_80_complete)
                    desc_txt.text = "You have reached 80% of your limit!"
                }
                else -> {
                    updateImg.setImageResource(R.drawable.ic_upgrade)
                    desc_txt.text = "Hurry..! Upgrade your membership"
                }
            }
        }


//

        upgrade_txt.setOnClickListener {
            if (upgrade_txt.text.toString().equals("Upgrade")) {
                upgrade_txt.text = "Yes, I'm Sure"
                updateImg.setImageDrawable(resources.getDrawable(R.drawable.ic_upgrade))
                desc_txt.text =
                    "You will need to submit documents like Business Registration, Bank statement and a couple more to upgrade and increase the daily limit.\n Are you sure you want to Upgrade?"
            } else {

                upgradeUser(currentBalance, amount)

            }
        }

        cancel_txt.setOnClickListener {

            if (currentBalance != null) {
                when {
                    currentBalance < 0 -> {
                    }
                    currentBalance > 0 -> {
                        navigateMoto(amount)
                    }
                }
            }

            mAlertDialog.dismiss()
        }

        mAlertDialog = mBuilder.show()
        mAlertDialog.setCancelable(false)
        mAlertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        mAlertDialog.window?.setBackgroundDrawable(ColorDrawable(this.resources.getColor(android.R.color.transparent)))
        val dialogWindow = mAlertDialog.window
        dialogWindow?.setGravity(Gravity.CENTER)
    }

    private fun getEzyCashTrx(totalAmount: String) {
        showDialog("Loading...")
        val paymentParams = HashMap<String, String>()
        paymentParams[Fields.Service] = CASH_TXN
        paymentParams[Fields.sessionId] = getLoginResponse().sessionId
        paymentParams[Fields.MerchantId] = getLoginResponse().merchantId
        when {
            getLoginResponse().tid.isNotEmpty() -> {
                paymentParams[Fields.tid] = getLoginResponse().tid
            }
            getLoginResponse().motoTid.isNotEmpty() -> {
                paymentParams[Fields.tid] = getLoginResponse().motoTid
            }
            getLoginResponse().ezypassTid.isNotEmpty() -> {
                paymentParams[Fields.tid] = getLoginResponse().ezypassTid
            }
            getLoginResponse().ezyrecTid.isNotEmpty() -> {
                paymentParams[Fields.tid] = getLoginResponse().ezyrecTid
            }
            getLoginResponse().gpayTid.isNotEmpty() -> {
                paymentParams[Fields.tid] = getLoginResponse().gpayTid
            }
        }
        paymentParams[Fields.Amount] = getAmount(totalAmount)
        paymentParams[Fields.AdditionalAmount] = getAmount("00")
        paymentParams[Fields.Latitude] = Constants.latitudeStr
        paymentParams[Fields.Longitude] = Constants.longitudeStr
        paymentParams[Fields.Location] = if (Pattern.matches(
                ".*[a-zA-Z]+.*[a-zA-Z]",
                Constants.countryStr
            )
        ) Constants.countryStr else ""
        paymentParams[Fields.InvoiceId] = invoiceEdt.text.toString()

        homeViewModel.requestCallAck(paymentParams)
        homeViewModel.callAckData.observe(this, androidx.lifecycle.Observer {
            if (it.responseCode.equals("0000", true)) {
                cancelDialog()
                val trxId = it.responseData.trxId
                val printReceiptFragment = PrintReceiptFragment()
                val bundle = Bundle()
                bundle.putString(Fields.Service, Fields.CASH_RECEIPT)
                bundle.putString(Fields.trxId, trxId)
                bundle.putString(Fields.Amount, amtEdt.text.toString())
                bundle.putString(Constants.ActivityName, Constants.MainAct)
                bundle.putString(Constants.Redirect, Constants.Home)
                addFragment(printReceiptFragment, bundle, "HistoryDetail")

            }
        })
    }

    //Login User
    private fun jsonValidate(validateMap: HashMap<String, String>, productName: String) {
        apiService.validateUser(validateMap).enqueue(object : Callback<UserValidation> {
            override fun onFailure(call: Call<UserValidation>, t: Throwable) {
                showLog("LoginModel Fail", "" + t.message)
                shortToast(t.message!!)
            }

            override fun onResponse(
                call: Call<UserValidation>,
                response: Response<UserValidation>
            ) {
                if (response.isSuccessful) {
                    if (response.body()?.responseData?.status.equals("ACTIVE")) {
                        sendPayment(productName)
                    } else {
                        if (response.body()!!.responseData.description != null) {
                            getActivity()?.let {
                                showDialog(
                                    "WARNING", response.body()!!.responseData.description!!,
                                    it
                                )
                            }
                        } else {
                            getActivity()?.let {
                                showDialog(
                                    "WARNING", "Your account has been suspended .\n" +
                                            "Please contact csmobi@gomobi.io for any assistance.",
                                    it
                                )
                            }
                        }
                    }
                    invalidDescStr = response.body()!!.responseDescription
                } else {
                    showLog(" Fail", "" + response.message())
                    shortToast(response.message())
                }
            }
        })
    }

    private fun getBalance() {
        showDialog("Loading...")
        val paymentParams = HashMap<String, String>()
        paymentParams[Fields.liteMid] = getLoginResponse().liteMid
        paymentParams[Fields.Service] = Fields.LITE_DTL
        paymentParams[Fields.txnAmount] = "0"
        viewModel.ezyMotoPaymentCheck(paymentParams)
        viewModel.ezyPayment.observe(viewLifecycleOwner, Observer {
            cancelDialog()
            if (it.responseCode.equals("0000", true)) {
                prefs[Constants.Balance] = it.responseData.dtlLimit
            } else {
                shortToast(it.responseDescription)
            }

        })
    }

    private fun upgradeUser(currentBalance: Float?, amount: String) {
        showDialog("Loading...")
        val paymentParams = HashMap<String, String>()
        paymentParams[Fields.merchantId] = getLoginResponse().merchantId
        paymentParams[Fields.Service] = Fields.LITE_MERCHANT_UPGRADE
        paymentParams[Fields.email] = getSharedString(Constants.UserName)
        viewModel.upgradeEzymoto(paymentParams)
        viewModel.upgradeData.observe(viewLifecycleOwner, Observer {
            cancelDialog()
            if (it.responseCode.equals("0000")) {
                prefs[Constants.UpgradeStatus] = "PROCESSING"
                if (currentBalance != null) {
                    when {
                        currentBalance < 0 -> {
                        }
                        currentBalance > 0 -> {
                            navigateMoto(amount)
                        }
                    }
                }
                getLoginResponse().liteUpdate = "PROCESSING"
                shortToast(it.responseDescription)
                showLog("Status ", getLoginResponse().liteUpdate)
            } else {
                shortToast(it.responseDescription)
            }

        })
        mAlertDialog.dismiss()
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