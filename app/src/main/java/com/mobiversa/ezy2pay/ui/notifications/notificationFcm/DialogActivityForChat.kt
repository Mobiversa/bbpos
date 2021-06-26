package com.mobiversa.ezy2pay.ui.notifications.notificationFcm

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.base.BaseActivity
import com.mobiversa.ezy2pay.ui.chat.ChatLoginActivity
import kotlinx.android.synthetic.main.activity_dialog_for_chat.*

class DialogActivityForChat : BaseActivity() {

    var txtmessage: String? = null
    var txtTitle:String? = null
    var button_cancel: Button? = null
    var button_go:Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialog_for_chat)

        val message = text_message
        val title = text_title

//        main_layout = (LinearLayout) findViewById(R.id.main_layout);

        //        main_layout = (LinearLayout) findViewById(R.id.main_layout);
        txtmessage = intent.getStringExtra("notify_msg")
        txtTitle = intent.getStringExtra("notify_title")
        button_cancel!!.setOnClickListener { finish() }
        button_go!!.setOnClickListener {
            startActivity(
                Intent(
                    applicationContext,
                    ChatLoginActivity::class.java
                ).putExtra("login", "no")
            )
        }


        message.text = txtmessage
        title.text = txtTitle
    }
}
