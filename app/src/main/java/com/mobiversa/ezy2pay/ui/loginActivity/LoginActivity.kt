package com.mobiversa.ezy2pay.ui.loginActivity

import android.app.AlertDialog
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import com.facebook.*
import com.facebook.login.LoginResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.mobiversa.ezy2pay.MainActivity
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.base.BaseActivity
import com.mobiversa.ezy2pay.network.ApiService
import com.mobiversa.ezy2pay.network.response.LoginResponse
import com.mobiversa.ezy2pay.network.response.OTPResponse
import com.mobiversa.ezy2pay.ui.chat.ChatLoginActivity
import com.mobiversa.ezy2pay.ui.contact.ContactActivity
import com.mobiversa.ezy2pay.ui.loginActivity.signup.SignupFragment
import com.mobiversa.ezy2pay.ui.tutorial.TutorialActivity
import com.mobiversa.ezy2pay.utils.*
import com.mobiversa.ezy2pay.utils.Constants.Companion.FIRE_BASE_TOKEN
import com.mobiversa.ezy2pay.utils.Constants.Companion.IsLoggedIn
import com.mobiversa.ezy2pay.utils.Constants.Companion.LoginResponse
import com.mobiversa.ezy2pay.utils.Constants.Companion.RememberMe
import com.mobiversa.ezy2pay.utils.Constants.Companion.UserName
import com.mobiversa.ezy2pay.utils.PreferenceHelper.get
import com.mobiversa.ezy2pay.utils.PreferenceHelper.set
import de.adorsys.android.finger.Finger
import kotlinx.android.synthetic.main.activity_login_21.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern


class LoginActivity : BaseActivity(), View.OnClickListener,
    ConnectivityReceiver.ConnectivityReceiverListener {

    lateinit var callbackManager: CallbackManager
    private var snackBar: Snackbar? = null
    private val TAG: String = "LoginActivity"

    private lateinit var loginViewModel: LoginViewModel

    private lateinit var finger: Finger
    private lateinit var prefs: SharedPreferences
    private lateinit var customPrefs: SharedPreferences
    private lateinit var userNameStr: String
    private lateinit var data_encrypted: String

    var count: Int = 0

    val bundle = Bundle()
    private val deliveryApiService = ApiService.serviceRequest()
    val otpDataMap = HashMap<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_21)
        FacebookSdk.sdkInitialize(applicationContext)

        if (resources.getBoolean(R.bool.portrait_only)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }

        //Invalid Password , You have 4 attempts left!!

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        initializeComponents()
        intialTextwatcher()

    }

    private fun initializeComponents() {
        prefs = PreferenceHelper.defaultPrefs(this)
        customPrefs = PreferenceHelper.customPrefs(this, "REMEMBER")

        //FireBase Token
        FirebaseInstanceId.getInstance()
            .instanceId.addOnSuccessListener(this@LoginActivity) { instanceIdResult ->
                val newToken = instanceIdResult.token
                showLog("newToken", newToken)

                prefs[FIRE_BASE_TOKEN] = newToken
            }

        finger = Finger(applicationContext)
        loginViewModel = ViewModelProviders.of(this@LoginActivity)[LoginViewModel::class.java]

        callbackManager = CallbackManager.Factory.create()

        edt_username_login.hint = "Username / Email"
        edt_email_forgot.visibility = View.GONE


        btn_sign_in.setOnClickListener(this)
        forgot_pswd_txt.setOnClickListener(this)
        btn_submit_forgot.setOnClickListener(this)
        txt_login.setOnClickListener(this)
//        btn_chat.setOnClickListener(this)
        txt_signup.setOnClickListener(this)
        login_contact_linear.setOnClickListener(this)
        login_tutorial_linear.setOnClickListener(this)

        /*Glide.with(this)
            .asGif()
            .load(R.drawable.login_bg)
            .into(login_bg_img)*/

        prefs = PreferenceHelper.defaultPrefs(this)

        showLog("IsRemember", "" + getIsRemember())

        if (getIsRemember()) {
            val fingerprintsEnabled = finger.hasFingerprintEnrolled()
            userNameStr = customPrefs[UserName]!!
            edt_username_login.setText(userNameStr)
            if (!fingerprintsEnabled) {
                Toast.makeText(this, R.string.error_override_hw_unavailable, Toast.LENGTH_LONG)
                    .show()
            } else {
                showDialog()
            }
        }

        showLog("FireBase Token", getToken())
        showLog("RememberMe ", "" + getIsRemember())

        chk_remember_me.isChecked = getIsRemember()

        //Network Connection Check
        registerReceiver(
            ConnectivityReceiver(),
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
//        LocationService.init(this)


//        edt_username_login.setText("INFO@LEICA-STORE-MALAYSIA.COM")
//        edt_pswd_login.setText("abc123")
//        edt_username_login.setText("umsandym") //for ecom
//        edt_username_login.setText("santhoshlite@gomobi.io") //for Lite
//        edt_username_login.setText("santhoshlitess@gomobi.io") //for LiteSS
//        edt_username_login.setText("santhoshnormal@gomobi.io") //for Normal
//        edt_pswd_login.setText("abc123")
//        edt_username_login.setText("san") //for live
//        edt_pswd_login.setText("san123")

    }

    private fun intialTextwatcher() {
        edit_password.onTextChange(object : OnAfterTextChangedListener {
            override fun complete() {

                count = 0

                val data = edit_password.text.toString()

                val capitalPattern: Pattern = Pattern.compile(".*[A-Z]+.*")
                val smallPattern: Pattern = Pattern.compile(".*[a-z]+.*")
                val numericPattern: Pattern = Pattern.compile("(.)*(\\d)(.)*")
                val specialPattern: Pattern = Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]~-]")
                val containsNumber: Boolean = numericPattern.matcher(data).matches()
                val containsSpecial: Boolean = specialPattern.matcher(data).find()
                val containsCapital: Boolean = capitalPattern.matcher(data).matches()
                val containsSmall: Boolean = smallPattern.matcher(data).matches()

                four_rb.isChecked = containsNumber && containsSpecial
                three_rb.isChecked = containsCapital
                two_rb.isChecked = containsSmall
                one_rb.isChecked = data.length > 7

                count = if (four_rb.isChecked) count + 25 else count + 0
                count = if (three_rb.isChecked) count + 25 else count + 0
                count = if (two_rb.isChecked) count + 25 else count + 0
                count = if (one_rb.isChecked) count + 25 else count + 0

                security_progress.progress = count
                strength_txt.text = "Password Strength: $count%"

                if (count == 100) {
                    edit_password.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_password,
                        0,
                        R.drawable.ic_tick,
                        0
                    )
                    edit_re_password.isEnabled = true
                } else {
                    edit_password.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_password,
                        0,
                        0,
                        0
                    )
                    edit_re_password.isEnabled = false
                }

            }
        })

        edit_re_password.onTextChange(object : OnAfterTextChangedListener {
            override fun complete() {
                val conf_pswd = edit_re_password.text.toString()
                val pswd = edit_password.text.toString()

                if (!pswd.equals(conf_pswd, false)) {
//                    edit_re_password.error = "Password Mismatch"
                    edit_re_password.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_confirm_password,
                        0,
                        R.drawable.ic_warning,
                        0
                    )
                } else {
//                    edit_re_password.error = null
                    edit_re_password.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_confirm_password,
                        0,
                        R.drawable.ic_tick,
                        0
                    )
                }
            }
        })
    }

    private fun showDialog() {
        finger.showDialog(
            this,
            Triple(
                // title
                getString(R.string.title_fingerprint),
                // subtitle
                null,
                // description
                getString(R.string.text_fingerprint)
            )
        )
    }

    private fun userLogin() {

        val userName = edt_username_login.text.toString()
        val password = edt_pswd_login.text.toString()

        if (userName.isEmpty() || userName.length < 2) {
            edt_username_login.error = "Enter Valid User Name"
        } else if (password.isEmpty() || password.length < 3 || password.length > 50) {
            edt_pswd_login.error = "Enter Valid Password"
        } else {
            val loginMap = HashMap<String, String>()
            loginMap[Fields.Service] = Fields.ServiceLogin
            loginMap[Fields.username] = userName
            loginMap[Fields.password] = password
            loginMap[Fields.appVersionNum] = getInstalledVersion()
            loginMap[Fields.deviceToken] = getToken()
            loginMap[Fields.deviceType] = Fields.Device
            jsonLogin(loginMap)
        }

    }

    private fun forgotPassword() {

        val userName = edt_username_forgot.text.toString().trim()
        val email = edt_email_forgot.text.toString()

        if (userName.isEmpty() || userName.length < 2 || userName.length > 100) {
            edt_username_forgot.error = "Enter Valid Username"
        } else {

            otpDataMap[Fields.Service] = Fields.ServiceForgotOTP
            otpDataMap[Fields.username] = userName
//            jsonLogin(loginMap)
            jsonOTP(otpDataMap) //SUSPENDED
        }
    }

    private fun confirmPswd() {

        val otp = edt_otp_forget.text.toString()
        val password = edit_password.text.toString()
        val confPassword = edit_re_password.text.toString()

        if (!otp.equals(data_encrypted, true)) {
            shortToast("Enter Valid OTP")
        } else {
            otpDataMap[Fields.Service] = Fields.UPDATEPASSWORD
            otpDataMap[Constants.otp] = otp
            otpDataMap[Fields.password] = password
            jsonOTP(otpDataMap) //SUSPENDED
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
    //Login User
    private fun jsonLogin(loginMap: HashMap<String, String>) {
        showDialog("Logging in...")
        deliveryApiService.login(loginMap).enqueue(object : Callback<LoginResponse> {
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                showLog("LoginModel Fail", "" + t.message)
                shortToast(t.message!!)
            }

            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    cancelDialog()
                    loginResponse(loginMap, response.body()!!)
                } else {
                    showLog("LoginModel Fail", "" + response.message())

                    shortToast(response.message())
                    cancelDialog()
                }
            }
        })
    }


    private fun jsonOTP(otpMap: HashMap<String, String>) {
        showDialog("Processing in...")
        deliveryApiService.getOTP(otpMap)
        deliveryApiService.getOTP(otpMap).enqueue(object : Callback<OTPResponse> {
            override fun onResponse(call: Call<OTPResponse>, response: Response<OTPResponse>) {
                if (response.body()!!.responseCode == "0000") {
                    cancelDialog()
                    if (otp_linear.isVisible) {
                        forgot_linear.visibility = View.GONE
                        login_linear.visibility = View.VISIBLE
                        otp_linear.visibility = View.GONE
                        shortToast(response.body()!!.responseDescription)

                        edt_otp_forget.setText("")
                        edit_password.setText("")
                        edit_re_password.setText("")

                    } else {
                        val str_date = response.body()!!.responseData.date
//                    putUserString(Constants.currencyCode, response.body()!!.responseData.countryCurPhone.currencyCode)
                        val hex_to_asci = Encryptor.hexaToAscii(
                            response.body()!!.responseData.trace,
                            true
                        )
                        otpDataMap[Fields.MobileNo] = response.body()!!.responseData.mobileNo
                        data_encrypted =
                            Encryptor.decrypt(str_date, str_date, hex_to_asci).toString()
                        forget_user_linear.visibility = View.GONE
                        otp_linear.visibility = View.VISIBLE

                        shortToast(response.body()!!.responseDescription)
                        showLog("OTP ", data_encrypted)
                    }
                } else {
                    shortToast(response.body()!!.responseDescription)
                    cancelDialog()
                }
            }

            override fun onFailure(call: Call<OTPResponse>, t: Throwable) {

            }

        })
    }

    fun loginResponse(
        loginMap: HashMap<String, String>,
        it: LoginResponse
    ) {
        cancelDialog()
        if (it.responseCode == "0000") {
            warning_rel.visibility = View.GONE
            if (loginMap[Fields.Service].equals(Fields.ServiceLogin)) {
                Toast.makeText(applicationContext, it.responseDescription, Toast.LENGTH_SHORT)
                    .show()

                val gson = Gson()
                val json = gson.toJson(it.responseData)
                prefs[LoginResponse] = json
                prefs[UserName] = loginMap[Fields.username]
                prefs[IsLoggedIn] = true
                customPrefs[UserName] = loginMap[Fields.username]
                customPrefs[RememberMe] = chk_remember_me.isChecked

                if (it.responseData.auth3DS.equals("Yes", true) ||
                    it.responseData.type.equals("LITE", true)
                ) {
                    Constants.EzyMoto = "EZYLINK"
                    prefs[Constants.UpgradeStatus] = getLoginResponse(applicationContext).liteUpdate
                } else
                    Constants.EzyMoto = "EZYMOTO"

                showLog("IsRemember 1", "" + chk_remember_me.isChecked)
                showLog("IsRemember 2", "" + getIsRemember())

                startActivity(Intent(this, MainActivity::class.java))

            } else {
                Toast.makeText(applicationContext, it.responseDescription, Toast.LENGTH_SHORT)
                    .show()
                forgot_linear.visibility = View.GONE
                login_linear.visibility = View.VISIBLE
                txt_new_user.text = "Still without account?"
                txt_signup.text = "Create One"
            }

        } else {
            warning_rel.visibility = View.VISIBLE

            if(!it.responseDescription.equals("Invalid Username and Password")){
                val arr = it.responseDescription.split(" ".toRegex()).toTypedArray()

                var data = arr[5]
                if (!data.equals("reset")) {

                    warning_title_txt.text = "Attempts remaining: $data"
                    val one = "<font color='#000'><b>Warning:</b> After </font>"
                    val two =
                        "<font color='#212121'><b> 5 consecutive </b> unsuccessful login attempts, your account will be locked.<br></font>"
                    val four =
                        "<font color='#212121'>Don???t remember your password? Reset it by clicking ???Forgot Password??? below</font>"
                    warning_desc_txt.text = Html.fromHtml(one + two + four)
                } else {
                    val one = "<font color='#000'><b>Warning:</b> </font>"
                    val two = "<font color='#212121'><b>Your Account is locked,<br></b> </font>"
                    val four =
                        "<font color='#212121'>Reset password by clicking <b>???Forgot Password???</b> below</font>"
                    warning_desc_txt.text = Html.fromHtml(one + two + four)
                }
            }else{
                shortToast(it.responseDescription)
            }


        }
    }

    public override fun onStart() {
        super.onStart()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.forgot_pswd_txt -> {
                forgot_linear.visibility = View.VISIBLE
                forget_user_linear.visibility = View.VISIBLE
                login_linear.visibility = View.GONE
                otp_linear.visibility = View.GONE
            }
            R.id.btn_sign_in -> {
                userLogin()
            }
            R.id.btn_submit_forgot -> {
                if (otp_linear.isVisible) {
                    confirmPswd()
                } else
                    forgotPassword()
            }
            R.id.btn_chat -> {
                showChatPrompt()
            }
            R.id.txt_signup -> {

                if (txt_signup.text.toString().equals("Login", false)) {
                    forgot_linear.visibility = View.GONE
                    login_linear.visibility = View.VISIBLE
                    txt_new_user.text = "Still without account?"
                    txt_signup.text = "Create One"
                } else {
                    //Intent to sign up page

                    val bundle = Bundle()
                    val signupFragment = SignupFragment()
                    addFragment(signupFragment, bundle, R.id.coordinatorLayout)
                }

            }
            R.id.txt_login -> {
                forgot_linear.visibility = View.GONE
                login_linear.visibility = View.VISIBLE
                txt_new_user.text = "Still without account?"
                txt_signup.text = "Create One"
            }
            R.id.login_contact_linear -> {
                startActivity(Intent(applicationContext, ContactActivity::class.java))
            }
            R.id.login_tutorial_linear -> {
                startActivity(Intent(applicationContext, TutorialActivity::class.java))
            }
        }
    }
    private fun showChatPrompt() {
        lateinit var mAlertDialog: AlertDialog

        val inflater = layoutInflater
        val alertLayout: View = inflater.inflate(R.layout.alert_before_login_chat, null)
        val nameEdt = alertLayout.findViewById<View>(R.id.edit_name) as EditText
        val phoneEdt = alertLayout.findViewById<View>(R.id.edit_phone) as EditText
        val companyEdt = alertLayout.findViewById<View>(R.id.edit_company) as EditText

        val positiveBtn = alertLayout.findViewById<View>(R.id.button_chat) as Button
        val negativeBtn = alertLayout.findViewById<View>(R.id.button_cancel) as Button
        val alert: AlertDialog.Builder = AlertDialog.Builder(this)
        alert.setView(alertLayout)
        alert.setCancelable(false)

        positiveBtn.setOnClickListener {

            val name = nameEdt.text.toString()
            val phone = phoneEdt.text.toString()
            var company = companyEdt.text.toString()

            when {
                name.isEmpty() -> { //EditText is empty
                    nameEdt.error = Constants.PLEASE_ENTER_NAME
                }
                phone.isEmpty() -> {
                    phoneEdt.error = Constants.PLEASE_ENTER_MOBILE
                }
                else -> { //EditText is not empty
                    if (company.isEmpty()) {
                        company = ""
                    }
                    startActivity(
                        Intent(this, ChatLoginActivity::class.java)
                            .putExtra("name", name)
                            .putExtra("phone", phone)
                            .putExtra("company", company)
                    )
                    mAlertDialog.dismiss()
                }
            }

        }

        negativeBtn.setOnClickListener {
            mAlertDialog.dismiss()
        }

        mAlertDialog = alert.create()
        mAlertDialog.show()
        //startActivity(new Intent(GoogleMaps.this, VoidAuthActivity.class).putExtra("trensID", transId));
    }

    fun getToken(): String {
        val fcmToken: String? = prefs[FIRE_BASE_TOKEN]
        return fcmToken.toString()
    }

    private fun getIsRemember(): Boolean {
        return customPrefs.getBoolean(RememberMe, false)
    }

    override fun onResume() {
        super.onResume()
        ConnectivityReceiver.connectivityReceiverListener = this

    }

    override fun onPause() {
        super.onPause()
    }

    override fun onBackPressed() {
        showExitAlert("Exit Alert", "Are you sure you want to exit from Mobiversa?")
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        showNetworkMessage(isConnected)
    }

    private fun showNetworkMessage(isConnected: Boolean) {
        if (!isConnected) {
            snackBar = Snackbar.make(
                findViewById(R.id.coordinatorLayout),
                "You are offline",
                Snackbar.LENGTH_LONG
            ) //Assume "rootLayout" as the root layout of every activity.
            snackBar?.duration = BaseTransientBottomBar.LENGTH_INDEFINITE
            snackBar?.show()

            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
        } else {
            snackBar?.dismiss()
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }
}

