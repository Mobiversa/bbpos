package com.mobiversa.ezy2pay.ui.loginActivity.signup


import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.gson.Gson
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.base.BaseFragment
import com.mobiversa.ezy2pay.network.response.StateModel
import com.mobiversa.ezy2pay.network.response.StateResponseData
import com.mobiversa.ezy2pay.ui.loginActivity.LoginActivity
import com.mobiversa.ezy2pay.ui.loginActivity.LoginViewModel
import com.mobiversa.ezy2pay.utils.Constants
import com.mobiversa.ezy2pay.utils.Fields
import com.mobiversa.ezy2pay.utils.Fields.Companion.fbLogin
import com.toptoche.searchablespinnerlibrary.SearchableSpinner
import kotlinx.android.synthetic.main.fragment_business_detail.*
import kotlinx.android.synthetic.main.fragment_business_detail.view.*
import org.json.JSONException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * A simple [Fragment] subclass.
 */
class BusinessDetailFragment : BaseFragment(), View.OnClickListener, TextWatcher {

    private lateinit var viewModel: LoginViewModel
    lateinit var spinner_business_category: AppCompatSpinner
    var business_category: String? = null
    private var businessArrayList: ArrayList<HashMap<String, String>> = ArrayList()
    private lateinit var businessList: HashMap<String, String>
    private val categorycodes = ArrayList<String>()
    private val categoryName = ArrayList<String>()
    private lateinit var businessAdapter: SimpleAdapter
    private lateinit var edit_companyname: AppCompatEditText
    private lateinit var edit_companynumber: AppCompatEditText
    private lateinit var edit_officestreet: AppCompatEditText
    private lateinit var edit_officecity: SearchableSpinner
    private lateinit var edit_officestate: SearchableSpinner
    private lateinit var edit_officepostalcode: AppCompatEditText
    private lateinit var button_next: AppCompatTextView
    private lateinit var button_prev: AppCompatTextView
    private lateinit var text_currencycode: AppCompatTextView
    private lateinit var text_country: AppCompatTextView
    private lateinit var category_code: String
    private lateinit var category_name: String
    private lateinit var username: String
    private lateinit var FullName: String
    private lateinit var password: String
    private lateinit var fb: String
    private lateinit var fbname: String
    private lateinit var gmail: String
    private lateinit var gmailname: String
    private lateinit var dob: String
    private lateinit var merchantType: String
    private lateinit var currencyCode: String
    private lateinit var country: String
    private lateinit var category_selected: String
    private lateinit var entered_mobile_no: String
    private lateinit var activation_code: String
    private var isBusinessDetailsPresent = false

    private lateinit var stateAdapter: ArrayAdapter<String>
    private lateinit var cityAdapter: ArrayAdapter<String>

    private var stateData = ArrayList<StateResponseData>()
    private var cityData = ArrayList<StateResponseData>()
    private val stateArray = ArrayList<String>()
    private val cityArray = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_business_detail, container, false)
        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)

        val activationCode = getUserString("activation_code")

        showLog("Activation Code", activationCode)

        initializer(rootView)
        loadBusinessSpinner()

        return rootView
    }

    private fun initializer(rootView: View) {

        button_next = rootView.button_next
        button_next.setOnClickListener(this)

        button_prev = rootView.button_prev
        button_prev.setOnClickListener(this)

        if (getUserString(Fields.activationCode).isNotEmpty()) {
            rootView.button_next.text = "Send"
            rootView.button_next.gravity = Gravity.CENTER
        } else {
            rootView.button_next.text = "Next"
            rootView.button_next.gravity = Gravity.END
        }

        text_currencycode = rootView.text_currencycode
        text_country = rootView.text_country
        edit_companyname = rootView.edit_companyname
        edit_companynumber = rootView.edit_companynumber
        edit_officestreet = rootView.edit_officestreet
        edit_officecity = rootView.edit_officecity
        edit_officestate = rootView.edit_officestate
        edit_officepostalcode = rootView.edit_officepostalcode
        spinner_business_category = rootView.spinner_business_category
        edit_companyname.addTextChangedListener(this)
        edit_companynumber.addTextChangedListener(this)
        edit_officestreet.addTextChangedListener(this)
//        edit_officecity.onItemSelectedListener(this)
//        edit_officestate.onItemSelectedListener(this)
//        edit_officecity.addTextChangedListener(this)
//        edit_officestate.addTextChangedListener(this)
        edit_officepostalcode.addTextChangedListener(this)
        rootView.back_sign_up.setOnClickListener(this)

        val map = HashMap<String, String>()
        map[Fields.StateOrCountry] = Fields.State
        map[Fields.CountryID] = "158"
        map[Fields.StateID] = "473"
        jsonSelectState(map)


        spinner_business_category.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                business_category = categoryName[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        //City Selection
        edit_officecity.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        //State Selection
        edit_officestate.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

                showLog("State", parent?.getItemAtPosition(position).toString())

                if (position != 0) {
                    val map = HashMap<String, String>()
                    map[Fields.StateOrCountry] = Fields.City
                    map[Fields.CountryID] = "158"
                    map[Fields.StateID] = stateData[position - 1].Id.toString()
                    jsonSelectCity(map)
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        currencyCode = getUserString(Constants.currencyCode)
        entered_mobile_no = getUserString(Constants.enteredMobilenumber)
        username = getUserString(Constants.UserName)
        FullName = getUserString(Constants.FullName)
        password = getUserString(Fields.password)
        fb = getUserString(Constants.fb)
        fbname = getUserString(Constants.fbname)
        gmail = getUserString(Constants.gmail)
        gmailname = getUserString(Constants.gmailname)
//        dob = getUserString(Constants.dob)
        merchantType = getUserString(Constants.merchantType)
        activation_code = getUserString(Fields.activationCode)
        country = getUserString(Fields.Country)

        setUI()

    }

    private fun setState() {
        stateAdapter = ArrayAdapter(
            context!!,
            R.layout.spinner_item_country,
            stateArray
        )
        edit_officestate.adapter = stateAdapter
        edit_officestate.setTitle("Select State")
        edit_officestate.setPositiveButton("Done")
    }

    private fun selectCity() {
        cityAdapter = ArrayAdapter(
            context!!,
            R.layout.spinner_item_country,
            cityArray
        )
        edit_officecity.adapter = cityAdapter
        edit_officecity.setTitle("Select City")
        edit_officecity.setPositiveButton("Done")
    }

    private fun setUI() {
        text_country.text = country
        text_currencycode.text = currencyCode
        try {
            if (getUserString(Fields.activationCode).isNotEmpty()) {
                isBusinessDetailsPresent = true
                val merchantName = getRegisterUserDetail().merchantName
                val merchantAddr = getRegisterUserDetail().merchantAddr
                val merchantPostCode = getRegisterUserDetail().merchantPostCode
                val merchantCity = getRegisterUserDetail().merchantCity
                val merchantState = getRegisterUserDetail().merchantState
                val officeNo = getRegisterUserDetail().officeNo
                showLog(
                    "testete",
                    merchantAddr + merchantCity + merchantName + merchantPostCode + merchantState + officeNo
                )
                edit_companyname.setText(merchantName)
                edit_companyname.isFocusable = false
                edit_companynumber.setText(officeNo)
                edit_companynumber.isFocusable = false
                edit_officestreet.setText(merchantAddr)
                edit_officestreet.isFocusable = false
                /*edit_officecity.setText(merchantCity)
                edit_officecity.isFocusable = false
                edit_officestate.setText(merchantState)
                edit_officestate.isFocusable = false*/
                edit_officepostalcode.setText(merchantPostCode)
                edit_officepostalcode.isFocusable = false
            } else {
                edit_companynumber.setText(getUserString(Fields.MobileNo))
            }
        } catch (je: JSONException) {
        }
    }

    private fun loadBusinessSpinner() {
        try {
            businessArrayList = ArrayList()
            val countriesList = getRegisterUserDetail().listCategoryData
            for (data in countriesList) {
                category_code = data.categoryCode
                category_name = data.categoryName
                // SEND JSON DATA INTO SPINNER TITLE LIST
                businessList = HashMap()
                businessList[Constants.categoryName] = category_name
                businessList[Constants.categoryCode] = category_code
                businessArrayList!!.add(businessList)
                categorycodes.add(category_code)
                categoryName.add(category_name)
                businessAdapter = SimpleAdapter(
                    getActivity(),
                    businessArrayList,
                    R.layout.country_spinner_item,
                    arrayOf(Constants.categoryCode, Constants.categoryName),
                    intArrayOf(R.id.phone_code, R.id.country_name)
                )
                getActivity()!!.runOnUiThread {
                    spinner_business_category.adapter = businessAdapter
                }
            }
        } catch (JE: JSONException) {
        }
    }

    private fun validateFields() {
        if (!isBusinessDetailsPresent) {

            if (TextUtils.isEmpty(edit_companyname.text.toString())) {
                edit_companyname.error = Constants.ENTER_COMPANY_NAME
            } else if (TextUtils.isEmpty(edit_officestreet.text.toString())) {
                edit_officestreet.error = Constants.ENTER_STREET
            } else if (edit_officecity.selectedItemPosition == 0) {
                shortToast(Constants.ENTER_CITY)
            } else if (edit_officestate.selectedItemPosition == 0) {
                shortToast(Constants.ENTER_STATE)
            } else if (TextUtils.isEmpty(edit_officepostalcode.text.toString())) {
                edit_officepostalcode.error = Constants.ENTER_POSTALCODE
            } else if (!isValidMobile(edit_companynumber.text.toString())) {
                edit_companynumber.error = Constants.PLEASE_ENTER_VALIDMOBILE
            } else {
                if (business_category.equals("Select Category", ignoreCase = true)) {
                    shortToast("Please select your business category")
                } else {
                    postToServer()
                }
            }
        } else {
            if (business_category.equals("Select Category", ignoreCase = true)) {
                shortToast("Please select your business category")
            } else {
                postToServer()
            }
        }
    }

    private fun postToServer() {
        //21 params
        val params: HashMap<String, String> = HashMap()
        params[Fields.Service] = Fields.MERCHANT_REG
        params["facebookId"] = fbname
        params[Fields.businessName] = edit_companyname.text.toString()
        params[Fields.contactName] = FullName
//        params["dob"] = dob
        params[Fields.country] = country
        params[Fields.categoryName] = business_category.toString()
        params[Fields.street] = edit_officestreet.text.toString()
        params["currency"] = currencyCode
        params[Fields.city] = edit_officecity.selectedItem.toString()
        params["fbLogin"] = fbLogin
        params[Fields.postalCode] = edit_officepostalcode.text.toString()
        params[Fields.state] = stateData.get(edit_officestate.selectedItemPosition-1).Id.toString()
        params["mobileNo"] = getUserString(Fields.MobileNo)
        params["googleLogin"] = gmail
        params["officeNo"] = edit_companynumber.text.toString()
        params["merchantType"] = merchantType
        params[Fields.password] = password
        params["googleId"] = gmailname
        params[Fields.username] = username
        if (!activation_code.equals("", ignoreCase = true)) {
            params["activationCode"] = activation_code
        }
        if (getUserString(Fields.activationCode).isNotEmpty()) {
            jsonPostUserDetails(params)
        } else {
            val gson = Gson()
            val json = gson.toJson(params)
            putRegisterString(Constants.BusinessDetailData, json)

            for ((key, value) in params) {
                putUserString(key, value)
            }
            val bundle = Bundle()
            val bankDetailFragment = BankDetailFragment()
            addFragment(bankDetailFragment, bundle, "BankDetail")
        }
    }

    private fun jsonPostUserDetails(detailsParam: HashMap<String, String>) {
        showDialog("processing in...")
        viewModel.registerUser(detailsParam)
        viewModel.successResponse.observe(this, androidx.lifecycle.Observer {
            if (it.responseCode.equals("0000", true)) {

                val address =
                    edit_officestreet.text.toString() + ", " + edit_officecity.selectedItem.toString() + ", " +
                            edit_officepostalcode.text.toString() + ", " + edit_officestate.selectedItem.toString()
                if (activation_code.isEmpty()) {
                    val params = HashMap<String, String>()
                    params[Fields.Service] = Fields.UPGRADE
                    params["Company"] = edit_companyname.text.toString()
                    params["ContactNo"] = getUserString(Fields.MobileNo)
                    params["contactName"] = FullName
                    params["Address"] = address
                    params["Email"] = username
                    params["Website"] = ""
                    jsonConfirmMerchant(params)
                } else {
                    startActivity(Intent(getActivity(), LoginActivity::class.java))
                }
            }
        })
    }

    private fun jsonSelectState(detailsParam: HashMap<String, String>) {
        showDialog("processing in...")
        viewModel.getStateList(detailsParam)
        viewModel.stateResponse.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it.responseCode.equals("0000", true)) {
                cancelDialog()
                stateData = it.responseData
                val data = it.responseData
                stateArray.add("Select State")
                for (state in data) {
                    stateArray.add(state.Name)
                }
                setState()

            } else {
                cancelDialog()
            }
        })
    }

    private fun jsonSelectCity(detailsParam: HashMap<String, String>) {
        showDialog("processing in...")
        viewModel.getStateList(detailsParam)
        viewModel.cityResponse.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it.responseCode.equals("0000", true)) {
                cancelDialog()
                if (detailsParam[Fields.StateOrCountry].equals(Fields.State)) {
                    showLog("State", it.responseDescription)
                    stateArray.clear()
                    stateData = it.responseData
                    val data = it.responseData
                    stateArray.add("Select State")
                    for (state in data) {
                        stateArray.add(state.Name)
                    }
                    setState()
                } else {
                    showLog("City", it.responseDescription)
                    cityData = it.responseData
                    val data = it.responseData
                    cityArray.clear()
                    cityArray.add("Select City")
                    for (state in data) {
                        cityArray.add(state.City)
                    }
                    selectCity()
                }
            } else {
                cancelDialog()
            }
        })
    }

    private fun jsonConfirmMerchant(detailsParam: HashMap<String, String>) {
        showDialog("Processing in...")
        viewModel.confirmMerchant(detailsParam)
        viewModel.confirmMerchantResponse.observe(this, androidx.lifecycle.Observer {
            if (it.responseCode.equals("0000")) {
                startActivity(Intent(getActivity(), LoginActivity::class.java))
                shortToast(it.responseDescription)
            }else{
                shortToast(it.responseDescription)
            }
        })
    }

    private fun isValidMobile(phone: String): Boolean {
        return Patterns.PHONE.matcher(phone).matches()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_next -> {
                validateFields()
            }
            R.id.back_sign_up -> {
                fragmentManager?.popBackStack()
            }
            R.id.button_prev -> {
                fragmentManager?.popBackStack()
            }
        }
    }

    override fun afterTextChanged(editable: Editable?) {
        if (editable === edit_companyname.editableText) {
            edit_companyname.error = null
        }
        if (editable === edit_companynumber.editableText) {
            edit_companynumber.error = null
        }
        if (editable === edit_officestreet.editableText) {
            edit_officestreet.error = null
        }
        if (editable === edit_officepostalcode.editableText) {
            edit_officepostalcode.error = null
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }
}
