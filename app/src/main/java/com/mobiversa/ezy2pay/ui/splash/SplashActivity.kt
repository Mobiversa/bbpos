package com.mobiversa.ezy2pay.ui.splash

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.mobiversa.ezy2pay.MainActivity
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.base.BaseActivity
import com.mobiversa.ezy2pay.ui.loginActivity.LoginActivity
import com.mobiversa.ezy2pay.utils.Constants
import com.mobiversa.ezy2pay.utils.PreferenceHelper
import com.mobiversa.ezy2pay.utils.RootUtil

class SplashActivity : BaseActivity() {

    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        FirebaseApp.initializeApp(applicationContext)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        startAnimations() // start the animation
        val mHandler = Handler()
        val mRunnable = Runnable {
            // Check if the device is Rooted or not - if rooted exit the app (For PCI)
            if (RootUtil.isDeviceRooted) {
                showAlertDialogAndExitApp(Constants.ROOTED_DEVICE)
            } else {
                val prefs = PreferenceHelper.defaultPrefs(this)
                val login = prefs.getBoolean(Constants.IsLoggedIn, false)

                Log.v("--callFromSplash--", "intent")
                val intent = Intent(applicationContext, if (login) MainActivity::class.java else LoginActivity::class.java).apply {
                    putExtra("fromlogin", "Yes")
                }
                startActivity(intent)
                finish()
            }
        }
        mHandler.postDelayed(mRunnable, 5000)
    }

    /*Splash Text animation*/

    /*Splash Text animation*/
    private fun startAnimations() {
        var anim =
            AnimationUtils.loadAnimation(this, R.anim.alpha)
        anim.reset()
        val l = findViewById<View>(R.id.linearLayout) as LinearLayout
        l.clearAnimation()
        l.startAnimation(anim)
        anim = AnimationUtils.loadAnimation(this, R.anim.translate)
        anim.reset()
        val iv =
            findViewById<View>(R.id.imageView4) as AppCompatImageView
        val tv = findViewById<View>(R.id.textView) as AppCompatTextView
        iv.clearAnimation()
        tv.clearAnimation()
        iv.startAnimation(anim)
        tv.startAnimation(anim)
    }

    /*Close the app and exit*/

    /*Close the app and exit*/
    fun showAlertDialogAndExitApp(message: String?) {
        val alertDialog: AlertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle("Alert")
        alertDialog.setMessage(message)
        alertDialog.setCancelable(false)
        alertDialog.setButton(
            AlertDialog.BUTTON_NEUTRAL, "OK"
        ) { dialog, which ->
            dialog.dismiss()
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        alertDialog.show()
    }
}
