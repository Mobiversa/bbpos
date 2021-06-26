package com.mobiversa.ezy2pay.ui.receipt

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
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
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.gson.Gson
import com.mobiversa.ezy2pay.MainActivity
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.base.BaseFragment
import com.mobiversa.ezy2pay.network.response.Country
import com.mobiversa.ezy2pay.ui.ezyMoto.EzyMotoViewModel
import com.mobiversa.ezy2pay.ui.ezyWire.EzyWireActivity
import com.mobiversa.ezy2pay.utils.Constants
import com.mobiversa.ezy2pay.utils.Fields
import com.toptoche.searchablespinnerlibrary.SearchableSpinner
import kotlinx.android.synthetic.main.print_receipt_fragment.view.*

class PrintReceiptFragment : BaseFragment(), AdapterView.OnItemSelectedListener,
    View.OnClickListener {

    companion object {
        fun newInstance() = PrintReceiptFragment()
    }

    private lateinit var chkWhatsapp: AppCompatCheckBox
    private lateinit var viewModel: PrintReceiptViewModel
    private lateinit var motoViewModel: EzyMotoViewModel


    private val countryArray = ArrayList<String>()
    private lateinit var countryList: ArrayList<Country>
    private lateinit var countryAdapter: ArrayAdapter<String>
    private lateinit var countrySpinner: SearchableSpinner
    private lateinit var btnSendReceipt: AppCompatTextView

    private lateinit var edtEmailReceipt: EditText
    private lateinit var edtPhoneNumReceipt: EditText
    private lateinit var edtCountryCodeReceipt: EditText
    private lateinit var PrintReceiptLayout: LinearLayout
//    private lateinit var PrintReceipt: ImageView
    private lateinit var printTVTxt: TextView

    private var spinnerPosition = 0

    private var service = ""
    private var trxId = ""
    private var amount = ""
    private var signatureStr = ""


    private var activityName = ""
    private var redirectName = ""
    protected var PRINTReceipt = false
    lateinit var rootView :View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.print_receipt_fragment, container, false)
        viewModel = ViewModelProviders.of(this).get(PrintReceiptViewModel::class.java)
        motoViewModel = ViewModelProviders.of(this).get(EzyMotoViewModel::class.java)

        service = arguments!!.getString(Fields.Service, "")
        trxId = arguments!!.getString(Fields.trxId, "")
        amount = arguments!!.getString(Fields.Amount, "")
        activityName = arguments!!.getString(Constants.ActivityName, "")
        redirectName = arguments!!.getString(Constants.Redirect, "")
        if (service.equals(Fields.TXN_REPRINT, true))
            signatureStr = arguments!!.getString(Fields.Signature, "")

        rootView.amount_txt.text = amount

        initialize(rootView)
//        editTextWatcher()

        val productParams = HashMap<String, String>()
        productParams[Fields.Service] = Fields.CountryList
        jsonCountryList(productParams)

        return rootView
    }

    private fun initialize(rootView: View) {
        countrySpinner = rootView.spinner_country_receipt
        edtEmailReceipt = rootView.edt_email_receipt
        edtPhoneNumReceipt = rootView.edt_phone_num_receipt
        chkWhatsapp = rootView.chk_whatsapp_receipt
        edtCountryCodeReceipt = rootView.edt_country_code_receipt
        btnSendReceipt = rootView.btn_receipt_slip
        PrintReceiptLayout = rootView.PrintReceiptLayout
//        PrintReceipt = rootView.PrintReceipt
        printTVTxt = rootView.printTVTxt
        btnSendReceipt.setOnClickListener(this)
        PrintReceiptLayout.setOnClickListener(this)

        PrintReceiptLayout.setBackgroundResource(R.color.white)
        printTVTxt.setTextColor(resources.getColor(R.color.grey))
        printTVTxt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.print_grey,0,0,0)

        setTitle("PrintReceipt", false)
        if (activityName.equals(Constants.MainAct, true)) {
            (activity as MainActivity).supportActionBar?.title = "PrintReceipt"
            (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        } else {
            (activity as EzyWireActivity).supportActionBar?.title = "PrintReceipt"
            (activity as EzyWireActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)
    }



    private fun editTextWatcher() {

        edtEmailReceipt.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) { // TODO Auto-generated method stub
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { // TODO Auto-generated method stub
            }

            override fun afterTextChanged(s: Editable) {
                if (!edtEmailReceipt.text.toString().equals("", ignoreCase = true)) {
                    edtPhoneNumReceipt.isFocusable = false
                    edtPhoneNumReceipt.isCursorVisible = false
                    edtPhoneNumReceipt.isFocusableInTouchMode = false
                } else {
                    edtPhoneNumReceipt.isFocusable = true
                    edtPhoneNumReceipt.isCursorVisible = true
                    edtPhoneNumReceipt.isFocusableInTouchMode = true
                }
            }
        })

        edtPhoneNumReceipt.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) { // TODO Auto-generated method stub
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { // TODO Auto-generated method stub
            }

            override fun afterTextChanged(s: Editable) {
                if (!edtPhoneNumReceipt.text.toString().equals("", ignoreCase = true)) {
                    edtEmailReceipt.isFocusable = false
                    edtEmailReceipt.isCursorVisible = false
                    edtEmailReceipt.isFocusableInTouchMode = false
                } else {
                    edtEmailReceipt.isFocusable = true
                    edtEmailReceipt.isCursorVisible = true
                    edtEmailReceipt.isFocusableInTouchMode = true
                }
            }
        })

    }

    private fun setUpCountrySpinner() {

        countryAdapter = ArrayAdapter(
            context!!,
            R.layout.support_simple_spinner_dropdown_item,
            countryArray
        )
        countrySpinner.adapter = countryAdapter
        countrySpinner.setTitle("Select Countries")
        countrySpinner.setPositiveButton("Done")
        countrySpinner.setSelection(spinnerPosition)

        countrySpinner.onItemSelectedListener = this
    }

    private fun jsonCountryList(countryParams: HashMap<String, String>) {
        showDialog("Processing...")
        motoViewModel.countryList(countryParams)
        motoViewModel.countryList.observe(viewLifecycleOwner, Observer {

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
                if (activityName.equals(Constants.MainAct, true)) {
                    (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
                } else
                    (activity as EzyWireActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

                fragmentManager?.popBackStack()
            }
            cancelDialog()
        })

    }

    private fun jsonSendReceipt() {
        showDialog("Loading")
        val paymentParams = HashMap<String, String>()
        paymentParams[Fields.Service] = service
        paymentParams[Fields.username] = getSharedString(Constants.UserName)
        paymentParams[Fields.sessionId] = getLoginResponse().sessionId
        paymentParams[Fields.HostType] = getLoginResponse().hostType
        paymentParams[Fields.trxId] = trxId
        if (edtPhoneNumReceipt.text.isEmpty()) {
            paymentParams[Fields.MobileNo] = ""
            paymentParams[Fields.email] = edtEmailReceipt.text.toString()
            paymentParams[Fields.WhatsApp] = getIsWhatsapp(false)
        } else {
            paymentParams[Fields.WhatsApp] = getIsWhatsapp(chkWhatsapp.isChecked)
            paymentParams[Fields.MobileNo] =
                edtCountryCodeReceipt.text.toString() + edtPhoneNumReceipt.text.toString()
            paymentParams[Fields.email] = edtEmailReceipt.text.toString()
        }

        viewModel.getReceipt(paymentParams)
        viewModel.printReceiptData.observe(this, androidx.lifecycle.Observer {
            cancelDialog()
            if (it.responseCode.equals("0000", true)) {
                shortToast(it.responseDescription)

                if (PRINTReceipt){
                    val gson = Gson()
                    val json = gson.toJson(it)
                    context!!.startActivity(Intent(getActivity(),PrinterActivity::class.java).putExtra("receiptData",json))
                }else{
                    if (activityName.equals(Constants.MainAct, true)) {
                        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
                        fragmentManager?.popBackStack()
                    } else {
                        context!!.startActivity(Intent(getActivity(),MainActivity::class.java))
                    }
                }


            }else{
                shortToast(it.responseDescription)
                context!!.startActivity(Intent(getActivity(),MainActivity::class.java))
            }
        })
    }

    private fun getIsWhatsapp(whatsApp: Boolean): String {
        return if (whatsApp) "Yes" else "No"
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(PrintReceiptViewModel::class.java)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    @SuppressLint("SetTextI18n")
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val spinner = parent as Spinner
        when (spinner.id) {

            R.id.spinner_country_receipt -> {
                edtCountryCodeReceipt.setText("" + countryList[position].phoneCode)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_receipt_slip -> {
                val emailStr = edtEmailReceipt.text.toString()
                val phoneStr = edtPhoneNumReceipt.text.toString()
                if (emailStr.isEmpty() && phoneStr.isEmpty()) {
                    shortToast("Please enter Email id or Mobile Number")
                } else if (phoneStr.isEmpty() && !isValidEmail(emailStr)) {
                    shortToast("Please enter Valid Email id")
                }else
                jsonSendReceipt()
            }
            R.id.PrintReceiptLayout -> {
                if (PRINTReceipt) {
                    PRINTReceipt = false
                    PrintReceiptLayout.setBackgroundResource(R.color.lit_grey)
                    printTVTxt.setTextColor(resources.getColor(R.color.grey))
                    printTVTxt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.print_grey,0,0,0)
                } else {
                    PRINTReceipt = true
                    PrintReceiptLayout.setBackgroundResource(R.color.colorPrimary)
                    printTVTxt.setTextColor(resources.getColor(R.color.colorPrimary))
                    printTVTxt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.print_blue,0,0,0)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setTitle("PrintReceipt", false)
        if (activityName.equals(Constants.MainAct, true)) {
            (activity as MainActivity).supportActionBar?.title = "PrintReceipt"
            (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        } else {
            (activity as EzyWireActivity).supportActionBar?.title = "PrintReceipt"
            (activity as EzyWireActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (activityName.equals(Constants.MainAct, true)) {
                    (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    if (redirectName.equals(Constants.History, true))
                        setTitle("Transactions", true)
                    else
                        setTitle("Home", true)
                } else {
                    setTitle("Home", true)
                    (activity as EzyWireActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
                }

                fragmentManager?.popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
