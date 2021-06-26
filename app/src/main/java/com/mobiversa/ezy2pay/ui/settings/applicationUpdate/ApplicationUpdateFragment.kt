package com.mobiversa.ezy2pay.ui.settings.applicationUpdate


import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.browser.customtabs.CustomTabsClient.getPackageName
import androidx.fragment.app.Fragment
import com.mobiversa.ezy2pay.BuildConfig
import com.mobiversa.ezy2pay.MainActivity
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.base.BaseFragment
import com.mobiversa.ezy2pay.ui.settings.SettingsFragment
import com.mobiversa.ezy2pay.utils.Constants
import kotlinx.android.synthetic.main.fragment_application_update.view.*
import kotlin.math.min



/**
 * A simple [Fragment] subclass.
 */
class ApplicationUpdateFragment : BaseFragment() , View.OnClickListener{

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_application_update, container, false)

        (activity as MainActivity).supportActionBar?.title = "App Update"
        setTitle("App Update", false)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        val serverVersion = getLoginResponse().appVersion
        val versionName = BuildConfig.VERSION_NAME
        val versionCode = BuildConfig.VERSION_CODE

        showLog("Server", serverVersion)
        showLog("Version", versionName)
        showLog("Version Code", ""+versionCode)
        showLog("Version Check 1", ""+compareVersions(serverVersion,versionName)) //2
        showLog("Version Check 2", ""+compareVersions(versionName,serverVersion)) //-2

        rootView.update_btn.setOnClickListener(this)

        if (compareVersions(versionName,serverVersion)<0){
            //Update Available
            rootView.update_txt.text = "Time to Fuel up your app for more\n" +
                    "Amazing Feature..! $serverVersion"
            rootView.update_btn.visibility = View.VISIBLE
        }else{
            //No Update
            rootView.update_txt.text = "Mobi is up to date"
            rootView.update_btn.visibility = View.GONE
        }

        return rootView

    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).supportActionBar?.title = "App Update"
        setTitle("App Update", false)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val components1 = v1.split("\\.").toTypedArray()
        val components2 = v2.split("\\.").toTypedArray()
        val length = min(components1.size, components2.size)
        for (i in 0 until length) {
            val result = components1[i].compareTo(components2[i])
            if (result != 0) {
                return result
            }
        }
        return components1.size.compareTo(components2.size)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val activity = activity as? MainActivity
        return when (item.itemId) {
            android.R.id.home -> {
                var bundle = Bundle()
                setTitle("Profile", true)
                val fragment = SettingsFragment()
                addFragment(fragment,bundle, Constants.Product)
                setTitle("Profile", true)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.update_btn -> {
                val appPackageName = "com.mobiversa.ezy2pay"// package name of the app
                try {
                    startActivity(
                        Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=$appPackageName")))
                } catch (anfe: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
                }
            }
        }
    }

}
