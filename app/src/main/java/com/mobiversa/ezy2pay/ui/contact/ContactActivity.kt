package com.mobiversa.ezy2pay.ui.contact

import android.content.ClipData
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.RequiresApi
import com.mobiversa.ezy2pay.R
import kotlinx.android.synthetic.main.activity_contact.*
import org.jetbrains.anko.toast

class ContactActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)

        supportActionBar?.title = "Contact Us"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        contact_email_txt.setOnClickListener {
            val clipboard =
                applicationContext!!.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip =
                ClipData.newPlainText("Copied Text", contact_email_txt.text.toString())
            clipboard.setPrimaryClip(clip)
            toast("Text Copied to ClipBoard")
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
               onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
