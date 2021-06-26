package com.mobiversa.ezy2pay.ui.settings.profileUpdate

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.view.menu.MenuBuilder
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.gson.Gson
import com.mobiversa.ezy2pay.MainActivity
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.base.BaseFragment
import com.mobiversa.ezy2pay.ui.loginActivity.signup.DatePickerFragmentpast
import com.mobiversa.ezy2pay.ui.settings.SettingsFragment
import com.mobiversa.ezy2pay.ui.settings.SettingsViewModel
import com.mobiversa.ezy2pay.utils.Constants
import com.mobiversa.ezy2pay.utils.Fields
import kotlinx.android.synthetic.main.profile_update_fragment.view.*
import java.util.*
import kotlin.collections.HashMap

class ProfileUpdateFragment : BaseFragment() , View.OnClickListener{

    private var date: String = ""
    private val MAX_DATE = 10

    lateinit var edtFbLogin : EditText
    lateinit var edtGmailLogin : EditText
    lateinit var edtUserName : EditText
    lateinit var edtPassword : EditText
    lateinit var edtConfirmPassword : EditText
    lateinit var buttonDob : Button
    lateinit var txtDob : TextView

    companion object {
        fun newInstance() = ProfileUpdateFragment()
    }

    private lateinit var viewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.profile_update_fragment, container, false)
        viewModel = ViewModelProviders.of(this).get(SettingsViewModel::class.java)

        //Basic Setup
        (activity as MainActivity).supportActionBar?.title = "Profile Update"
        setTitle("Profile Update", false)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        initializer(rootView)


        return rootView
    }

    override fun onResume() {
        super.onResume()
        //Basic Setup
        (activity as MainActivity).supportActionBar?.title = "Profile Update"
        setTitle("Profile Update", false)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
    }

    private fun initializer(rootView: View){
        edtFbLogin = rootView.edt_fb_id
        edtGmailLogin = rootView.edt_google_id
        edtUserName = rootView.edt_user_name
        edtPassword = rootView.edt_password
        edtConfirmPassword = rootView.edt_confirm_password
        buttonDob = rootView.button_dob
        txtDob = rootView.txt_dob

        edtFbLogin.isEnabled = false
        edtGmailLogin.isEnabled = false
        edtUserName.isEnabled = false
        edtPassword.isEnabled = false
        edtConfirmPassword.isEnabled = false
        buttonDob.isEnabled = false

        buttonDob.setOnClickListener(this)
        rootView.btn_next_update.setOnClickListener(this)

        val productParams = HashMap<String, String>()
        productParams[Fields.Service] = Fields.GET_MERCHANT_DET
        productParams[Fields.username] = getSharedString(Constants.UserName)
        productParams[Fields.sessionId] = getLoginResponse().sessionId
        if (getLoginResponse().tid.isNotEmpty()) {
            productParams[Fields.tid] = getLoginResponse().tid
            productParams[Fields.mid] = getLoginResponse().mid.toString()
        }else if (getLoginResponse().motoTid.isNotEmpty()) {
            productParams[Fields.tid] = getLoginResponse().motoTid
            productParams[Fields.mid] = getLoginResponse().motoMid
        }else{
            productParams[Fields.tid] = getLoginResponse().ezyrecTid
            productParams[Fields.mid] = getLoginResponse().ezyrecMid
        }
        jsonMerchantDetail(productParams)
    }

    private fun jsonMerchantDetail(boostParams: HashMap<String, String>) {
        showDialog("Processing...")
        viewModel.getMerchantDetail(boostParams)
        viewModel.merchantDetailModel.observe(viewLifecycleOwner, Observer {
            cancelDialog()
            if (it.responseCode.equals("0000", true)) {

                val fbLogin: String = it.responseData.fbLogin
                val username: String =  it.responseData.username
                val googleLogin: String = it.responseData.googleLogin
                val dob: String = it.responseData.dob
                if (fbLogin.equals("Yes", ignoreCase = true)) {
                    edtFbLogin.setText(it.responseData.facebookId)
                }
                if (googleLogin.equals("Yes", ignoreCase = true)) {
                    edtGmailLogin.setText(it.responseData.googleId)
                }
                edtUserName.setText(username)
                if (!dob.equals(" ", ignoreCase = true)) {
                    txtDob.text = dob
                }

                val gson = Gson()
                val json = gson.toJson(it.responseData)
                putUserString(Constants.MerchantDetailData, json)

            }else{
                val bundle = Bundle()
                val settingsFragment = SettingsFragment()
                addFragment(settingsFragment, bundle, "Settings")
//                setTitle("Profile", true)
//                (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
//                fragmentManager?.popBackStack()
            }

        })
    }

    private fun validateNext(){
        if (edtFbLogin.text.toString().isNotEmpty()) {
            putUserString(Constants.fb, "Yes")
            putUserString(Constants.fbname, edtFbLogin.text.toString())
        } else {
            putUserString(Constants.fb, "No")
            putUserString(Constants.fbname, "nofb")
        }
        if (edtGmailLogin.text.toString().isNotEmpty()) {
            putUserString(Constants.gmail, "Yes")
            putUserString(Constants.gmailname, edtGmailLogin.text.toString())
        } else {
            putUserString(Constants.gmail, "No")
            putUserString(Constants.gmailname, "nogmail")
        }
        if (edtUserName.text.toString().isNotEmpty()) {
            putUserString(Constants.UserName, edtUserName.text.toString())
        } else {
           shortToast("Please Enter Username")
            return
        }
        if (edtPassword.text.toString().isNotEmpty() && edtConfirmPassword.text.toString().isNotEmpty()) {
            if (!edtPassword.text.toString().equals(edtConfirmPassword.text.toString(),ignoreCase = true)) {
                shortToast("Passwords not matching")
                edtConfirmPassword.setText("")
                edtPassword.setText("")
                return
            } else if (edtPassword.text.toString().length < 6 && edtConfirmPassword.text.toString().length < 6) {
                shortToast("Password should be minimum 6 characters")
                edtConfirmPassword.setText("")
                edtPassword.setText("")
                return
            } else {
                putUserString(Fields.password, edtPassword.text.toString())
            }
        }
//        if (!txtDob.text.toString().equals("Date of Birth", ignoreCase = true)) {
//            putUserString(Constants.dob, txtDob.text.toString())
//        }

        val bundle = Bundle()
        val businessDetailFragment = EditBusinessDetailFragment()
        addFragment(businessDetailFragment, bundle, "BusinessDetail")
    }

    private fun datePicker() {
        val date = DatePickerFragmentpast()
        val calender = Calendar.getInstance()
        val args = Bundle()
        args.putInt(Constants.YEAR, calender[Calendar.YEAR])
        args.putInt(Constants.MONTH, calender[Calendar.MONTH])
        args.putInt(Constants.DAY, calender[Calendar.DAY_OF_MONTH])
        date.arguments = args
        date.setCallBack(dob)
        date.show(fragmentManager!!, "Date Picker")
    }

    @SuppressLint("SetTextI18n")
    var dob = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
        val userDate: Calendar =
            GregorianCalendar(year, monthOfYear, dayOfMonth)
        date = "$year/$monthOfYear/$dayOfMonth"
        val month = monthOfYear + 1
        if (month < MAX_DATE && dayOfMonth < MAX_DATE) { //edit_date.setText(year + "-" + "0" + month + "-" + "0" + dayOfMonth);
            txtDob.text = "$dayOfMonth/0$month/$year"
        } else if (MAX_DATE in (dayOfMonth + 1)..month) { //edit_date.setText(year + "-" + month + "-" + "0" + dayOfMonth);
            txtDob.text = "$dayOfMonth/$month/$year"
        } else if (month < MAX_DATE && dayOfMonth > MAX_DATE) { //edit_date.setText(year + "-" + "0" + month + "-" + dayOfMonth);
            txtDob.text = "$dayOfMonth/0$month/$year"
        } else { //edit_date.setText(year + "-" + month + "-" + dayOfMonth);
            txtDob.text = "$dayOfMonth/$month/$year"
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SettingsViewModel::class.java)
        // TODO: Use the ViewModel
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_edit, menu)
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
                setTitle("Profile", true)
                (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
                fragmentManager?.popBackStack()
                true
            }
            R.id.action_edit -> {
                edtFbLogin.isEnabled = true
                edtGmailLogin.isEnabled = true
                edtUserName.isEnabled = true
                edtPassword.isEnabled = true
                edtConfirmPassword.isEnabled = true
                buttonDob.isEnabled = true
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.button_dob -> {
                datePicker()
            }
            R.id.btn_next_update -> {
                validateNext()
            }
        }
    }

}
