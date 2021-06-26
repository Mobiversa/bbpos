package com.mobiversa.ezy2pay.ui.ezyCash

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.gson.Gson
import com.mobiversa.ezy2pay.MainActivity
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.base.BaseFragment
import com.mobiversa.ezy2pay.network.response.Country
import com.mobiversa.ezy2pay.ui.ezyMoto.EzyMotoViewModel
import com.mobiversa.ezy2pay.ui.receipt.PrintReceiptFragment
import com.mobiversa.ezy2pay.utils.Constants
import com.mobiversa.ezy2pay.utils.Fields
import com.toptoche.searchablespinnerlibrary.SearchableSpinner
import kotlinx.android.synthetic.main.ezy_cash_fragment.view.*
import kotlinx.android.synthetic.main.ezy_moto_fragment.view.amount_txt
import java.util.regex.Pattern

class EzyCashFragment : BaseFragment(), View.OnClickListener, AdapterView.OnItemSelectedListener {

    companion object {
        fun newInstance() = EzyCashFragment()
    }

    private lateinit var viewModel: EzyCashViewModel
    private lateinit var countryViewModel: EzyMotoViewModel

    private lateinit var rootView: View

    private val countryArray = ArrayList<String>()
    private lateinit var countryList: ArrayList<Country>
    private lateinit var countryAdapter: ArrayAdapter<String>
    private lateinit var countrySpinner: SearchableSpinner
    private lateinit var edtCountryCodeEzycash: EditText
    private lateinit var edtEmailEzycash: EditText
    private lateinit var edtPhoneNumEzycash: EditText
    private lateinit var chkWhatsapp: CheckBox

    private var spinnerPosition = 0

    private val paymentParams = HashMap<String, String>()
    var service = ""
    private var totalAmount = ""
    var invoiceId = ""
    var trxId = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.ezy_cash_fragment, container, false)

        viewModel = ViewModelProviders.of(this).get(EzyCashViewModel::class.java)
        countryViewModel = ViewModelProviders.of(this).get(EzyMotoViewModel::class.java)

        service = arguments!!.getString(Fields.Service) as String
        totalAmount = arguments!!.getString(Fields.Amount) as String
        invoiceId = arguments!!.getString(Fields.InvoiceId) as String

        rootView.amount_txt.text = "RM $totalAmount"

        initialize(rootView)
        editTextWatcher()

        if (isGPSEnabled())
            getLocation()

        getEzyCashTrx()

        val productParams = HashMap<String, String>()
        productParams[Fields.Service] = Fields.CountryList
        jsonCountryList(productParams)

        return rootView
    }

    private fun initialize(rootView: View) {

        edtEmailEzycash = rootView.edt_email_ezycash
        edtCountryCodeEzycash = rootView.edt_country_code_ezycash
        edtPhoneNumEzycash = rootView.edt_phone_num_ezycash
        countrySpinner = rootView.spinner_country_ezycash
        edtEmailEzycash = rootView.edt_email_ezycash
        chkWhatsapp = rootView.chk_whatsapp_ezycash

        rootView.btn_submit_ezycash.setOnClickListener(this)

        setHasOptionsMenu(true)
        (activity as MainActivity).supportActionBar?.title = "EZYCASH"
        setTitle("EZYCASH", false)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    private fun jsonCountryList(countryParams: java.util.HashMap<String, String>) {
        showDialog("Processing...")
        countryViewModel.countryList(countryParams)
        countryViewModel.countryList.observe(this, Observer {
            cancelDialog()
            if (it.responseCode.equals("0000", true)) {
                countryArray.clear()
                countryList = it.responseData.country
                for ((index, value) in it.responseData.country.withIndex()) {
                    countryArray.add(value.countryName)
                    if (value.countryName.equals("Malaysia", false))
                        spinnerPosition = index
                }
                val gson = Gson()
                val json = gson.toJson(it.responseData)
                putSharedString(Constants.CountryResponse, json)
                setUpCountrySpinner()

            } else {
                setTitle("Home", true)
                (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
                fragmentManager?.popBackStack()
            }
        })
    }

    private fun setUpCountrySpinner() {

        countryAdapter =
            ArrayAdapter(context!!, R.layout.support_simple_spinner_dropdown_item, countryArray)
        countrySpinner.adapter = countryAdapter
        countrySpinner.setTitle("Select Countries")
        countrySpinner.setPositiveButton("Done")
        countrySpinner.setSelection(spinnerPosition)

        countrySpinner.onItemSelectedListener = this

    }

    private fun editTextWatcher() {

        edtEmailEzycash.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) { // TODO Auto-generated method stub
            }

            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) { // TODO Auto-generated method stub
            }

            override fun afterTextChanged(s: Editable) {
                if (!edtEmailEzycash.text.toString().equals("", ignoreCase = true)) {
                    edtPhoneNumEzycash.isFocusable = false
                    edtPhoneNumEzycash.isCursorVisible = false
                    edtPhoneNumEzycash.isFocusableInTouchMode = false
                } else {
                    edtPhoneNumEzycash.isFocusable = true
                    edtPhoneNumEzycash.isCursorVisible = true
                    edtPhoneNumEzycash.isFocusableInTouchMode = true
                }
            }
        })

        edtPhoneNumEzycash.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) { // TODO Auto-generated method stub
            }

            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) { // TODO Auto-generated method stub
            }

            override fun afterTextChanged(s: Editable) {
                if (!edtPhoneNumEzycash.text.toString().equals("", ignoreCase = true)) {
                    edtEmailEzycash.isFocusable = false
                    edtEmailEzycash.isCursorVisible = false
                    edtEmailEzycash.isFocusableInTouchMode = false
                } else {
                    edtEmailEzycash.isFocusable = true
                    edtEmailEzycash.isCursorVisible = true
                    edtEmailEzycash.isFocusableInTouchMode = true
                }
            }
        })

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(EzyCashViewModel::class.java)
        countryViewModel = ViewModelProviders.of(this).get(EzyMotoViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
        (activity as MainActivity).supportActionBar?.title = "EZYCASH"
        setTitle("EZYCASH", false)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onClick(v: View) {

        when (v.id) {
            R.id.btn_submit_ezycash -> {
                val emailStr = edtEmailEzycash.text.toString()
                val phoneStr = edtPhoneNumEzycash.text.toString()

                if (emailStr.isEmpty() && phoneStr.isEmpty()) {
                    shortToast("Please enter Email id or Mobile Number")
                } else if (phoneStr.isEmpty() && !isValidEmail(emailStr)) {
                    shortToast("Please enter Valid Email id")
                } else {
                    if (trxId.isNotEmpty())
                        jsonSendReceipt()
                    else
                        getEzyCashTrx()
                }
            }
        }
    }

    private fun getEzyCashTrx() {
        paymentParams.clear()
        paymentParams[Fields.Service] = service
        paymentParams[Fields.sessionId] = getLoginResponse().sessionId
        paymentParams[Fields.MerchantId] = getLoginResponse().merchantId
        paymentParams[Fields.tid] = getLoginResponse().tid
        paymentParams[Fields.Amount] = getAmount(totalAmount)
        paymentParams[Fields.AdditionalAmount] = getAmount("00")
        paymentParams[Fields.Latitude] = Constants.latitudeStr
        paymentParams[Fields.Longitude] = Constants.longitudeStr
        paymentParams[Fields.Location] = if(Pattern.matches(".*[a-zA-Z]+.*[a-zA-Z]", Constants.countryStr)) Constants.countryStr else ""
        paymentParams[Fields.InvoiceId] = invoiceId

        viewModel.requestCallAck(paymentParams)
        viewModel.callAckData.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it.responseCode.equals("0000", true)) {
                cancelDialog()
                trxId = it.responseData.trxId
                val printReceiptFragment = PrintReceiptFragment()
                val bundle = Bundle()
                bundle.putString(Fields.Service, Fields.CASH_RECEIPT)
                bundle.putString(Fields.trxId, trxId)
                bundle.putString(Constants.ActivityName, Constants.MainAct)
                addFragment(printReceiptFragment, bundle, "HistoryDetail")

            }
        })
    }


    private fun jsonSendReceipt() {
        paymentParams.clear()
        paymentParams[Fields.Service] = Fields.CASH_RECEIPT
        paymentParams[Fields.username] = getSharedString(Constants.UserName)
        paymentParams[Fields.sessionId] = getLoginResponse().sessionId
        paymentParams[Fields.HostType] = getLoginResponse().hostType
        paymentParams[Fields.trxId] = trxId
        if (edtPhoneNumEzycash.text.isEmpty()) {
            paymentParams[Fields.MobileNo] = ""
            paymentParams[Fields.email] = edtEmailEzycash.text.toString()
            paymentParams[Fields.WhatsApp] = getIsWhatsapp(false)
        } else {
            paymentParams[Fields.WhatsApp] = getIsWhatsapp(chkWhatsapp.isChecked)
            paymentParams[Fields.MobileNo] =
                edtCountryCodeEzycash.text.toString() + edtPhoneNumEzycash.text.toString()
            paymentParams[Fields.email] = ""
        }

        var urlStr = "mobiapr19"
        countryViewModel.ezyMotoRecPayment(urlStr ,paymentParams)
        countryViewModel.ezyMotoRecPayment.observe(this, androidx.lifecycle.Observer {
            cancelDialog()
            if (it.responseCode.equals("0000", true)) {
                shortToast(it.responseDescription)
                (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
                fragmentManager?.popBackStack()
            }
        })
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    @SuppressLint("SetTextI18n")
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val spinner = parent as Spinner
        when (spinner.id) {

            R.id.spinner_country_ezymoto -> {
                edtCountryCodeEzycash.setText("" + countryList[position].phoneCode)
            }
        }
    }

    private fun getIsWhatsapp(whatsApp: Boolean): String {
        return if (whatsApp) "Yes" else "No"
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val activity = activity as? MainActivity
        return when (item.itemId) {
            android.R.id.home -> {
                setTitle("Home", true)
                (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
                fragmentManager?.popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
