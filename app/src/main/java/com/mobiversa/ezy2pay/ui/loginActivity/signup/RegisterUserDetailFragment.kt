package com.mobiversa.ezy2pay.ui.loginActivity.signup


import android.annotation.SuppressLint
import android.app.DatePickerDialog.OnDateSetListener
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.ViewModelProviders
import com.facebook.AccessToken
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.gson.Gson
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.base.BaseFragment
import com.mobiversa.ezy2pay.ui.loginActivity.LoginActivity
import com.mobiversa.ezy2pay.ui.loginActivity.LoginViewModel
import com.mobiversa.ezy2pay.utils.*
import com.mobiversa.ezy2pay.utils.PreferenceHelper.set
import kotlinx.android.synthetic.main.fragment_register_user_detail.view.*
import kotlinx.android.synthetic.main.fragment_register_user_detail.view.back_sign_up
import kotlinx.android.synthetic.main.fragment_register_user_detail.view.button_dob
import kotlinx.android.synthetic.main.fragment_register_user_detail.view.button_next
import kotlinx.android.synthetic.main.fragment_register_user_detail.view.edit_cpassword
import kotlinx.android.synthetic.main.fragment_register_user_detail.view.edit_fullname
import kotlinx.android.synthetic.main.fragment_register_user_detail.view.edit_password
import kotlinx.android.synthetic.main.fragment_register_user_detail.view.edit_username
import kotlinx.android.synthetic.main.fragment_register_user_detail.view.facebook_img
import kotlinx.android.synthetic.main.fragment_register_user_detail.view.google_img
import kotlinx.android.synthetic.main.fragment_register_user_detail.view.login_button
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.mail.internet.InternetAddress
import kotlin.collections.HashMap


class RegisterUserDetailFragment : BaseFragment(), View.OnClickListener,
    GoogleApiClient.OnConnectionFailedListener,
    CompoundButton.OnCheckedChangeListener {

    private var date: String = ""

    //    private lateinit var toggleButton: AppCompatToggleButton
    private lateinit var editUsername: AppCompatEditText
    private lateinit var editPassword: AppCompatEditText
    private lateinit var editCpassword: AppCompatEditText
    private lateinit var editActivationcode: AppCompatEditText
    private lateinit var editFullname: AppCompatEditText
    private lateinit var login_button: LoginButton

    private lateinit var buttonDob: AppCompatEditText
    private lateinit var buttonNext: AppCompatTextView

    private lateinit var merchantType: String
    private var facebookMail: String = ""
    private var googleMail: String = ""

    private val MAX_DATE = 10
    lateinit var rootView: View

    private lateinit var viewModel: LoginViewModel
    private val RC_SIGN_IN = 9001

    private lateinit var prefs: SharedPreferences
    private lateinit var customPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_register_user_detail, container, false)
        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)

        prefs = PreferenceHelper.defaultPrefs(context!!)
        customPrefs = PreferenceHelper.customPrefs(context!!, "REMEMBER")

        initializer(rootView)

        return rootView
    }

    private fun initializer(rootView: View) {
//        toggleButton = rootView.toggle_ezywire

        login_button = rootView.login_button
        editUsername = rootView.edit_username
        editPassword = rootView.edit_password
        editCpassword = rootView.edit_cpassword
        editActivationcode = rootView.edit_activationcode
        editFullname = rootView.edit_fullname

        buttonDob = rootView.button_dob
        buttonNext = rootView.button_next
        buttonDob.setOnClickListener(this)
        buttonDob.isClickable = true
        buttonDob.isLongClickable = false //Prevent cpy paste

//        editUsername.setText("sangavi@gomobi.io")
//        editFullname.setText("SANGAVI")
//        editPassword.setText("San@123")
//        editCpassword.setText("San@123")

        rootView.activation_linear.visibility = View.GONE
        rootView.user_details_linear.visibility = View.VISIBLE

        rootView.google_img.setOnClickListener(this)
        rootView.facebook_img.setOnClickListener(this)
        rootView.submit_btn_activation.setOnClickListener(this)

        buttonNext.setOnClickListener(this)
        rootView.back_sign_up.setOnClickListener(this)

//        toggleButton.setOnCheckedChangeListener(this)
        fbIntial()

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
    var dob = OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
        val userDate: Calendar =
            GregorianCalendar(year, monthOfYear, dayOfMonth)
        date = "$year/$monthOfYear/$dayOfMonth"
        val month = monthOfYear + 1
        if (month < MAX_DATE && dayOfMonth < MAX_DATE) { //edit_date.setText(year + "-" + "0" + month + "-" + "0" + dayOfMonth);
            buttonDob.setText("$dayOfMonth/0$month/$year")
        } else if (MAX_DATE in (dayOfMonth + 1)..month) { //edit_date.setText(year + "-" + month + "-" + "0" + dayOfMonth);
            buttonDob.setText("$dayOfMonth/$month/$year")
        } else if (month < MAX_DATE && dayOfMonth > MAX_DATE) { //edit_date.setText(year + "-" + "0" + month + "-" + dayOfMonth);
            buttonDob.setText("$dayOfMonth/0$month/$year")
        } else { //edit_date.setText(year + "-" + month + "-" + dayOfMonth);
            buttonDob.setText("$dayOfMonth/$month/$year")
        }
    }

    private fun validateFields(ezyWire: Boolean) {

        showLog("GMAIL ", "" + googleMail)
//        if (toggleButton.isChecked) {
        if (ezyWire) {
            merchantType = "EZYWIRE"
            if (TextUtils.isEmpty(editActivationcode.text.toString())) {
                editActivationcode.error = "Please enter the Activation code"
                return
            }
        } else {
            merchantType = "NONEZYWIRE"
        }
        if (TextUtils.isEmpty(editFullname.text.toString())) {
            editFullname.error = "FullName is required"
        }

        if (editUsername.text.toString().isEmpty() &&
            googleMail.isEmpty()
            && facebookMail.isEmpty()
        ) {

            editUsername.error = Constants.ENTER_UNAME
            return
        } else if (editUsername.text.toString().isNotEmpty()) {
            if (!isValidEmail(editUsername.text.toString())) {
                editUsername.error = "Please enter valid email id"
                return
            }
        } else if (editPassword.text.toString().isNotEmpty()) {
            if (!isValidPassword(editPassword.text.toString())) {
                shortToast("Password must contain Capital Letter,small letter,special character and numeric")
                return
            }
        }

        if (!isValidPassword(editPassword.text.toString())) {
            shortToast("Password must contain Capital Letter,small letter,special character and numeric")
            return
        }

        if (TextUtils.isEmpty(editPassword.text.toString())) {
            editPassword.error = Constants.ENTER_PASSWORD_REG
        } else if (TextUtils.isEmpty(editCpassword.text.toString())) {
            editCpassword.error = Constants.ENTER_CPASSWORD_REG
        } else if (!editPassword.text.toString()
                .equals(editCpassword.text.toString(), ignoreCase = true)
        ) {
            shortToast(Constants.PASSWORDS_NOT_MATCH)
        } else if (buttonDob.text.toString().equals("Select Date", ignoreCase = true)) {
            shortToast("please select the date of birth")
        } else {

            val fb: String
            val fbId: String
            val google: String
            val googleId: String

            if (facebookMail.isEmpty()) {
                fb = "No"
                fbId = "Nil"
            } else {
                fb = "Yes"
                fbId = facebookMail
            }
            if (googleMail.isEmpty()) {
                google = "No"
                googleId = "Nil"
            } else {
                google = "Yes"
                googleId = googleMail
            }

            val registerMap = HashMap<String, String>()
            registerMap[Fields.Service] = Fields.INT_MER_REG
            registerMap[Fields.username] = editUsername.text.toString()
            registerMap[Fields.password] = editPassword.text.toString()
            registerMap[Fields.merchantType] = merchantType
            val activationCode = editActivationcode.text.toString()
            if (!activationCode.equals("", ignoreCase = true)) {
                registerMap[Fields.activationCode] = activationCode
            }
            registerMap[Fields.fbLogin] = fb
            registerMap[Fields.facebookId] = fbId
            registerMap[Fields.googleLogin] = google
            registerMap[Fields.googleId] = googleId

            if (ezyWire) {
                jsonUserDetails(registerMap, ezyWire)
            } else {
                    val userMap = HashMap<String, String>()
                    userMap[Fields.Service] = Fields.INT_VERIFY_REG
                    userMap[Fields.email] = editUsername.text.toString()
                    jsonVerifyUser(userMap, registerMap)
            }

        }
    }

    private fun jsonVerifyUser(dataMap: HashMap<String, String>, userMap: HashMap<String, String>) {
        showDialog("Processing in...")
        viewModel.checkUserType(dataMap)
        viewModel.verifyUserResponse.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            cancelDialog()
            if (it.responseCode == "0000") {
                if (it.responseData.type.equals(Constants.LITE)) {
                    jsonUserDetails(userMap, false)
                } else {
                    rootView.activation_linear.visibility = View.VISIBLE
                    rootView.user_details_linear.visibility = View.GONE
                }
            } else {
                shortToast(it.responseDescription)
            }
            cancelDialog()
        })
    }

    private fun jsonUserDetails(userMap: HashMap<String, String>, ezyWire: Boolean) {
        showDialog("Processing in...")
        viewModel.setUserDetails(userMap)
        viewModel.userDetailResponse.observe(viewLifecycleOwner, androidx.lifecycle.Observer {

            if (it.responseCode == "0000") {
                cancelDialog()
                //SetValues in shared Preference
                putUserString(Constants.UserName, editUsername.text.toString())
                putUserString(Constants.FullName, editFullname.text.toString())
//                val date = Calendar.getInstance().time
//                // Display a date in day, month, year format
//                val formatter = SimpleDateFormat("ddMMyyyy")
//                val today = formatter.format(date)
//
//                val value_encrypted = Encryptor.encrypt(today+today, today+today, editPassword.text.toString())
//                Log.v("--encrypt--", ""+value_encrypted)
//                val hex_to_asci_name = Encryptor.encodeHexString(value_encrypted!!)
//
//                putUserString(Fields.date,today+today)
//                putUserString(Fields.password,hex_to_asci_name)
                putUserString(Fields.password,editPassword.text.toString())
                val activationCode: String
                if (TextUtils.isEmpty(editActivationcode.text.toString())) {
                    putUserString(Fields.activationCode, "")
                } else {
                    activationCode = editActivationcode.text.toString()
                    putUserString(Fields.activationCode, activationCode)
                }
                if (facebookMail.isEmpty()) {
                    putUserString(Constants.fb, "No")
                    putUserString(Constants.fbname, "nofb")
                } else {
                    putUserString(Constants.fb, "Yes")
                    putUserString(Constants.fbname, facebookMail)
                }
                if (googleMail.isEmpty()) {
                    putUserString(Constants.gmail, "No")
                    putUserString(Constants.gmailname, "nogmail")
                } else {
                    putUserString(Constants.gmail, "Yes")
                    putUserString(Constants.gmailname, googleMail)
                }
//                putUserString(Constants.dob, buttonDob.text.toString())
//                if (toggleButton.isChecked) {
                if (ezyWire) {
                    merchantType = "EZYWIRE"
                    putUserString(Constants.merchantType, merchantType)
                } else {
                    merchantType = "NONEZYWIRE"
                    putUserString(Constants.merchantType, merchantType)
                }

                userMap[Fields.MobileNo] = getUserString(Fields.MobileNo)
                userMap[Constants.FullName] = editFullname.text.toString()
                userMap[Fields.street] = it.responseData.merchantAddr
                userMap[Fields.postalCode] = it.responseData.merchantPostCode
                if (it.responseData.merchantName != null)
                userMap[Fields.businessName] = it.responseData.merchantName
                userMap[Fields.city] = it.responseData.merchantCity
                userMap[Fields.state] = it.responseData.merchantState
                userMap["officeNo"] = it.responseData.officeNo
                userMap["merchantType"] = merchantType
                userMap[Fields.categoryName] = ""
                userMap[Fields.contactName] =  editFullname.text.toString()
                userMap["currency"] = "MYR"
                userMap[Fields.country] = getUserString(Fields.Country)

//                val dataMap = userMap
//                dataMap.remove(Fields.password)

                val gson = Gson()
                val json = gson.toJson(it.responseData)
                val gmailMail = gson.toJson(userMap)
                putUserString(Constants.RegisterResponseData, json)

//                val auth = EmailService.UserPassAuthenticator("ezylinklite@gmail.com", "Mobi@7548")
//                val auth = EmailService.UserPassAuthenticator("ezylinklite@gmail.com", "Mobi123ver$@")
//                val to = listOf(InternetAddress("ezylinklite@gmail.com"))
//                val from = InternetAddress("ezylinklite@email.com")
                val auth = EmailService.UserPassAuthenticator("sangavi@gomobi.io", "S@9943291177")
                val to = listOf(InternetAddress("ezylinklite@gmail.com"))
                val from = InternetAddress("sangavi@gomobi.io")
                val email =
                    EmailService.Email(auth, to, from, "Lite merchant info", gmailMail.toString())
                val emailService = EmailService("smtp.office365.com", 587)

                GlobalScope.launch { // or however you do background threads
                    emailService.send(email)
                }
                if (ezyWire) {
                    userMap[Fields.Service] = Fields.MERCHANT_REG
                    jsonPostUserDetails(userMap)
                } else {
                    val bundle = Bundle()
                    val businessDetailFragment = BusinessDetailFragment()
                    addFragment(businessDetailFragment, bundle, "BusinessDetail")
                }



            } else {
                cancelDialog()
                shortToast(it.responseDescription)
            }

        })
    }

    private fun jsonPostUserDetails(detailsParam: HashMap<String, String>) {
        showDialog("processing in...")
        viewModel.registerUser(detailsParam)
        viewModel.successResponse.observe(this, androidx.lifecycle.Observer {
            if (it.responseCode.equals("0000", true)) {
                val params = detailsParam
                params[Fields.Service] = Fields.UPGRADE
                params["ContactNo"] = getUserString(Fields.MobileNo)
                params["contactName"] = detailsParam[Fields.contactName]!!
                params["Email"] = detailsParam[Fields.username]!!
                params["Website"] = ""
                jsonConfirmMerchant(params)
            }
        })
    }

    private fun jsonConfirmMerchant(detailsParam: HashMap<String, String>) {
        showDialog("Processing in...")
        viewModel.confirmMerchant(detailsParam)
        viewModel.confirmMerchantResponse.observe(this, androidx.lifecycle.Observer {
            if (it.responseCode.equals("0000")) {

                prefs[Constants.UserName] = getUserString(Fields.username)
                prefs[Constants.IsLoggedIn] = false
                customPrefs[Constants.UserName] = getUserString(Fields.username)
                customPrefs[Constants.RememberMe] = true

                startActivity(Intent(getActivity(), LoginActivity::class.java))
                shortToast(it.responseDescription)
            }
            else
                shortToast(it.responseDescription)
        })
        cancelDialog()
    }

    override fun onClick(v: View) {

        when (v.id) {
            R.id.back_sign_up -> {
                context!!.startActivity(Intent(context, LoginActivity::class.java))
            }
            R.id.button_next -> {
                validateFields(false)

            }
            R.id.submit_btn_activation -> {
                validateFields(true)
            }
            R.id.button_dob -> {
                datePicker()
            }
            R.id.google_img -> {
                googleSignIn()
            }
            R.id.facebook_img -> {
                login_button.performClick()
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (isChecked) {
            editActivationcode.visibility = View.VISIBLE
            merchantType = "EZYWIRE"
        } else {
            editActivationcode.visibility = View.GONE
            merchantType = "NONEZYWIRE"
        }
    }

    private fun googleSignIn() {
        val signInIntent = (activity as LoginActivity).googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                fireBaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                shortToast("Google sign in failed")
            }
        } else {
            (activity as LoginActivity).callbackManager.onActivityResult(
                requestCode,
                resultCode,
                data
            )
        }
    }

    private fun fireBaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        (activity as LoginActivity).auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    googleMail = acct.email!!
                    editUsername.setText(googleMail)
                    editUsername.isFocusable = false
                    putUserString("gmailData", googleMail)
                }
            }
    }

    private fun fbIntial() {
        login_button.registerCallback((activity as LoginActivity).callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
                // [START_EXCLUDE]
                // [END_EXCLUDE]
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
                // [START_EXCLUDE]
                // [END_EXCLUDE]
            }
        })
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")
        // [START_EXCLUDE silent]
        showDialog("Loading")
        // [END_EXCLUDE]

        val credential = FacebookAuthProvider.getCredential(token.token)
        (activity as LoginActivity).auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                val user = (activity as LoginActivity).auth.currentUser
                facebookMail = user?.email.toString()
                putUserString(Constants.fbname, facebookMail)
            } else {
                Log.w(TAG, "signInWithCredential:failure", it.exception)
                shortToast("Authentication failed.")
            }
        }
        cancelDialog()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }
}
