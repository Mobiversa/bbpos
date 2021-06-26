package com.mobiversa.ezy2pay.ui.ezyMoto

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.gson.Gson
import com.mobiversa.ezy2pay.MainActivity
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.adapter.PeopleAdapter
import com.mobiversa.ezy2pay.base.BaseFragment
import com.mobiversa.ezy2pay.network.response.ContactPojo
import com.mobiversa.ezy2pay.network.response.Country
import com.mobiversa.ezy2pay.utils.Constants
import com.mobiversa.ezy2pay.utils.Constants.Companion.CountryResponse
import com.mobiversa.ezy2pay.utils.Fields
import com.toptoche.searchablespinnerlibrary.SearchableSpinner
import kotlinx.android.synthetic.main.ezy_moto_fragment.view.*
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.set


class EzyMotoFragment : BaseFragment(), AdapterView.OnItemSelectedListener, View.OnClickListener {

    companion object {
        fun newInstance() = EzyMotoFragment()
    }

    private lateinit var rootView: View
    private lateinit var viewModel: EzyMotoViewModel

    private val countryArray = ArrayList<String>()
    private lateinit var countryList: ArrayList<Country>
    private lateinit var countryAdapter: ArrayAdapter<String>
    private lateinit var countrySpinner: SearchableSpinner
    private lateinit var monthAdapter: ArrayAdapter<String>
    private lateinit var monthSpinner: Spinner

    private lateinit var edtCountryCodeEzymoto: EditText
    private lateinit var edtEmailEzymoto: EditText
    private lateinit var edtPhoneNumEzymoto: AutoCompleteTextView
    private lateinit var sendLinearEzymoto: LinearLayout
    private lateinit var btnSubmitEzymoto: Button
    private lateinit var linearMonthEzyrec: LinearLayout
    private lateinit var chk_multioption_ezymoto: CheckBox
    private lateinit var chkWhatsapp: AppCompatCheckBox

    private lateinit var edtNameEzymoto: EditText
    private lateinit var edtReferenceEzymoto: EditText

    private var spinnerPosition = 0

    private val paymentParams = HashMap<String, String>()

    //Contact Details
    var contactNumList = ArrayList<ContactPojo>()

    var service = ""
    private var totalAmount = ""
    var invoiceId = ""


    lateinit var adapter: PeopleAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.ezy_moto_fragment, container, false)
        viewModel = ViewModelProviders.of(this).get(EzyMotoViewModel::class.java)

        service = arguments!!.getString(Fields.Service) as String
        totalAmount = arguments!!.getString(Fields.Amount) as String
        invoiceId = arguments!!.getString(Fields.InvoiceId) as String

        rootView.amount_txt.text = "RM $totalAmount"
        rootView.edt_reference_ezymoto.setText(invoiceId)

        contactNumList = getContactsIntoArrayList()

        initialize(rootView)
        editTextWatcher()

        val productParams = HashMap<String, String>()
        productParams[Fields.Service] = Fields.CountryList
        jsonCountryList(productParams)
        return rootView
    }

    override fun onResume() {
        super.onResume()
        setUpTitle()
    }

    private fun initialize(rootView: View) {

        edtNameEzymoto = rootView.edt_name_ezymoto
        edtReferenceEzymoto = rootView.edt_reference_ezymoto
        chkWhatsapp = rootView.chk_whatsapp_ezymoto
        chkWhatsapp.visibility = View.VISIBLE
        chkWhatsapp.isChecked = true

        countrySpinner = rootView.spinner_country_ezymoto
        monthSpinner = rootView.spinner_month_ezyrec
        linearMonthEzyrec = rootView.linear_month_ezyrec
        chk_multioption_ezymoto = rootView.chk_multioption_ezymoto
        countrySpinner.onItemSelectedListener = this
        edtCountryCodeEzymoto = rootView.edt_country_code_ezymoto
        edtEmailEzymoto = rootView.edt_email_ezymoto
        edtPhoneNumEzymoto = rootView.edt_phone_num_ezymoto
        sendLinearEzymoto = rootView.send_linear_ezymoto
        btnSubmitEzymoto = rootView.btn_submit_ezymoto
        edtCountryCodeEzymoto.keyListener = null

        sendLinearEzymoto.visibility = View.GONE

        rootView.txt_send_ezymoto.setOnClickListener(this)
        rootView.txt_share_ezymoto.setOnClickListener(this)
        rootView.btn_submit_ezymoto.setOnClickListener(this)

        adapter = PeopleAdapter(context!!, R.layout.activity_main, contactNumList)
        edtPhoneNumEzymoto.setAdapter(adapter)
        edtPhoneNumEzymoto.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, pos, id -> //this is the way to find selected object/item
                val selectedPerson = adapterView.getItemAtPosition(pos) as ContactPojo
            }

        setUpTitle()
    }

    private fun setUpTitle(){
        when(service){
            Fields.EzyMoto, Fields.PreAuthMoto, Fields.EzyMotoLite -> {
                rootView.btn_submit_ezymoto.text = "Submit"
                linearMonthEzyrec.visibility = View.GONE
                if (service.equals(Fields.EzyMoto)) {
                    chk_multioption_ezymoto.visibility = View.VISIBLE
                } else {
                    chk_multioption_ezymoto.visibility = View.GONE
                }

                if (getLoginResponse().auth3DS.equals("Yes", true)) {
                    rootView.ezy_moto_logo_img.setImageResource(R.drawable.ezylink_blue_icon)

                } else
                    rootView.ezy_moto_logo_img.setImageResource(R.drawable.ezymoto_blue_icon)
                (activity as MainActivity).supportActionBar?.title = Constants.EzyMoto
                setTitle(Constants.EzyMoto, false)
                rootView.prod_name_txt.text = Constants.EzyMoto
            }
            Fields.EzyRec -> {
                rootView.btn_submit_ezymoto.text = "Pay"
                chk_multioption_ezymoto.visibility = View.GONE
                linearMonthEzyrec.visibility = View.VISIBLE
                rootView.ezy_moto_logo_img.setImageResource(R.drawable.ezyrec_blue_icon)
                (activity as MainActivity).supportActionBar?.title = "EZYREC"
                setTitle("EZYREC", false)
                rootView.prod_name_txt.text = "EZYREC"
            }
            Fields.EzySplit -> {
                rootView.btn_submit_ezymoto.text = "Pay"
                chk_multioption_ezymoto.visibility = View.GONE
                linearMonthEzyrec.visibility = View.VISIBLE
                rootView.ezy_moto_logo_img.setImageResource(R.drawable.ezysplit_logo)
                (activity as MainActivity).supportActionBar?.title = "EZYSPLIT"
                setTitle("EZYSPLIT", false)
                rootView.prod_name_txt.text = "EZYSPLIT"
                rootView.prod_name_txt.visibility = View.GONE
            }
        }
        setHasOptionsMenu(true)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun jsonCountryList(boostParams: HashMap<String, String>) {
        showDialog("Processing...")
        viewModel.countryList(boostParams)
        viewModel.countryList.observe(viewLifecycleOwner, Observer {
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
                putSharedString(CountryResponse, json)
                setUpCountrySpinner()

            } else {
                setTitle("Home", true)
                (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
                fragmentManager?.popBackStack()
            }
        })
    }

    private fun editTextWatcher() {

        edtEmailEzymoto.addTextChangedListener(object : TextWatcher {
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
                if (!edtEmailEzymoto.text.toString().equals("", ignoreCase = true)) {
                    edtPhoneNumEzymoto.isFocusable = false
                    edtPhoneNumEzymoto.isCursorVisible = false
                    edtPhoneNumEzymoto.isFocusableInTouchMode = false
                } else {
                    edtPhoneNumEzymoto.isFocusable = true
                    edtPhoneNumEzymoto.isCursorVisible = true
                    edtPhoneNumEzymoto.isFocusableInTouchMode = true
                }
            }
        })

        edtPhoneNumEzymoto.addTextChangedListener(object : TextWatcher {
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
                if (!edtPhoneNumEzymoto.text.toString().equals("", ignoreCase = true)) {
                    edtEmailEzymoto.isFocusable = false
                    edtEmailEzymoto.isCursorVisible = false
                    edtEmailEzymoto.isFocusableInTouchMode = false
                } else {
                    edtEmailEzymoto.isFocusable = true
                    edtEmailEzymoto.isCursorVisible = true
                    edtEmailEzymoto.isFocusableInTouchMode = true
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

        monthAdapter = ArrayAdapter(
            context!!,
            R.layout.support_simple_spinner_dropdown_item,
            getMonthList()
        )
        monthSpinner.adapter = monthAdapter
        monthSpinner.onItemSelectedListener = this

    }

    private fun applicationSubmit(clickAction: String) {

        paymentParams.clear()
        paymentParams[Fields.Service] = service
        paymentParams[Fields.sessionId] = getLoginResponse().sessionId
        paymentParams[Fields.AppVersionNumber] = "5.4" //appVersionNum
        paymentParams[Fields.InvoiceId] = edtReferenceEzymoto.text.toString()

        paymentParams[Fields.Amount] = getAmount(totalAmount)


        if (clickAction.equals("Send", true)) {
            if (edtPhoneNumEzymoto.text.isEmpty()) {
                paymentParams[Fields.MobileNo] = ""
                paymentParams[Fields.email] = edtEmailEzymoto.text.toString()
                paymentParams[Fields.WhatsApp] = getIsWhatsapp(false)
            } else {

                var phNum = edtPhoneNumEzymoto.text.toString().replace("+","").replace(" ","")
                var countryCode = edtCountryCodeEzymoto.text.toString().replace("+","")

                if (phNum.startsWith(countryCode)){
                    if (phNum.length>10)
                    phNum = phNum.substring(countryCode.length)
                }
                val num = edtCountryCodeEzymoto.text.toString() + phNum
                if (num.length<9){
                   shortToast("Enter Valid Mobile number")
                    return
                }
                paymentParams[Fields.MobileNo] = num
                paymentParams[Fields.WhatsApp] = getIsWhatsapp(chkWhatsapp.isChecked)
                paymentParams[Fields.email] = ""

                /*paymentParams[Fields.MobileNo] =
                    edtCountryCodeEzymoto.text.toString() + edtPhoneNumEzymoto.text.toString()
                paymentParams[Fields.WhatsApp] = getIsWhatsapp(chkWhatsapp.isChecked)
                paymentParams[Fields.email] = ""*/


            }
        } else {
            paymentParams[Fields.MobileNo] = ""
            paymentParams[Fields.email] = ""
        }

        when (service) {
            Fields.EzyMoto, Fields.PreAuthMoto -> {
                paymentParams[Fields.tid] = getLoginResponse().motoTid
                paymentParams[Fields.mid] = getLoginResponse().motoMid
                paymentParams[Fields.HostType] = getLoginResponse().hostType
                paymentParams[Fields.MultiOption] = getIsWhatsapp(chk_multioption_ezymoto.isChecked)
            }
            Fields.EzyMotoLite -> {
                paymentParams[Fields.txnAmount] = totalAmount
                paymentParams[Fields.tid] = getLoginResponse().motoTid
                paymentParams[Fields.liteMid] = getLoginResponse().liteMid
//                paymentParams[Fields.reqMode] = Constants.app
            }
            Fields.EzyRec -> {
                paymentParams[Fields.tid] = getLoginResponse().ezyrecTid
                paymentParams[Fields.mid] = getLoginResponse().ezyrecMid
                paymentParams[Fields.HostType] = getLoginResponse().hostType
                paymentParams[Fields.Recurring] = "Yes"
                paymentParams[Fields.Period] = "1"
                paymentParams[Fields.InstallmentCount] =
                    getMonthList()[monthSpinner.selectedItemPosition]
            }
            Fields.EzySplit -> {
                paymentParams[Fields.tid] = getLoginResponse().ezysplitTid
                paymentParams[Fields.mid] = getLoginResponse().ezysplitMid
//                paymentParams[Fields.tid] = "10003078"  //For Static values to test
//                paymentParams[Fields.mid] = "000000000008971"
                paymentParams[Fields.HostType] = getLoginResponse().hostType
                paymentParams[Fields.Recurring] = "Yes"
//                paymentParams[Fields.Period] = "1"
                paymentParams[Fields.InstallmentCount] =
                    getMonthList()[monthSpinner.selectedItemPosition]
            }
        }

        paymentParams[Fields.ContactName] = rootView.edt_name_ezymoto.text.toString()

        paymentParams[Fields.Latitude] = Constants.latitudeStr
        paymentParams[Fields.Longitude] = Constants.longitudeStr
        paymentParams[Fields.Location] = if(Pattern.matches(
                ".*[a-zA-Z]+.*[a-zA-Z]",
                Constants.countryStr
            )) Constants.countryStr else ""

        ezyMotoRecPayment(paymentParams, clickAction)

        showLog(service, "" + paymentParams)

    }

    private fun ezyMotoRecPayment(paymentParams: HashMap<String, String>, clickAction: String) {
        var urlStr = ""

        if (getLoginResponse().type.equals("LITE",true)){
            urlStr = "mobilite/jsonservice"
        }else{
            urlStr = "mobiapr19/mobi_jsonservice"
        }

        showLog("Moto", ""+paymentParams)

//        getLoginResponse().
        showDialog("Processing...")
        viewModel.ezyMotoRecPayment(urlStr ,paymentParams)
        viewModel.ezyMotoRecPayment.observe(this, Observer {
            cancelDialog()
            if (it.responseCode.equals("0000", true)) {

                when (clickAction) {
                    "Send" -> {
                        shortToast(it.responseData.invoiceId)
                        context!!.startActivity(Intent(getActivity(), MainActivity::class.java))
//                        setTitle("Home", true)
//                        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
//                        fragmentManager?.popBackStack()

                    }
                    "Share" -> {
                        val share = Intent(Intent.ACTION_SEND)
                        share.type = "text/plain"
                        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                        share.putExtra(Intent.EXTRA_SUBJECT, Constants.EzyMoto)
                        share.putExtra(
                            Intent.EXTRA_TEXT,
                            it.responseData.invoiceId + "\n" + it.responseData.opt
                        )
                        startActivity(Intent.createChooser(share, "Share $Constants.EzyMoto"))

                    }
                }

            } else {
                if (it.responseDescription.equals(
                        "Update your App",
                        ignoreCase = true
                    )
                ) {
                    DialogInterface.OnClickListener { dialog, which ->
                        if (true) { //Check Network
                            val appPackageName =
                                getActivity()!!.packageName // getPackageName() from Context or Activity object
                            try {
                                startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("market://details?id=$appPackageName")
                                    )
                                )
                            } catch (anfe: ActivityNotFoundException) {
                                startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                                    )
                                )
                            }
                        } else {
                            //Here No Network Dialog
                            showLog("Internet", "No Internet")
                        }
                    }
//                    EzywireUtils.showDialog(
//                        getActivity(),
//                        "Error",
//                        "New version of " + "EZYWIRE" + " is available for update, Goto playstore",
//                        "Update",
//                        "",
//                        onclick,
//                        null
//                    )
                } else {
                    shortToast(it.responseDescription)
                }
            }
        })

    }

    private fun getIsWhatsapp(whatsApp: Boolean): String {
        return if (whatsApp) "Yes" else "No"
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(EzyMotoViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    @SuppressLint("SetTextI18n")
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val spinner = parent as Spinner
        when (spinner.id) {

            R.id.spinner_country_ezymoto -> {
                edtCountryCodeEzymoto.setText("" + countryList[position].phoneCode)
            }
            R.id.spinner_month_ezyrec -> {

            }
        }
    }

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.txt_send_ezymoto -> {
                sendLinearEzymoto.visibility = View.VISIBLE
            }
            R.id.txt_share_ezymoto -> {
                sendLinearEzymoto.visibility = View.GONE
                //For EzyMoto
                when (service) {
                    Fields.EzyMoto, Fields.PreAuthMoto, Fields.EzyMotoLite -> {
                        val nameStr = edtNameEzymoto.text.toString()
                        if (nameStr.isEmpty() || nameStr.length < 2) {
                            edtNameEzymoto.error = "Enter Valid Name"
                        } else
                            applicationSubmit("Share")
                    }
                    Fields.EzyRec -> {
                        //For EzyRec
                        val nameStr = edtNameEzymoto.text.toString()
                        if (nameStr.isEmpty() || nameStr.length < 2) {
                            edtNameEzymoto.error = "Enter Valid Name"
                        } else if (monthSpinner.selectedItemPosition > 0) {
                            applicationSubmit("Share")
                        } else {
                            shortToast("Please Select Month")
                        }
                    }
                    Fields.EzySplit -> {
                        //For EzyRec
                        val nameStr = edtNameEzymoto.text.toString()
                        if (nameStr.isEmpty() || nameStr.length < 2) {
                            edtNameEzymoto.error = "Enter Valid Name"
                        } else if (monthSpinner.selectedItemPosition > 0) {
                            applicationSubmit("Share")
                        } else {
                            shortToast("Please Select Month")
                        }
                    }
                }
            }
            R.id.btn_submit_ezymoto -> {
                val nameStr = edtNameEzymoto.text.toString()
                val emailStr = edtEmailEzymoto.text.toString()
                val phoneStr = edtPhoneNumEzymoto.text.toString()

                if (nameStr.isEmpty() || nameStr.length < 2) {
                    edtNameEzymoto.error = "Enter Valid Name"
                } else if (emailStr.isEmpty() && phoneStr.isEmpty()) {
                    shortToast("Please enter Email id or Mobile Number")
                } else if (phoneStr.isEmpty() && !isValidEmail(emailStr)) {
                    shortToast("Please enter Valid Email id")
                } else {

                    when (service) {
                        Fields.EzyMoto,Fields.EzyMotoLite, Fields.PreAuthMoto -> {
                            val nameStr = edtNameEzymoto.text.toString()
                            if (nameStr.isEmpty() || nameStr.length < 2) {
                                edtNameEzymoto.error = "Enter Valid Name"
                            } else
                                applicationSubmit("Send")
                        }
                        Fields.EzyRec -> {
                            if (monthSpinner.selectedItemPosition > 0) {
                                applicationSubmit("Send")
                            } else {
                                shortToast("Please Select Month")
                            }
                        }
                        Fields.EzySplit -> {
                            if (monthSpinner.selectedItemPosition > 0) {
                                applicationSubmit("Send")
                            } else {
                                shortToast("Please Select Month")
                            }
                        }
                    }
                }
            }
        }
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

    private fun getMonthList(): ArrayList<String> {

        if (service.equals(Fields.EzyRec, true)){
            val month = ArrayList<String>()
            month.add("Select Month")
            for (i in 2..36)
                month.add(i.toString())
            return month
        }else{
            val month = ArrayList<String>()
            month.add("Select Month")
            for (i in 3..12 step 3)
                month.add(i.toString())
            return month
        }



    }

    private fun getContactsIntoArrayList() : ArrayList<ContactPojo>{
        var mList: ArrayList<ContactPojo> = ArrayList()
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
        )
        var cursor: Cursor? = null
        try {
            cursor = context!!.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                null
            )
        } catch (e: SecurityException) {
            //SecurityException can be thrown if we don't have the right permissions
        }
        if (cursor != null) {
            try {
                val normalizedNumbersAlreadyFound = HashSet<String>()
                val indexOfNormalizedNumber =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER)
                val indexOfDisplayName =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val indexOfDisplayNumber =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (cursor.moveToNext()) {
                    val normalizedNumber = cursor.getString(indexOfNormalizedNumber)
                    if (normalizedNumbersAlreadyFound.add(normalizedNumber)) {
                        val displayName = cursor.getString(indexOfDisplayName)
                        val displayNumber = cursor.getString(indexOfDisplayNumber)
//                        showLog(
//                            "Contact ",
//                            "GetContactsIntoArrayList: $displayName\t$displayNumber"
//                        )
                        mList.add(ContactPojo(displayName, displayNumber, ""))

                        //haven't seen this number yet: do something with this contact!
                    } else {
                        //don't do anything with this contact because we've already found this number
                    }
                }
            } finally {
                cursor.close()
            }
        }

        showLog("List Count", ""+mList.size)
        return mList
    }

}
