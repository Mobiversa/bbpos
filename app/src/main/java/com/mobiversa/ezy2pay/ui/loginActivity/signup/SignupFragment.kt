package com.mobiversa.ezy2pay.ui.loginActivity.signup

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.base.BaseFragment
import com.mobiversa.ezy2pay.network.response.Country
import com.mobiversa.ezy2pay.ui.ezyMoto.EzyMotoViewModel
import com.mobiversa.ezy2pay.ui.loginActivity.LoginActivity
import com.mobiversa.ezy2pay.ui.loginActivity.LoginViewModel
import com.mobiversa.ezy2pay.utils.Constants
import com.mobiversa.ezy2pay.utils.Encryptor
import com.mobiversa.ezy2pay.utils.Fields
import com.toptoche.searchablespinnerlibrary.SearchableSpinner
import kotlinx.android.synthetic.main.signup_fragment.*
import kotlinx.android.synthetic.main.signup_fragment.view.*
import java.util.concurrent.TimeUnit

class SignupFragment : BaseFragment() , View.OnClickListener, AdapterView.OnItemSelectedListener {

    private val countryArray = ArrayList<String>()
    private var spinnerPosition = 0
    private var countryNameStr = ""

    private lateinit var countryList: ArrayList<Country>
    private lateinit var countryAdapter: ArrayAdapter<String>
    private lateinit var countrySpinner: SearchableSpinner
    private lateinit var edtCountryCodeSignUp: EditText
    private lateinit var edtPhoneNumSignUp: EditText
    private lateinit var signup_timer_txt: AppCompatTextView
    private lateinit var resend_txt: AppCompatTextView
    private lateinit var edt_otp_sign_up: EditText
    private lateinit var sign_up_contact_linear: LinearLayout
    private lateinit var resend_linear: LinearLayout
    private lateinit var signUpOtpLinear: LinearLayout
    private lateinit var btn_next_sign_up: AppCompatTextView

    private var str_trace = ""
    private var str_date = ""
    private var hex_to_asci = ""
    private var data_encrypted = ""
    private var timer_count = 0

    val otpMap = HashMap<String,String>()

    companion object {
        fun newInstance() = SignupFragment()
    }

    private lateinit var viewModel: LoginViewModel

    private lateinit var motoViewModel: EzyMotoViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.signup_fragment, container, false)
        motoViewModel = ViewModelProviders.of(this).get(EzyMotoViewModel::class.java)

        val params : java.util.HashMap<String, String> = java.util.HashMap()
        params[Fields.Service] = Fields.MERCHANT_REG


        /*Glide.with(this)
            .asGif()
            .load(R.drawable.login_bg)
            .into(rootView.signup_bg_img)*/

        signup_timer_txt = rootView.signup_timer_txt
        resend_txt = rootView.resend_txt
        countrySpinner = rootView.spinner_country_signup
        edtCountryCodeSignUp = rootView.edt_country_code_sign_up
        edtPhoneNumSignUp = rootView.edt_phone_num_sign_up
        edt_otp_sign_up = rootView.edt_otp_sign_up
        signUpOtpLinear = rootView.sign_up_otp_linear
        sign_up_contact_linear = rootView.sign_up_contact_linear
        resend_linear = rootView.resend_linear
        btn_next_sign_up = rootView.btn_next_sign_up
        rootView.btn_send_sign_up.setOnClickListener(this)
        rootView.back_sign_up.setOnClickListener(this)
        rootView.btn_next_sign_up.setOnClickListener(this)
        rootView.resend_txt.setOnClickListener(this)
        countrySpinner.onItemSelectedListener = this

        sign_up_contact_linear.visibility = View.VISIBLE
        signUpOtpLinear.visibility = View.GONE
        resend_linear.visibility = View.GONE

        val productParams = HashMap<String, String>()
        productParams[Fields.Service] = Fields.CountryList
        jsonCountryList(productParams)


        return rootView
    }



    private fun jsonCountryList(countryParam: HashMap<String, String>) {
        showDialog("Processing...")
        motoViewModel.countryList(countryParam)
        motoViewModel.countryList.observe(viewLifecycleOwner, Observer {
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
                (activity as LoginActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
                fragmentManager?.popBackStack()
            }
        })
    }

    private fun timerTask() {
        timer_count += 1

        val countDownTimer = object : CountDownTimer(120000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                Log.d("tekloon", "millisUntilFinished $millisUntilFinished")
                val hms = String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                    ),
                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                    )
                )
                signup_timer_txt.text = "$hms"
            }

            override fun onFinish() {
                Log.d("tekloon", "onFinish")
                signup_timer_txt.visibility = View.GONE
                if (timer_count<2)
                resend_linear.visibility = View.VISIBLE
                else
                    resend_linear.visibility = View.GONE

            }
        }
        countDownTimer.start()
    }

    private fun setUpCountrySpinner() {

        countryAdapter = ArrayAdapter(
            context!!,
            R.layout.spinner_item_country,
            countryArray
        )
        countrySpinner.adapter = countryAdapter
        countrySpinner.setTitle("Select Countries")
        countrySpinner.setPositiveButton("Done")
        countrySpinner.setSelection(spinnerPosition)
        countrySpinner.onItemSelectedListener = this

        /*
        smsVerifyCatcher = new SmsVerifyCatcher(getActivity(), new OnSmsCatchListener<String>() {
            @Override
            public void onSmsCatch(String message) {
                String code = parseCode(message);//Parse verification code
                edit_otpnumber.setText(code);//set code in edit text
                //then you can send verification code to server
            }
        });
*/
        edtPhoneNumSignUp.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
                if (charSequence.toString().trim { it <= ' ' }.isEmpty()) {
                    btn_next_sign_up.visibility = View.GONE
                } else {
                    btn_next_sign_up.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        motoViewModel = ViewModelProviders.of(this).get(EzyMotoViewModel::class.java)
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.btn_send_sign_up -> {
                val txtMobileNumber =
                    edtPhoneNumSignUp.text.toString()
                if (txtMobileNumber == "" || txtMobileNumber.length < 8) { //                Toast.makeText(getApplicationContext(), Constants.PLEASE_ENTER_MOBILE, Toast.LENGTH_LONG).show();
                    shortToast(Constants.PLEASE_ENTER_MOBILE)
                } else if (!isValidMobile(txtMobileNumber)) { //                Toast.makeText(getApplicationContext(), Constants.PLEASE_ENTER_VALIDMOBILE, Toast.LENGTH_LONG).show();
                    shortToast(Constants.PLEASE_ENTER_VALID_MOBILE)
                }  else {

                    otpMap[Fields.Service]=Fields.ReqOTP
                    otpMap[Fields.MobileNo]=edtCountryCodeSignUp.text.toString() + edtPhoneNumSignUp.text.toString()
                    otpMap[Fields.deviceToken]=(activity as LoginActivity).getToken()
                    otpMap[Fields.deviceType]=Fields.Device
                    otpMap[Fields.Country]= countryNameStr

                    jsonOTP(otpMap)
                    showLog("OTPMAP", ""+otpMap)
                }
            }
            R.id.resend_txt ->{
                resend_linear.visibility = View.GONE
                jsonOTP(otpMap)
                showLog("OTPMAP", ""+otpMap)
            }
            R.id.back_sign_up -> {
                startActivity(Intent(getActivity(), LoginActivity::class.java))
            }
            R.id.btn_next_sign_up->{
                if (!edt_otp_sign_up.text.toString().equals("", ignoreCase = true)) {
                    if (data_encrypted.equals(edt_otp_sign_up.text.toString(),ignoreCase = true)) {
                        putUserString(Fields.MobileNo,edtCountryCodeSignUp.text.toString() + edtPhoneNumSignUp.text.toString())
                        putUserString(Constants.enteredMobilenumber,edtPhoneNumSignUp.text.toString())
                        putUserString(Fields.Country,countryNameStr)
                        val bundle = Bundle()
                        val registerUserDetailFragment = RegisterUserDetailFragment()
//                        val registerUserDetailFragment = BankDetailFragment()
                        addFragment(registerUserDetailFragment,bundle,"RegisterUserDetail")
//                        addFragment(registerUserDetailFragment,bundle,"BankDetail")
                    } else {
                        Snackbar.make(
                            coordinatorLayout,
                            Constants.ENTERED_WRONG_OTP,
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun jsonOTP(otpMap: HashMap<String, String>) {
        showDialog("Processing in...")
        viewModel.getOTP(otpMap)
        viewModel.otpResponse.observe(this, Observer {
            if (it.responseCode == "0000") {
                cancelDialog()
                str_trace = it.responseData.trace
                str_date = it.responseData.date
                putUserString(Constants.currencyCode, it.responseData.countryCurPhone.currencyCode)

                hex_to_asci = Encryptor.hexaToAscii(str_trace, true)
                Log.v("--hex_to Asci--", hex_to_asci)
                data_encrypted = Encryptor.decrypt(str_date, str_date, hex_to_asci).toString()
                sign_up_contact_linear.visibility = View.GONE
                signUpOtpLinear.visibility = View.VISIBLE
                timerTask()
                showLog("Response ", it.responseMessage)
                showLog("OTP ", data_encrypted)
            } else {
                shortToast(it.responseDescription)
                cancelDialog()
            }
        })
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val spinner = parent as Spinner
        when (spinner.id) {
            R.id.spinner_country_signup -> {
                edtCountryCodeSignUp.setText("" + countryList[position].phoneCode)
                countryNameStr = countryList[position].countryName
            }
        }
    }

    private fun isValidMobile(phone: String): Boolean {
        return Patterns.PHONE.matcher(phone).matches()
    }

}
