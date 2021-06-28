package com.mobiversa.ezy2pay.base

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.text.format.Formatter
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.location.LocationManagerCompat
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mobiversa.ezy2pay.BuildConfig
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.network.response.*
import com.mobiversa.ezy2pay.ui.loginActivity.LoginActivity
import com.mobiversa.ezy2pay.utils.Constants
import com.mobiversa.ezy2pay.utils.Constants.Companion.Boost
import com.mobiversa.ezy2pay.utils.Constants.Companion.EzyAuth
import com.mobiversa.ezy2pay.utils.Constants.Companion.EzyMoto
import com.mobiversa.ezy2pay.utils.Constants.Companion.EzyRec
import com.mobiversa.ezy2pay.utils.Constants.Companion.EzySplit
import com.mobiversa.ezy2pay.utils.Constants.Companion.Ezywire
import com.mobiversa.ezy2pay.utils.Constants.Companion.GrabPay
import com.mobiversa.ezy2pay.utils.Constants.Companion.MobiCash
import com.mobiversa.ezy2pay.utils.Constants.Companion.MobiPass
import com.mobiversa.ezy2pay.utils.Fields
import com.mobiversa.ezy2pay.utils.LocationService
import com.mobiversa.ezy2pay.utils.PreferenceHelper
import com.mobiversa.ezy2pay.utils.PreferenceHelper.get
import com.mobiversa.ezy2pay.utils.PreferenceHelper.set
import org.jetbrains.anko.indeterminateProgressDialog
import java.io.IOException
import java.net.NetworkInterface
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList


open class BaseFragment : Fragment() {

    lateinit var mProgressDialog: ProgressDialog
    lateinit var activity: Activity
    val MULTIPLE_PERMISSIONS = 10

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
    }

    fun showDialog(message: String) {
        mProgressDialog = activity.indeterminateProgressDialog(message)
        if (!mProgressDialog.isShowing)
            mProgressDialog.show()
    }

    fun cancelDialog() {
        if (mProgressDialog.isShowing)
            mProgressDialog.dismiss()
    }

    fun getLoginResponse(): ResponseData {
        val prefs: SharedPreferences = PreferenceHelper.defaultPrefs(activity)
        val response: String? = prefs[Constants.LoginResponse]
        val result = Gson()
        return result.fromJson(response, ResponseData::class.java)
    }

    fun getTidValue() : String{

        when {
            getLoginResponse().tid.isNotEmpty() -> {
                return getLoginResponse().tid
            }
            getLoginResponse().motoTid.isNotEmpty() -> {
                return  getLoginResponse().motoTid
            }
            getLoginResponse().ezypassTid.isNotEmpty() -> {
                return getLoginResponse().ezypassTid
            }
            getLoginResponse().ezyrecTid.isNotEmpty() -> {
                return getLoginResponse().ezyrecTid
            }
            getLoginResponse().ezysplitTid.isNotEmpty() -> {
                return getLoginResponse().ezyrecTid
            }
            else -> {
                return ""
            }
        }
    }

    fun getProductResponse(): ProductResponseData {
        val prefs: SharedPreferences = PreferenceHelper.defaultPrefs(activity)
        val response: String? = prefs[Constants.ProductResponse]
        val result = Gson()
        return result.fromJson(response, ProductResponseData::class.java)
    }

    fun getSharedString(value: String): String {
        val prefs: SharedPreferences = PreferenceHelper.defaultPrefs(activity)
        return prefs.getString(value, "").toString()
    }

    fun putSharedString(value: String, data: String) {
        val prefs: SharedPreferences = PreferenceHelper.defaultPrefs(activity)
        prefs[value] = data
    }

    fun putUserString(value: String, data: String) {
        val prefs: SharedPreferences = PreferenceHelper.customPrefs(activity, "RegData")
        prefs[value] = data
    }

    fun getRegisterString(value: String): String {
        val prefs: SharedPreferences = PreferenceHelper.customPrefs(
            activity,
            Constants.BusinessDetailData
        )
        val retMap: Map<String, String> = Gson().fromJson(
            prefs.getString(value, "").toString(),
            object : TypeToken<HashMap<String?, Any?>?>() {}.type
        )
        return retMap[value].toString()
    }

    fun putRegisterString(value: String, data: String) {
        val prefs: SharedPreferences = PreferenceHelper.customPrefs(
            activity,
            Constants.BusinessDetailData
        )
        prefs[value] = data
    }

    fun getUserString(value: String): String {
        val prefs: SharedPreferences = PreferenceHelper.customPrefs(activity, "RegData")
        return prefs.getString(value, "").toString()
    }

    fun getRegisterUserDetail(): RegisterResponseData {
        val prefs: SharedPreferences = PreferenceHelper.customPrefs(activity, "RegData")
        val response: String? = prefs[Constants.RegisterResponseData]
        val result = Gson()
        return result.fromJson(response, RegisterResponseData::class.java)

    }

    fun getEditBusinessDetail(): MerchantDetailResponseData {
        val prefs: SharedPreferences = PreferenceHelper.customPrefs(activity, "RegData")
        val response: String? = prefs[Constants.MerchantDetailData]
        val result = Gson()
        return result.fromJson(response, MerchantDetailResponseData::class.java)
    }

    fun logoutUser() {
        val prefs: SharedPreferences = PreferenceHelper.defaultPrefs(activity)
        val editor = prefs.edit().clear()
        editor.apply()
        clearRegData()
        // user is not logged in redirect him to Login Activity
        val i = Intent(activity, LoginActivity::class.java)
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        // Add new Flag to start new Activity
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        // Staring Login Activity
        activity.startActivity(i)
    }

    fun clearRegData() {
        val prefs: SharedPreferences = PreferenceHelper.customPrefs(activity, "RegData")
        val editor = prefs.edit().clear()
        editor.apply()
    }

    open fun isValidPassword(password: String?): Boolean {
        val pattern: Pattern
        val matcher: Matcher
        val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{4,}$"
        pattern = Pattern.compile(PASSWORD_PATTERN)
        matcher = pattern.matcher(password)
        return matcher.matches()
    }

    fun shortToast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    fun showLog(title: String, content: String) {
        if (BuildConfig.DEBUG) {
            Log.e(title, content)
        }
    }

    fun addFragment(fragment: Fragment, bundle: Bundle, backStack: String) {
        // Get the support fragment manager instance
        val manager = fragmentManager
        fragment.arguments = bundle
        val transaction = manager?.beginTransaction()
        // Replace the fragment on container
        transaction?.replace(R.id.coordinatorLayout, fragment)
        transaction?.addToBackStack(backStack)
        // Finishing the transition
        transaction?.commitAllowingStateLoss()
    }

    fun getProductList(): ArrayList<ProductList> {

        val loginResponse = getLoginResponse()

        val productList: ArrayList<ProductList> = ArrayList()

            productList.add(
                ProductList(
                    Ezywire,
                    R.drawable.ezywire_blue_icon, R.drawable.ezywire_disabled_icon,
                    loginResponse.tid,
                    loginResponse.mid,
                    loginResponse.enableEzywire.equals("Yes", false),
                    Fields.EZYWIRE
                )
            )
            productList.add(
                ProductList(
                    EzyRec,
                    R.drawable.ezyrec_blue_icon, R.drawable.recurring_disabled,
                    loginResponse.ezyrecTid,
                    loginResponse.ezyrecMid,
                    loginResponse.enableEzyrec.equals("Yes", false),
                    Fields.EZYREC
                )
            )
            productList.add(
                ProductList(
                    EzySplit,
                    R.drawable.ezyrec_blue_icon, R.drawable.recurring_disabled,
                    loginResponse.ezysplitTid,
                    loginResponse.ezysplitMid,
                    loginResponse.enableSplit.equals("Yes", false),
                    Fields.EZYSPLIT
                )
            )
            productList.add(
                ProductList(
                    EzyAuth,
                    R.drawable.ezyauth_blue_icon, R.drawable.ezyauth_disabled_icon,
                    "",
                    "",
                    loginResponse.preAuth.equals("Yes", false), Fields.PREAUTH
                )
            )
            productList.add(
                ProductList(
                    MobiPass,
                    R.drawable.mobipass_icon, R.drawable.mobipass_logo_disabled,
                    loginResponse.ezypassTid,
                    loginResponse.ezypassMid,
                    loginResponse.enableEzypass.equals("Yes", false),
                    Fields.EZYPASS
                )
            )


        productList.add(
            ProductList(
                Boost,
                R.drawable.ic_boost, R.drawable.boost_disabled_icon,
                "",
                "",
                loginResponse.enableBoost.equals("Yes", false),
                Fields.BOOST
            )
        )
        productList.add(
            ProductList(
                GrabPay,
                R.drawable.ic_grabpay, R.drawable.grab_disabled_icon,
                loginResponse.gpayMid,
                loginResponse.gpayTid,
                loginResponse.enableGrabPay.equals("Yes", false),
                Fields.GRABPAY
            )
        )

        productList.add(
            ProductList(
                MobiCash,
                R.drawable.cash_blue_icon, R.drawable.mobipass_logo_disabled,
                "",
                "",
                true,
                Fields.CASH
            )
        )

        return productList
    }


    fun setTitle(title: String, isCenter: Boolean) {

        (getActivity() as AppCompatActivity).supportActionBar!!.setHomeButtonEnabled(true)
        (getActivity() as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val textView = TextView(getActivity())
        textView.text = title
        textView.textSize = 20f
        textView.setTypeface(null, Typeface.BOLD)
        textView.layoutParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        if (isCenter)
            textView.gravity = Gravity.CENTER
        else
            textView.gravity = Gravity.LEFT
        textView.typeface = ResourcesCompat.getFont(context!!, R.font.ubuntu_bold)
        textView.setTextColor(resources.getColor(R.color.white))
        (getActivity() as AppCompatActivity).supportActionBar!!.displayOptions =
            ActionBar.DISPLAY_SHOW_CUSTOM
        (getActivity() as AppCompatActivity).supportActionBar!!.customView = textView
    }

    fun getAmount(amount: String): String {

        val ams: String = amount.replace(".", "")
        val amount_to_send = String.format("%012d", ams.toLong())

        return amount_to_send
    }

    fun String.decodeBase64IntoBitmap(): Bitmap {
        val imageBytes = Base64.decode(this, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(
            imageBytes, 0, imageBytes.size
        )
    }

    fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email)
            .matches()
    }

    fun isGPSEnabled(): Boolean {
        var result = false
        val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm?.run {
                cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                    result = when {
                        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                        else -> false
                    }
                }
            }
        } else {
            cm?.run {
                cm.activeNetworkInfo?.run {
                    if (type == ConnectivityManager.TYPE_WIFI) {
                        result = true
                    } else if (type == ConnectivityManager.TYPE_MOBILE) {
                        result = true
                    }
                }
            }
        }
        return result
    }

    fun checkAndRequestPermissions(): Boolean {
        val permissionSendMessage: Int = ContextCompat.checkSelfPermission(
            this.context!!,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val locationPermission: Int = ContextCompat.checkSelfPermission(
            this.context!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val cameraPermission: Int = ContextCompat.checkSelfPermission(
            this.context!!,
            Manifest.permission.CAMERA
        )
        val contactPermission: Int = ContextCompat.checkSelfPermission(
            this.context!!,
            Manifest.permission.READ_CONTACTS
        )
        val listPermissionsNeeded: MutableList<String> =
            java.util.ArrayList()
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (permissionSendMessage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (contactPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_CONTACTS)
        }
        if (permissionSendMessage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE)
        }
        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                listPermissionsNeeded.toTypedArray(),
                MULTIPLE_PERMISSIONS
            )
            return false
        }
        return true
    }

    fun requestPermissions(): Boolean {
        val permissionSendMessage: Int = ContextCompat.checkSelfPermission(
            this.context!!,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val locationPermission: Int = ContextCompat.checkSelfPermission(
            this.context!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val cameraPermission: Int = ContextCompat.checkSelfPermission(
            this.context!!,
            Manifest.permission.CAMERA
        )

        val listPermissionsNeeded: MutableList<String> =
            java.util.ArrayList()
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (permissionSendMessage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (permissionSendMessage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE)
        }
        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                listPermissionsNeeded.toTypedArray(),
                MULTIPLE_PERMISSIONS
            )
            return false
        }
        return true
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    fun getLocation() {

        LocationService.getLocation(activity, { location ->
            Log.e("Location ", location.latitude.toString())
            Constants.latitudeStr = location.latitude.toString()
            Constants.longitudeStr = location.longitude.toString()
            getCountryName(location.latitude, location.longitude)
        }, {
            Log.e("Location ", "Error")
        })

    }

    open fun getCountryName(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(context, Locale.getDefault())
        var addresses: List<Address>? = null
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1)
            var result: Address
            if (addresses != null && addresses.isNotEmpty()) {
                addresses[0].countryName
                Constants.countryStr = addresses[0].countryName
            } else Constants.countryStr = ""
        } catch (ignored: IOException) { //do something
        }
    }

    fun showDialog(title: String, description: String, activity: Activity) {
        // Initialize a new instance of
        val builder = AlertDialog.Builder(activity,R.style.AlertDialogTheme)
        // Set the alert dialog title
        builder.setTitle(title)
        // Display a message on alert dialog
        builder.setMessage(description)
        // Set a positive button and its cloick listener on alert dialog
        builder.setPositiveButton("Ok") { dialog, which ->
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
    fun getLocalIpAddress(): String {
        try {
            val en =
                NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val enumIpAddr =
                    intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress) {
                        val ip =
                            Formatter.formatIpAddress(inetAddress.hashCode())
                        Log.i("IP", "***** IP=$ip")
                        return ip
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e("IP Address", ex.toString())
        }
        return ""
    }

    fun getMacAddr(): String {
        try {
            val all = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (nif in all) {
                if (!nif.name.equals("wlan0", ignoreCase = true)) continue

                val macBytes = nif.hardwareAddress ?: return ""

                val res1 = StringBuilder()
                for (b in macBytes) {
                    //res1.append(Integer.toHexString(b & 0xFF) + ":");
                    res1.append(String.format("%02X:", b))
                }

                if (res1.length > 0) {
                    res1.deleteCharAt(res1.length - 1)
                }
                return res1.toString()
            }
        } catch (ex: Exception) {
        }

        return "02:00:00:00:00:00"
    }

}

