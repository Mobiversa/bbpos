package com.mobiversa.ezy2pay.ui.notifications.notificationFcm

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mobiversa.ezy2pay.MainActivity
import com.mobiversa.ezy2pay.R

class ShowNotificationAcknowledgement : AppCompatActivity() {

    var txtmessage: String? = null
    var txtTitle: String? = null
    var main_layout: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_notification_acknowledgement)

        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down)
        findViewById<View>(R.id.TryAgain).setOnClickListener { sendSuccess() }

        val message = findViewById<View>(R.id.message) as TextView
        val title = findViewById<View>(R.id.title) as TextView
        main_layout = findViewById<View>(R.id.main_layout) as LinearLayout

        txtmessage = intent.getStringExtra("notify_msg")
        txtTitle = intent.getStringExtra("notify_title")

        if (txtTitle.equals("EZYMOTO Payment Failure", ignoreCase = true)) {
            message.text = txtmessage
            title.text = txtTitle
            title.setTextColor(ContextCompat.getColor(applicationContext, R.color.void_red))
            message.setTextColor(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.colorPrimaryDark
                )
            )
        } else if (txtTitle.equals("EZYAUTH Payment Failure", ignoreCase = true)) {
            message.text = txtmessage
            title.text = txtTitle
            title.setTextColor(ContextCompat.getColor(applicationContext, R.color.void_red))
            message.setTextColor(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.colorPrimaryDark
                )
            )
        } else {
            message.text = txtmessage
            title.text = txtTitle
            title.setTextColor(ContextCompat.getColor(applicationContext, R.color.green))
            message.setTextColor(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.colorPrimaryDark
                )
            )
        }

        main_layout!!.setOnClickListener { startActivity(
            Intent( applicationContext, MainActivity::class.java ) )
            finish() }
        setFinishOnTouchOutside(true)
    }

    private fun sendSuccess() {
        startActivity(
            Intent(
                applicationContext,
                MainActivity::class.java
            )
        )
        finish()
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down)
    }

    override fun onBackPressed() { // TODO :: not required
        startActivity(Intent(applicationContext, MainActivity::class.java))
    }
}
