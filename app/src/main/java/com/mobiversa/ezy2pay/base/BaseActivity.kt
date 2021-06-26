package com.mobiversa.ezy2pay.base

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.gson.Gson
import com.mobiversa.ezy2pay.BuildConfig
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.network.response.ProductList
import com.mobiversa.ezy2pay.network.response.ResponseData
import com.mobiversa.ezy2pay.utils.Constants
import com.mobiversa.ezy2pay.utils.Constants.Companion.countryStr
import com.mobiversa.ezy2pay.utils.Constants.Companion.latitudeStr
import com.mobiversa.ezy2pay.utils.Constants.Companion.longitudeStr
import com.mobiversa.ezy2pay.utils.Fields
import com.mobiversa.ezy2pay.utils.LocationService
import com.mobiversa.ezy2pay.utils.PreferenceHelper
import com.mobiversa.ezy2pay.utils.PreferenceHelper.get
import com.mobiversa.ezy2pay.utils.PreferenceHelper.set
import org.jetbrains.anko.indeterminateProgressDialog
import java.io.IOException
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList

@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity() {
    private val DOT = "."
    lateinit var mProgressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        // setSupportActionBar(toolbar)

        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE)

        LocationService.init(this)
    }

    fun showLog(title: String,content : String) {
        if (BuildConfig.DEBUG){
            Log.e(title,content)
        }
    }

    fun showDialog(message: String) {
        mProgressDialog = indeterminateProgressDialog(message)
        if (!mProgressDialog.isShowing)
            mProgressDialog.show()
    }

    fun cancelDialog() {
        if (mProgressDialog.isShowing)
            mProgressDialog.dismiss()
    }

    fun getInstalledVersion(): String {
        val packageInfo: PackageInfo
        var version = StringBuilder()

        try {
            packageInfo = this.packageManager.getPackageInfo(this.packageName, 0)
            version = StringBuilder(packageInfo.versionName)

//            while (version.indexOf(DOT) != -1) {
//                version.deleteCharAt(version.indexOf(DOT))
//            }
            return version.toString()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return ""
    }



    open fun isValidPassword(password: String?): Boolean {
        val pattern: Pattern
        val matcher: Matcher
        val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{4,}$"
        pattern = Pattern.compile(PASSWORD_PATTERN)
        matcher = pattern.matcher(password)
        return matcher.matches()
    }

    fun getAmount(amount: String): String {

        val ams: String = amount.replace(".", "")

        return String.format("%012d", ams.toLong())
    }

    fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun shortToast(text: String) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
    }

    fun getSharedString(value: String, context: Context): String {
        val prefs: SharedPreferences = PreferenceHelper.defaultPrefs(context)
        return prefs[value]!!
    }

    fun putSharedString(value: String, data: String) {
        val prefs: SharedPreferences = PreferenceHelper.defaultPrefs(applicationContext)
        prefs[value] = data
    }

    fun getLoginResponse(context: Context): ResponseData {
        val prefs: SharedPreferences = PreferenceHelper.defaultPrefs(context)
        val response: String? = prefs[Constants.LoginResponse]
        val result = Gson()
        return result.fromJson(response, ResponseData::class.java)
    }

    fun addFragment(fragment: Fragment, bundle: Bundle, frameId: Int){
        fragment.arguments = bundle
        supportFragmentManager.inTransaction { replace(frameId, fragment) }
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    fun replaceFragment(fragment: Fragment, bundle: Bundle, frameId: Int) {
        fragment.arguments = bundle
        supportFragmentManager.inTransaction{replace(frameId, fragment)}
    }
    private inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) {
        beginTransaction().func().commit()
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val n = cm.activeNetwork
            if (n != null) {
                val nc = cm.getNetworkCapabilities(n)
                //It will check for both wifi and cellular network
                return nc!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
                )
            }
            return false
        } else {
            val netInfo = cm.activeNetworkInfo
            return netInfo != null && netInfo.isConnectedOrConnecting
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        LocationService.onRequestPermissionsResult(this, requestCode, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LocationService.onActivityResult(this, requestCode, resultCode)
    }

    fun getLocation() {

        LocationService.getLocation(this, { location ->
            Log.e("Location ", location.latitude.toString())
            latitudeStr = location.latitude.toString()
            longitudeStr = location.longitude.toString()
            getCountryName(location.latitude, location.longitude)
        }, {
            Log.e("Location ", "Error")
        })

    }

    open fun getCountryName(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(applicationContext, Locale.getDefault())
        var addresses: List<Address>? = null
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1)
            var result: Address
            countryStr = if (addresses != null && addresses.isNotEmpty()) {
                addresses[0].countryName
            } else ""
        } catch (ignored: IOException) { //do something
        }
    }

    fun networkError(context: Context) {
        shortToast("Internet Connection Error")
    }

    fun getProductList(): ArrayList<ProductList> {

        val loginResponse = getLoginResponse(applicationContext)

        val productList: ArrayList<ProductList> = ArrayList()
        if (loginResponse.auth3DS.equals("Yes",true)){
            productList.add(
                ProductList(
                    Constants.EzyMoto,
                    R.drawable.ezylink_blue_icon,R.drawable.link_disable_icon,
                    loginResponse.motoTid,
                    loginResponse.motoMid,
                    loginResponse.enableMoto.equals("Yes", false),
                    Constants.EzyMoto
                )
            )
        }else{
            productList.add(
                ProductList(
                    Constants.EzyMoto,
                    R.drawable.ezymoto_blue_icon,R.drawable.moto_disabled_icon,
                    loginResponse.motoTid,
                    loginResponse.motoMid,
                    loginResponse.enableMoto.equals("Yes", false),
                    Constants.EzyMoto
                )
            )
        }

        productList.add(
            ProductList(
                Constants.Ezywire,
                R.drawable.ezywire_blue_icon,R.drawable.ezywire_disabled_icon,
                loginResponse.tid,
                loginResponse.mid,
                loginResponse.enableEzywire.equals("Yes", false),
                Fields.EZYWIRE
            )
        )
        productList.add(
            ProductList(
                Constants.EzyRec,
                R.drawable.ezyrec_blue_icon,R.drawable.recurring_disabled,
                loginResponse.ezyrecTid,
                loginResponse.ezyrecMid,
                loginResponse.enableEzyrec.equals("Yes", false),
                Fields.EZYREC
            )
        )
        productList.add(
            ProductList(
                Constants.EzySplit,
                R.drawable.ezyrec_blue_icon,R.drawable.recurring_disabled,
                loginResponse.ezysplitTid,
                loginResponse.ezysplitMid,
                loginResponse.enableEzyrec.equals("Yes", false),
                Fields.EZYSPLIT
            )
        )
        productList.add(
            ProductList(
                Constants.EzyAuth,
                R.drawable.ezyauth_blue_icon,R.drawable.ezyauth_disabled_icon,
                "",
                "",
                loginResponse.preAuth.equals("Yes", false),Fields.PREAUTH
            )
        )
        productList.add(
            ProductList(
                Constants.Boost,
                R.drawable.ic_boost,R.drawable.boost_disabled_icon,
                "",
                "",
                loginResponse.enableBoost.equals("Yes", false),
                Fields.BOOST
            )
        )
        productList.add(
            ProductList(
                Constants.GrabPay,
                R.drawable.ic_grabpay,R.drawable.grab_disabled_icon,
                loginResponse.gpayMid,
                loginResponse.gpayTid,
                loginResponse.enableGrabPay.equals("Yes", false),
                Fields.GRABPAY
            )
        )
        productList.add(
            ProductList(
                Constants.MobiPass,
                R.drawable.mobipass_icon,R.drawable.mobipass_logo_disabled,
                loginResponse.ezypassTid,
                loginResponse.ezypassMid,
                loginResponse.enableEzypass.equals("Yes", false),
                Fields.EZYPASS
            )
        )
        productList.add(
            ProductList(
                Constants.MobiCash,
                R.drawable.cash_blue_icon,R.drawable.mobipass_logo_disabled,
                "",
                "",
                true,
                Fields.CASH
            )
        )

        return productList

    }

    fun showDialog(title: String, description: String) {
        // Initialize a new instance of
        val builder = AlertDialog.Builder(this,R.style.AlertDialogTheme)
        // Set the alert dialog title
        builder.setTitle(title)
        // Display a message on alert dialog
        builder.setMessage(description)
        // Set a positive button and its cloick listener on alert dialog
        builder.setPositiveButton("Ok") { dialog, which ->
            // Do something when user press the positive button
            dialog.dismiss()
            // Change the app background color
//            root_layout.setBackgroundColor(Color.RED)
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    fun showExitAlert(title: String, description: String) {
        val builder = AlertDialog.Builder(this,R.style.AlertDialogTheme)
        builder.setTitle(title)
        builder.setMessage(description)
        builder.setPositiveButton("Exit") { dialog, which ->
            dialog.dismiss()
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            startActivity(intent)
//            exitProcess(0)
            // Change the app background color
//            root_layout.setBackgroundColor(Color.RED)
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    fun EditText.onTextChange(onAfterTextChanged: OnAfterTextChangedListener) {
        addTextChangedListener(object : TextWatcher {
            private var text = ""

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
//                if (s?.length == 1) {
//                }
                onAfterTextChanged.complete()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        })
    }

    interface OnAfterTextChangedListener {
        fun complete ()
    }
}