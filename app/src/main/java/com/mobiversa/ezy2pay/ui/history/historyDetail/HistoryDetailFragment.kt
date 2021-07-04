package com.mobiversa.ezy2pay.ui.history.historyDetail

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.view.menu.MenuBuilder
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.mobiversa.ezy2pay.MainActivity
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.base.BaseFragment
import com.mobiversa.ezy2pay.network.response.ForSettlement
import com.mobiversa.ezy2pay.ui.history.HistoryViewModel
import com.mobiversa.ezy2pay.ui.receipt.PrintReceiptFragment
import com.mobiversa.ezy2pay.ui.receipt.PrinterActivity
import com.mobiversa.ezy2pay.utils.Constants
import com.mobiversa.ezy2pay.utils.Constants.Companion.MainAct
import com.mobiversa.ezy2pay.utils.Fields
import com.mobiversa.ezy2pay.utils.Fields.Companion.BOOST_VOID
import com.mobiversa.ezy2pay.utils.Fields.Companion.CASH
import com.mobiversa.ezy2pay.utils.Fields.Companion.CASH_CANCEL
import com.mobiversa.ezy2pay.utils.Fields.Companion.GPAY_REFUND
import com.mobiversa.ezy2pay.utils.Fields.Companion.VALIDATE_VOID
import com.mobiversa.ezy2pay.utils.Fields.Companion.VOID
import de.adorsys.android.finger.Finger
import de.adorsys.android.finger.FingerListener
import kotlinx.android.synthetic.main.history_detail_fragment.view.*
import java.util.regex.Pattern


@Suppress("DEPRECATION")
class HistoryDetailFragment : BaseFragment(), View.OnClickListener {

    private var historyData: ForSettlement? = null

    private var latVal = 3.1412
    private var longVal = 101.68653
    private var mapTitle = "Mobiversa"
    private var histTrxType = ""
    private var amount = ""
    private var currentAmount = 0.00

    private var percentAmount = 0.0

    lateinit var mAlertDialog: AlertDialog
    private var printReceiptFragment = PrintReceiptFragment()
    private lateinit var btn_history_detail_receipt: Button
    val requestVal = HashMap<String, String>()

    companion object {
        private var mMap: GoogleMap? = null
        private var mapFragment: SupportMapFragment? = null
    }

    private lateinit var viewModel: HistoryViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.history_detail_fragment, container, false)
        viewModel = ViewModelProviders.of(this).get(HistoryViewModel::class.java)

        initialize(rootView)
        setHasOptionsMenu(true)

        return rootView
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun initialize(rootView: View) {
        setTitle("Transactions", false)
        setHasOptionsMenu(true)
        (activity as MainActivity).supportActionBar?.title = "Transactions"
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        historyData = arguments!!.getSerializable("History") as ForSettlement?
        amount = arguments!!.getString(Constants.Amount).toString()
        val date = arguments!!.getString(Constants.Date)
        histTrxType = arguments!!.getString(Fields.TRX_TYPE).toString()


        val amtStr = amount.replace("RM ", "")
        currentAmount = amtStr.replace(",", "").toDouble()

        percentAmount = (currentAmount * 20.0f) / 100

        showLog("PercentAmount ", currentAmount.toString())
        showLog("PercentAmount ", percentAmount.toString())

        rootView.btn_history_detail_receipt.setOnClickListener(this)
        rootView.btn_history_detail_void.setOnClickListener(this)

        btn_history_detail_receipt = rootView.btn_history_detail_receipt
        rootView.txt_amount_history.text = amount
        rootView.txt_date_history.text = date
        rootView.prod_name_txt.text = historyData?.txnType ?: ""
        rootView.txt_rrn_history.text = "${historyData?.rrn}"
        rootView.txt_status_history.text = "Completed"
        rootView.txt_stan_history.text = "${historyData?.stan}"
        rootView.txt_authcode_history.text = "${historyData?.aidResponse}"
        rootView.txt_invoice_history.text = " ${historyData?.invoiceId}"

        mapTitle = " $amount, $date"

        if (historyData?.txnType.equals("FPX")){
            rootView.txt_stan_history.visibility = View.GONE
            Glide.with(this.context!!)
                .load(historyData?.latitude) // image url
                .into(rootView.history_logo_img)  // imageview object

        }else{
            if (historyData?.latitude?.length ?: 0 > 0) {
                latVal = historyData?.latitude?.toDouble() ?: 3.1412
                longVal = historyData?.longitude?.toDouble() ?: 101.68653
            }
        }

        if (historyData?.txnType.equals(CASH, true)) {
            rootView.txt_rrn_history.visibility = View.GONE
            rootView.txt_authcode_history.visibility = View.GONE
        } else if (historyData?.txnType.equals(Fields.GRABPAY, true)) {
            rootView.txt_authcode_history.visibility = View.GONE
        }

        if (historyData?.cardType != null) {
            when {
                historyData!!.cardType?.toLowerCase().equals("master", true) -> {
                    rootView.history_logo_img.setBackgroundResource(R.drawable.master)
                }
                historyData!!.cardType?.toLowerCase().equals("visa", true) -> {
                    rootView.history_logo_img.setBackgroundResource(R.drawable.visaa)
                }
                historyData!!.cardType?.toLowerCase().equals("unionpay", true) -> {
                    rootView.history_logo_img.setBackgroundResource(R.drawable.union_pay_logo)
                }
            }
        }

        if (historyData?.txnType != null) {
            when {
                historyData!!.txnType.equals(CASH, true) -> {
                    rootView.txt_rrn_history.visibility = View.GONE
                    rootView.btn_history_detail_void.visibility = View.VISIBLE
                    rootView.txt_authcode_history.visibility = View.GONE
                    rootView.history_logo_img.setBackgroundResource(R.drawable.cash_list_icon)
                    rootView.btn_history_detail_void.text = "Cancel"
                }
                historyData!!.txnType.equals(Fields.GRABPAY, true) -> {
                    rootView.history_logo_img.setBackgroundResource(R.drawable.grab_pay_list)
                }
                historyData!!.txnType.equals(Fields.BOOST, true) -> {
                    rootView.history_logo_img.setBackgroundResource(R.drawable.boost_red_list_icon)
                }
            }
        }
        rootView.btn_history_detail_receipt.visibility = View.VISIBLE
        if (histTrxType.equals(Fields.PREAUTH, true)) {
            rootView.btn_history_detail_receipt.text = "Convert to Sale"
        }else if (historyData?.txnType.equals("FPX", true)) {
            rootView.btn_history_detail_receipt.visibility = View.GONE
            rootView.btn_history_detail_void.visibility = View.GONE

            rootView.txt_authcode_history.text = "Order ID : ${historyData?.aidResponse}"
            rootView.txt_rrn_history.text = "Transaction ID : ${historyData?.rrn}"
        } else {
            rootView.btn_history_detail_receipt.text = "Receipt"
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(HistoryViewModel::class.java)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_history_detail_receipt -> {
                if (btn_history_detail_receipt.text.toString().equals("Receipt", true)) {
                    startActivity(Intent(context, PrinterActivity::class.java).apply {
                        putExtra(Fields.Service, Fields.TXN_REPRINT)
                        putExtra(Fields.trxId, historyData!!.txnId)
                        putExtra(Fields.Amount, amount)
                    })
                } else {
                    showConvertSaleAlert()
                }
            }
            R.id.btn_history_detail_void -> {
                showPasswordPrompt()
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun showPasswordPrompt() {

        val inflater = getActivity()!!.layoutInflater
        val alertLayout: View = inflater.inflate(R.layout.alert_void_validate, null)
        val etUsername = alertLayout.findViewById<View>(R.id.username_edt_void) as EditText
        val etPassword = alertLayout.findViewById<View>(R.id.password_edt_void) as EditText
        val btnVoid = alertLayout.findViewById<View>(R.id.btn_alert_void) as Button
        val btnCancel = alertLayout.findViewById<View>(R.id.btn_alert_cancel) as Button
        val savedName = getSharedString(Constants.UserName)
        etUsername.setText(savedName)
        etUsername.isEnabled = false
        etPassword.isEnabled = true
        etPassword.isFocusable = true
        etPassword.requestFocus()
        val alert: AlertDialog.Builder = AlertDialog.Builder(getActivity())
        alert.setView(alertLayout)
        alert.setCancelable(false)

        btnVoid.setOnClickListener {
            val textPassword = etPassword.text.toString()
            if (!textPassword.equals("", ignoreCase = true)) {
                val requestVal = HashMap<String, String>()
                requestVal[Fields.Service] = VALIDATE_VOID
                requestVal[Fields.username] = getSharedString(Constants.UserName)
                requestVal[Fields.password] = textPassword

                jsonVoidTransaction(requestVal)
                mAlertDialog.dismiss()
            } else { //                    Toast.makeText(getActivity(), Constants.ENTER_PASSWORD, Toast.LENGTH_SHORT).show();
                shortToast(Constants.ENTER_PASSWORD)
            }
        }

        btnCancel.setOnClickListener {
            mAlertDialog.dismiss()
        }

        mAlertDialog = alert.create()
        mAlertDialog.show()
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    private fun showConvertSaleAlert() {
        lateinit var mAlertDialog: AlertDialog

        val inflater = getActivity()!!.layoutInflater
        val alertLayout: View = inflater.inflate(R.layout.alert_sale_amount, null)
        val confirm_sale_rg = alertLayout.findViewById<View>(R.id.confirm_sale_rg) as RadioGroup
        val preAuthAmountTxt = alertLayout.findViewById<View>(R.id.pre_auth_amount_txt) as TextView
        val newAmtEdt = alertLayout.findViewById<View>(R.id.new_amt_edt) as EditText
        val btnConfirm = alertLayout.findViewById<View>(R.id.button_confirm) as Button
        val btnCancel = alertLayout.findViewById<View>(R.id.button_cancel) as Button
        val alert: AlertDialog.Builder = AlertDialog.Builder(getActivity())
        alert.setView(alertLayout)
        alert.setCancelable(false)

        var isNewAmount = false

        preAuthAmountTxt.text = amount
        newAmtEdt.isEnabled = false

        confirm_sale_rg.setOnCheckedChangeListener { group, checkedId ->
            val radio: RadioButton = alertLayout.findViewById(checkedId)
            when (radio.id) {
                R.id.pre_auth_rb -> {
                    newAmtEdt.isEnabled = false
                    isNewAmount = false
                }
                R.id.new_amt_rb -> {
                    newAmtEdt.isEnabled = true
                    isNewAmount = true
                }
            }
        }
//        newAmtEdt.setRawInputType(Configuration.KEYBOARD_12KEY)
        newAmtEdt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                showLog("Changed", s.toString())

                val sPattern = Pattern.compile("^(\\d{1,3}(\\,\\d{3})*|(\\d+))(\\.\\d{2})")

                if (!sPattern.matcher(s).matches()) {
                    val userInput = "" + s.toString().replace("[^\\d]".toRegex(), "")
                    val cashAmountBuilder =
                        StringBuilder(userInput)
                    while (cashAmountBuilder.length > 3 && cashAmountBuilder[0] == '0') {
                        cashAmountBuilder.deleteCharAt(0)
                    }
                    while (cashAmountBuilder.length < 3) {
                        cashAmountBuilder.insert(0, '0')
                    }
                    cashAmountBuilder.insert(cashAmountBuilder.length - 2, '.')
                    //                    cashAmountBuilder.insert(0, '$');
                    newAmtEdt.setText(cashAmountBuilder.toString())
                    // keeps the cursor always to the right
                    Selection.setSelection(
                        newAmtEdt.text,
                        cashAmountBuilder.toString().length
                    )
                }
            }
        })

        btnConfirm.setOnClickListener {

            val requestVal = HashMap<String, String>()
            requestVal[Fields.Service] = Fields.PRE_AUTH_SALE
            requestVal[Fields.sessionId] = getLoginResponse().sessionId
            requestVal[Fields.trxId] = historyData?.txnId ?: ""
            requestVal[Fields.HostType] = getLoginResponse().hostType
            requestVal[Fields.MerchantId] = getLoginResponse().merchantId
            val newAmount = newAmtEdt.text.toString()

            if (newAmount.toDouble() > (currentAmount + percentAmount)) {
                newAmtEdt.error = "Amount Must not above 20%"
                return@setOnClickListener
            }

            /*if (isNewAmount && getLoginResponse().hostType.equals("U", false)) {
                    if (newAmount.toDouble() > (currentAmount + percentAmount)) {
                        newAmtEdt.error = "Amount Must not above 20%"
                        return@setOnClickListener
                    } else if (newAmount.toDouble() < (currentAmount - percentAmount)) {
                        newAmtEdt.error = "Amount Must not below 20%"
                        return@setOnClickListener
                    }
            }*/

            if (isNewAmount) {

                when {
                    newAmount.isEmpty() -> {
                        newAmtEdt.error = "Enter Valid amount"
                    }
                    newAmount.toFloat() < 0.11 -> {
                        newAmtEdt.error = "Enter Valid amount"
                    }
                    else -> {
                        requestVal[Fields.Amount] = getAmount(newAmount)
                        jsonSetSale(requestVal)
                        mAlertDialog.dismiss()
                    }
                }
            } else {
                requestVal[Fields.Amount] = getAmount(historyData?.amount.toString())
                jsonSetSale(requestVal)
                mAlertDialog.dismiss()
            }
        }

        btnCancel.setOnClickListener {
            mAlertDialog.dismiss()
        }

        mAlertDialog = alert.create()
        mAlertDialog.show()
    }


    private fun jsonTransactionHistory(userValidateParam: HashMap<String, String>) {
        showDialog("Validating...")
        viewModel.getUserVerification(userValidateParam)
        viewModel.userVerification.observe(this, Observer {
            cancelDialog()
            if (it.responseCode.equals("0000", true)) {
                showLog("Void Test", it.responseDescription)
                jsonVoidTransaction(requestVal)
            }else{
                shortToast(it.responseDescription)
            }

        })
    }

    private fun jsonSetSale(saleParam: HashMap<String, String>) {
        showDialog("Validating...")
        viewModel.getUserVerification(saleParam)
        viewModel.userVerification.observe(this, Observer {
            cancelDialog()
            if (it.responseCode.equals("0000", true)) {
                showLog("Void Test", it.responseDescription)
                shortToast(it.responseDescription)
                context!!.startActivity(Intent(getActivity(), MainActivity::class.java))
            } else {
                shortToast(it.responseDescription)
            }
        })
    }

    private fun jsonVoidTransaction(requestVal: HashMap<String, String>) {

        var pathStr = "mobiapr19"
        when {
            historyData?.txnType.equals(Fields.BOOST) -> {
                requestVal[Fields.Service] = BOOST_VOID
                requestVal[Fields.sessionId] = getLoginResponse().sessionId
                requestVal[Fields.tid] = getLoginResponse().tid
                requestVal[Fields.mid] = getLoginResponse().mid!!
                requestVal[Fields.AID] = historyData!!.aidResponse
                requestVal[Fields.trxId] = historyData?.txnId!!
                requestVal[Fields.InvoiceId] = historyData?.invoiceId ?: ""
            }
            historyData?.txnType.equals(Fields.GRABPAY) -> {
                pathStr = "grabpay"
                requestVal[Fields.Service] = GPAY_REFUND
                requestVal[Fields.sessionId] = getLoginResponse().sessionId
                requestVal[Fields.tid] = getLoginResponse().gpayTid
                requestVal[Fields.mid] = getLoginResponse().gpayMid
                requestVal[Fields.Amount] = historyData?.amount ?: "0"
                requestVal[Fields.Rrn] = historyData!!.rrn!!
                requestVal[Fields.AID] = historyData!!.aidResponse
                requestVal[Fields.txnId] = historyData?.txnId!!
                requestVal[Fields.InvoiceId] = historyData?.invoiceId ?: ""
            }
            else -> {
                requestVal[Fields.Service] = VOID
                requestVal[Fields.sessionId] = getLoginResponse().sessionId
                requestVal[Fields.trxId] = historyData?.txnId ?: ""
                requestVal[Fields.HostType] = getLoginResponse().hostType
                requestVal[Fields.MerchantId] = getLoginResponse().merchantId
                requestVal[Fields.tid] = getLoginResponse().tid
            }
        }

        showDialog("Processing...")
        viewModel.setVoidHistory(pathStr, requestVal)
        viewModel.setVoidHistory.observe(this, Observer {
            if (it.responseCode.equals("0000", true)) {
                shortToast(it.responseDescription)
                val bundle = Bundle()
                if (histTrxType.equals(Fields.PREAUTH, false)){
                    bundle.putString(Fields.Service, Fields.PRE_AUTH_RECEIPT)
                    bundle.putString(Fields.trxId, it.responseData.trxId)
                    bundle.putString(Fields.Amount, amount)
                    bundle.putString(Constants.ActivityName, MainAct)
                    addFragment(printReceiptFragment, bundle, "HistoryDetail")
                }
                else if( historyData?.txnType.equals(Fields.GRABPAY)){
                    startActivity(Intent(context,MainActivity::class.java))
                }
                else if( historyData?.txnType.equals(Fields.BOOST)){
                    startActivity(Intent(context,MainActivity::class.java))
                }
                else{
                    bundle.putString(Fields.Service, Fields.RECEIPT)
                    bundle.putString(Fields.trxId, it.responseData.trxId)
                    bundle.putString(Fields.Amount, amount)
                    bundle.putString(Constants.ActivityName, MainAct)
                    addFragment(printReceiptFragment, bundle, "HistoryDetail")
                }

            } else
                shortToast(it.responseDescription)

            cancelDialog()
        })
    }


    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_receipt, menu)
        val item = menu.findItem(R.id.action_receipt)
        item.isVisible = histTrxType.equals(Fields.PREAUTH, true)

        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val activity = activity as? MainActivity
        return when (item.itemId) {
            android.R.id.home -> {
                (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
                setTitle("Transactions", true)
                fragmentManager?.popBackStack()
                true
            }
            R.id.action_receipt -> {

                val bundle = Bundle()
                bundle.putString(Fields.Service, Fields.PRE_AUTH_RECEIPT)
                bundle.putString(Fields.trxId, historyData!!.txnId)
                bundle.putString(Fields.Amount, amount)
                bundle.putString(Constants.ActivityName, MainAct)
                addFragment(printReceiptFragment, bundle, "HistoryDetail")
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).supportActionBar?.title = "Transactions"
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
    }
}
