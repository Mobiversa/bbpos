package com.mobiversa.ezy2pay.ui.loginActivity.signup

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Html
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.base.BaseFragment
import com.mobiversa.ezy2pay.network.response.BankX
import com.mobiversa.ezy2pay.ui.loginActivity.LoginActivity
import com.mobiversa.ezy2pay.ui.loginActivity.LoginViewModel
import com.mobiversa.ezy2pay.utils.Constants
import com.mobiversa.ezy2pay.utils.Constants.Companion.pswdStr
import com.mobiversa.ezy2pay.utils.Encryptor
import com.mobiversa.ezy2pay.utils.Fields
import com.mobiversa.ezy2pay.utils.PreferenceHelper
import com.mobiversa.ezy2pay.utils.PreferenceHelper.get
import com.mobiversa.ezy2pay.utils.PreferenceHelper.set
import kotlinx.android.synthetic.main.fragment_bank_detail.view.*
import kotlinx.android.synthetic.main.fragment_bank_detail.view.back_sign_up
import kotlinx.android.synthetic.main.fragment_bank_detail.view.button_next
import kotlinx.android.synthetic.main.fragment_bank_detail.view.button_prev
import java.io.ByteArrayOutputStream


class BankDetailFragment : BaseFragment(), View.OnClickListener,
    AdapterView.OnItemSelectedListener {
    lateinit var rootView: View
    private val CAMERA_REQUEST = 1888
    private val MY_CAMERA_PERMISSION_CODE = 100
    private lateinit var viewModel: LoginViewModel
    var bankList: ArrayList<String> = ArrayList()
    var retailBankList: List<BankX> = ArrayList()
    private lateinit var countryAdapter: ArrayAdapter<String>
    //Bitmap photo;
    var photo: String = ""
    lateinit var theImage: Bitmap
    private lateinit var prefs: SharedPreferences
    private lateinit var customPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_bank_detail, container, false)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        prefs = PreferenceHelper.defaultPrefs(context!!)
        customPrefs = PreferenceHelper.customPrefs(context!!, "REMEMBER")

        rootView.fee_txt.paintFlags = rootView.fee_txt.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        val first = "I have read and agree to the "
        val one = "<font color='#005baa'><b><u>terms of service</b></u></font>"
        val two = "<font color='#005baa'><b> &amp; </b></font>"
        val three = "<font color='#005baa'><b><u> privacy policy</b></u></font>"
        rootView.terms_txt.text = Html.fromHtml(first + one + two + three)

        jsonBankList()

        //Page 3 data are hidden
        changeVisibility(4)
        rootView.button_prev.setOnClickListener(this)
        rootView.back_sign_up.setOnClickListener(this)
        rootView.button_next.setOnClickListener(this)
        rootView.terms_txt.setOnClickListener(this)
        checkAndRequestPermissions()
        rootView.camera_img.setOnClickListener(this)

        rootView.terms_check.setOnClickListener(this)

        if (!checkAndRequestPermissions()) {
            shortToast("Enable Location Permission From Settings")
        } else if (!isLocationEnabled(this.context!!)) {
            isGPSEnabled()
            shortToast("Enable GPS to Start")
        }else{
            getLocation()
        }

        return rootView
    }

    private fun setSpinner(){

        countryAdapter = ArrayAdapter(
            context!!,
            R.layout.spinner_item_country,
            bankList
        )
        rootView.spinner_bank_name.adapter = countryAdapter
        rootView.spinner_bank_name.setTitle("Bank List")
        rootView.spinner_bank_name.setPositiveButton("Confirm")
        rootView.spinner_bank_name.onItemSelectedListener = this
    }

    private fun validation(page: Int) {
         if (TextUtils.isEmpty(rootView.edit_owner_name.text.toString())) {
            rootView.edit_owner_name.error = "Enter valid data"
        } else if (TextUtils.isEmpty(rootView.edit_owner_contact.text.toString()) ||rootView.edit_owner_contact.text.toString().length<5) {
            rootView.edit_owner_contact.error = "Enter valid data"
        }else if (TextUtils.isEmpty(rootView.edit_passport.text.toString())) {
            rootView.edit_passport.error = "Enter valid data"
        } else if (!checkAndRequestPermissions()) {
            shortToast("Enable Location Permission From Settings")
            return
        } else if (!isLocationEnabled(this.context!!)) {
            isGPSEnabled()
            shortToast("Enable GPS to Start")
            return
        } else {
            getLocation()
            changeVisibility(4)
        }

        if (page==4){
            if (TextUtils.isEmpty(rootView.edit_account_no.text.toString())) {
                rootView.edit_account_no.error = "Enter valid data"
                return
            }else if(rootView.spinner_bank_name.selectedItemPosition ==0){
                shortToast("Select any one Bank")
            } else if (TextUtils.isEmpty(photo)) {
                shortToast("Add iC Copy")
                returnTransition
            } else if (!rootView.terms_check.isChecked) {
                shortToast("Please check T&C")
                return
            }else{
                getLocation()
                getData()
            }
        }

    }

    private fun getData() {
        val params: HashMap<String, String> = HashMap()
        params[Fields.Service] = Fields.LITE_APP_MERCHANT_REG
        params["registeredName"] = rootView.edit_bus_reg_name.text.toString()
        params["businessName"] = getUserString(Fields.businessName)
        params[Fields.ContactName] = getUserString(Fields.ContactName)
        params["businessRegNo"] = ""+rootView.edit_bus_regno.text.toString()
        params["businessType"] = getUserString(Fields.categoryName)
        params["email"] = getUserString(Fields.username)
        params["businessAddress"] = getUserString(Fields.street)
        params["businessCity"] = getUserString(Fields.city)
        params["businessPostCode"] = getUserString(Fields.postalCode)
        params["businessState"] = getUserString(Fields.state)
        params["website"] = rootView.edit_website.text.toString()
        params["contactIc"] = rootView.edit_passport.text.toString()
        params["contactNo"] = getUserString(Fields.MobileNo)
        params["title"] = "Mr"
        params[Fields.deviceToken] = getToken()
        params[Fields.deviceType] = Fields.Device
        params["ownerName"] = rootView.edit_owner_name.text.toString()
        params["ownerICNo"] = rootView.edit_passport.text.toString()
        params["ownerContactNo"] = rootView.edit_owner_contact.text.toString()
//        params["natureOfBusiness"] = rootView.edit_nob.text.toString()
        params["bankName"] = rootView.spinner_bank_name.selectedItem.toString()
        params["bankAccNo"] = rootView.edit_account_no.text.toString()
        params[Fields.Latitude] = Constants.latitudeStr
        params[Fields.Longitude] = Constants.longitudeStr
        params["ipAddress"] = getLocalIpAddress()
        params["mac"] = getMacAddr()


//        val hex_to_asci = Encryptor.hexaToAscii(getUserString(Fields.password), true)
//        Log.v("--hex_to Asci--", hex_to_asci)
//        val data_encrypted = Encryptor.decrypt(getUserString(Fields.date), getUserString(Fields.date), hex_to_asci).toString()

//        params["password"] = data_encrypted
        params["password"] = getUserString(Fields.password)
        params["icPhoto"] = photo

        showLog("Register", "" + params)
        jsonPostUserDetails(params)

    }
    private fun getToken(): String {
        val fcmToken: String? = prefs[Constants.FIRE_BASE_TOKEN]
        return fcmToken.toString()
    }

    private fun jsonPostUserDetails(detailsParam: HashMap<String, String>){
        showDialog("Loading...")
        viewModel.registerLiteUser(detailsParam)
        viewModel.successResponse.observe(this, androidx.lifecycle.Observer {
            cancelDialog()
            if (it.responseCode.equals("0000", true)) {

                prefs[Constants.UserName] = getUserString(Fields.username)
                prefs[Constants.IsLoggedIn] = false
                customPrefs[Constants.UserName] = getUserString(Fields.username)
                customPrefs[Constants.RememberMe] = true


                shortToast(it.responseDescription)
                startActivity(
                    Intent(getActivity(), LoginActivity::class.java).putExtra(
                        Fields.username,
                        it.responseData.username
                    )
                )

                /* val address = edit_officestreet.text.toString() + ", " + edit_officecity.text.toString() + ", " +
                        edit_officepostalcode.text.toString() + ", " + edit_officestate.text.toString()
                if (activation_code.isEmpty()){
                    val params = HashMap<String,String>()
                    params[Fields.Service] = Fields.UPGRADE
                    params["Company"] = edit_companyname.text.toString()
                    params["ContactNo"] = getUserString(Fields.MobileNo)
                    params["contactName"] = FullName
                    params["Address"] = address
                    params["Email"] = username
                    params["Website"] = ""
                    jsonConfirmMerchant(params)
                }else{
                    startActivity(Intent(getActivity(), LoginActivity::class.java))
                }*/
            } else {
                shortToast(it.responseDescription)
            }
        })
    }

    private fun jsonBankList(){
        val requestMap: HashMap<String, String> = HashMap()
        requestMap[Fields.Service] = Fields.FULL_LIST
        showDialog("Loading in...")
        viewModel.getBankList(requestMap)
        viewModel.bankData.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it.responseCode.equals("0000", true)) {
                cancelDialog()
                it.responseCode
                retailBankList = it.responseDataB2C.bankList
                bankList.add("Select Bank")
                for (data in retailBankList) {
                    bankList.add(data.BankName)
                }
                setSpinner()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                theImage = data.extras?.get("data") as Bitmap
            }
            photo = getEncodedString(theImage)
            rootView.edit_image.setText("icPhoto.png")
//            showLog("Image Base 64", photo)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "camera permission granted", Toast.LENGTH_LONG).show()
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, CAMERA_REQUEST)
            } else {
                Toast.makeText(getActivity(), "camera permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun getEncodedString(bitmap: Bitmap): String {
        val os = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, os)

        /* or use below if you want 32 bit images

       bitmap.compress(Bitmap.CompressFormat.PNG, (0–100 compression), os);*/
        val imageArr: ByteArray = os.toByteArray()
        return Base64.encodeToString(imageArr, Base64.URL_SAFE)
    }

    //mechant_details_linear -> page 3
    //bank_details_linear -> page 4
    private fun changeVisibility(page: Int) {

        when (page) {
            4 -> {
                rootView.mechant_details_linear.visibility = View.GONE
                rootView.bank_details_linear.visibility = View.VISIBLE
                rootView.button_prev.visibility = View.VISIBLE
                rootView.button_next.text = "Submit"
            }
            3 -> {
                rootView.mechant_details_linear.visibility = View.VISIBLE
                rootView.bank_details_linear.visibility = View.GONE
                rootView.button_prev.visibility = View.GONE
                rootView.button_next.text = "Next"
            }
        }


    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_prev -> {
                fragmentManager?.popBackStack()
//                changeVisibility(3)
            }
            R.id.back_sign_up -> {
                fragmentManager?.popBackStack()
            }
            R.id.button_next -> {
                if (rootView.mechant_details_linear.visibility == View.VISIBLE)
                    validation(3)
                else {
                    validation(4)
                }
            }
            R.id.camera_img -> {
                if (checkAndRequestPermissions()) {
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cameraIntent, CAMERA_REQUEST)
                } else {
                    checkAndRequestPermissions()
                }
            }
            R.id.terms_txt -> {
                alertWebview()
            }
        }
    }

    fun getPath(context: Context, uri: Uri?): String {
        var result: String? = null
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? =
            uri?.let { context.contentResolver.query(it, proj, null, null, null) }
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val column_index: Int = cursor.getColumnIndexOrThrow(proj[0])
                result = cursor.getString(column_index)
            }
            cursor.close()
        }
        if (result == null) {
            result = "Not found"
        }
        return result
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val spinner = parent as Spinner
        when (spinner.id) {
            R.id.spinner_bank_name -> {

            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    private fun alertWebview() {
        lateinit var mAlertDialog: AlertDialog

        val inflater = LayoutInflater.from(activity)
        val alertLayout: View = inflater.inflate(R.layout.alert_webview, null)

        val webView = alertLayout.findViewById<View>(R.id.mWebView) as WebView
        val close_txt = alertLayout.findViewById<View>(R.id.close_txt) as TextView
        val mBuilder = activity.let {
            AlertDialog.Builder(it)
                .setView(alertLayout)
        }

        close_txt.setOnClickListener {
            mAlertDialog.dismiss()
        }

        mAlertDialog = mBuilder.show()
        mAlertDialog.setCancelable(false)
        mAlertDialog.window?.setBackgroundDrawableResource(android.R.color.white)
        mAlertDialog.window?.setBackgroundDrawable(ColorDrawable(activity.resources.getColor(android.R.color.white)))

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(mAlertDialog.window?.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.MATCH_PARENT
        mAlertDialog.window?.attributes = lp
        mAlertDialog.show()

        webView.settings.javaScriptEnabled = true
        webView.settings.loadWithOverviewMode = true;
        webView.settings.useWideViewPort = true;
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                showDialog("Loading")
                if (url != null) {
                    view?.loadUrl(url)
                }
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                cancelDialog()
            }
        }
        webView.loadUrl(Constants.tcHtmlString)

    }
}