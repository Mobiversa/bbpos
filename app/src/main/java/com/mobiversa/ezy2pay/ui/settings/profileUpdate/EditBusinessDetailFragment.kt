package com.mobiversa.ezy2pay.ui.settings.profileUpdate


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.annotation.RequiresApi
import androidx.appcompat.view.menu.MenuBuilder
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.mobiversa.ezy2pay.MainActivity
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.base.BaseFragment
import com.mobiversa.ezy2pay.ui.loginActivity.LoginViewModel
import com.mobiversa.ezy2pay.utils.Constants
import com.mobiversa.ezy2pay.utils.Fields
import com.mobiversa.ezy2pay.utils.Fields.Companion.fbLogin
import kotlinx.android.synthetic.main.fragment_edit_business_detail.view.*
import org.json.JSONException
import java.util.*
import kotlin.collections.HashMap

/**
 * A simple [Fragment] subclass.
 */
class EditBusinessDetailFragment : BaseFragment(), View.OnClickListener, TextWatcher {

    private lateinit var viewModel: LoginViewModel
    var spinner_business_category: Spinner? = null
    var business_category: String = ""
    private var businessArrayList: ArrayList<HashMap<String, String>>? =
        null
    private lateinit var businessList: HashMap<String, String>
    private val categorycodes = ArrayList<String>()
    private val categoryName = ArrayList<String>()
    private lateinit var businessAdapter: SimpleAdapter
    private lateinit var edit_companyname: EditText
    private lateinit var edit_companynumber: EditText
    private lateinit var edit_officestreet: EditText
    private lateinit var edit_category_name: EditText
    private lateinit var edit_officecity: EditText
    private lateinit var edit_officestate: EditText
    private lateinit var edit_officepostalcode: EditText
    private lateinit var button_next: Button
    private lateinit var relativeBusinessCategory: RelativeLayout
    private lateinit var category_code: String
    private lateinit var category_name: String
    private lateinit var username: String
    private lateinit var password: String
    private lateinit var fb: String
    private lateinit var fbname: String
    private lateinit var gmail: String
    private lateinit var gmailname: String
    private lateinit var dob: String
    private lateinit var merchantType: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_edit_business_detail, container, false)
        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        //Basic Setup
        (activity as MainActivity).supportActionBar?.title = "Profile Update"
        setTitle("Profile Update", false)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initializer(rootView)

        setHasOptionsMenu(true)

//        showLog(Constants.RegisterResponseData, "" + getEditBusinessDetail().listCategoryData.size)



        return rootView
    }

    override fun onResume() {
        super.onResume()
        //Basic Setup
        (activity as MainActivity).supportActionBar?.title = "Profile Update"
        setTitle("Profile Update", false)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initializer(rootView: View){

        button_next = rootView.button_next
        button_next.setOnClickListener(this)
        edit_companyname = rootView.edit_companyname
        edit_companynumber = rootView.edit_companynumber
        edit_category_name = rootView.edit_category_name
        edit_officestreet = rootView.edit_officestreet
        edit_officecity = rootView.edit_officecity
        edit_officestate = rootView.edit_officestate
        edit_officepostalcode = rootView.edit_officepostalcode
        edit_companyname.addTextChangedListener(this)
        edit_companynumber.addTextChangedListener(this)
        edit_officestreet.addTextChangedListener(this)
        edit_officecity.addTextChangedListener(this)
        edit_officestate.addTextChangedListener(this)
        edit_officepostalcode.addTextChangedListener(this)

        spinner_business_category = rootView.spinner_business_category
        relativeBusinessCategory = rootView.relative_business_category


        username = getUserString(Constants.UserName)
        password = getUserString(Fields.password)
        fb = getUserString(Constants.fb)
        fbname = getUserString(Constants.fbname)
        gmail = getUserString(Constants.gmail)
        gmailname = getUserString(Constants.gmailname)
//        dob = getUserString(Constants.dob)
        merchantType = getEditBusinessDetail().merchantType


        if (merchantType.equals(Constants.NONEZYWIRE, false)){
            loadBusinessSpinner()
        }

        setUI()

    }

    private fun setUI() {
        try {
            val officeNo = getEditBusinessDetail().officeNo
            val businessName = getEditBusinessDetail().businessName
            val street = getEditBusinessDetail().street
            val city = getEditBusinessDetail().city
            val state = getEditBusinessDetail().state
            val postalCode = getEditBusinessDetail().postalCode
            val categoryName =getEditBusinessDetail().categoryName
            merchantType = getEditBusinessDetail().merchantType
            if (merchantType.equals("NONEZYWIRE", ignoreCase = true)) {
                relativeBusinessCategory.visibility = View.VISIBLE
            } else {
                relativeBusinessCategory.visibility = View.GONE
            }
            showLog("Edit", ""+getEditBusinessDetail())
            edit_companyname.setText(businessName)
            edit_companynumber.setText(officeNo)
            edit_officestreet.setText(street)
            edit_officecity.setText(city)
            edit_officestate.setText(state)
            edit_officepostalcode.setText(postalCode)
            edit_category_name.setText(categoryName)

            edit_companyname.isEnabled = false
            edit_companynumber.isEnabled = false
            edit_officestreet.isEnabled = false
            edit_officecity.isEnabled = false
            edit_officestate.isEnabled = false
            edit_officepostalcode.isEnabled = false
            edit_category_name.isEnabled = false
        } catch (je: JSONException) {
        }
    }

    private fun loadBusinessSpinner() {

        spinner_business_category!!.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                business_category = categoryName[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

            try {
                businessArrayList = ArrayList()
                val categoryList = getEditBusinessDetail().listCategoryData
                if (categoryList != null) {
                    for (data in categoryList) {
                        category_code = data.categoryCode
                        category_name = data.categoryName
                        // SEND JSON DATA INTO SPINNER TITLE LIST
                        businessList = java.util.HashMap()
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
                            spinner_business_category!!.adapter = businessAdapter
                        }
                    }
                }
            } catch (JE: JSONException) {
            }

    }

    private fun validateFields() {
        if (TextUtils.isEmpty(edit_companyname.text.toString())
            && TextUtils.isEmpty(edit_companynumber.text.toString())
            && TextUtils.isEmpty(edit_officestreet.text.toString())
            && TextUtils.isEmpty(edit_officecity.text.toString())
            && TextUtils.isEmpty(edit_officestate.text.toString())
            && TextUtils.isEmpty(edit_officepostalcode.text.toString())
        ) {
            edit_companyname.error = Constants.ENTER_COMPANY_NAME
            edit_companynumber.error = Constants.ENTER_COMPANY_PHONE
            edit_officestreet.error = Constants.ENTER_STREET
            edit_officecity.error = Constants.ENTER_CITY
            edit_officestate.error = Constants.ENTER_STATE
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
    }

    private fun postToServer(){
        //19 params
        val params : HashMap<String,String> = HashMap()
        params[Fields.Service] = Fields.UPDATE_USER_DET
        when {
            getLoginResponse().tid.isNotEmpty() -> {
                params[Fields.tid] = getLoginResponse().tid
                params[Fields.mid] = getLoginResponse().mid!!
            }
            getLoginResponse().motoTid.isNotEmpty() -> {
                params[Fields.tid] = getLoginResponse().motoTid
                params[Fields.mid] = getLoginResponse().motoMid
            }
            getLoginResponse().ezypassTid.isNotEmpty() -> {
                params[Fields.tid] = getLoginResponse().ezypassTid
                params[Fields.mid] = getLoginResponse().ezypassMid
            }
            getLoginResponse().ezyrecTid.isNotEmpty() -> {
                params[Fields.tid] = getLoginResponse().ezyrecTid
                params[Fields.mid] = getLoginResponse().ezyrecMid
            }
        }
        params[Fields.merchantType] = merchantType
        params["officeNo"] = edit_companynumber.text.toString()
        params["businessName"] = edit_companyname.text.toString()
        params["categoryName"] = business_category
//        params["dob"] = dob
        params["fbLogin"] = fbLogin
        params["facebookId"] = fbname
        params["googleLogin"] = gmail
        params["googleId"] = gmailname
        params["street"] = edit_officestreet.text.toString()
        params["city"] = edit_officecity.text.toString()
        params["state"] = edit_officestate.text.toString()
        params["postalCode"] = edit_officepostalcode.text.toString()
        params["password"] = password
        params["username"] = username
        params[Fields.sessionId] = getLoginResponse().sessionId

        jsonPostUserDetails(params)
    }

    private fun jsonPostUserDetails(detailsParam : HashMap<String,String>){
        showDialog("Processing in...")
        viewModel.updateMerchant(detailsParam)
        viewModel.updateResponse.observe(this, androidx.lifecycle.Observer {
            if (it.responseCode.equals("0000",true)){
                shortToast(it.responseDescription)
                clearRegData()
                startActivity(Intent(getActivity(), MainActivity::class.java))
            }else
                shortToast(it.responseDescription)
            cancelDialog()
        })
    }

    private fun isValidMobile(phone: String): Boolean {
        return Patterns.PHONE.matcher(phone).matches()
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.button_next -> {
                validateFields()
            }
            R.id.back_sign_up -> {
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
        if (editable === edit_officecity.editableText) {
            edit_officecity.error = null
        }
        if (editable === edit_officestate.editableText) {
            edit_officestate.error = null
        }
        if (editable === edit_officepostalcode.editableText) {
            edit_officepostalcode.error = null
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_edit, menu)
        val edit = menu.findItem(R.id.action_edit)
        edit.isVisible = merchantType.equals(Constants.NONEZYWIRE,true)
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
                fragmentManager?.popBackStack()
                true
            }
            R.id.action_edit -> {
                edit_companyname.isEnabled = true
                edit_companynumber.isEnabled = true
                edit_officestreet.isEnabled = true
                edit_officecity.isEnabled = true
                edit_officestate.isEnabled = true
                edit_officepostalcode.isEnabled = true
                edit_category_name.isEnabled = true
                relativeBusinessCategory.visibility = View.VISIBLE
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
