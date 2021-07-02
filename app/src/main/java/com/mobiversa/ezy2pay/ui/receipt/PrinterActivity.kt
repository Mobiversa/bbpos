package com.mobiversa.ezy2pay.ui.receipt

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProviders
import com.bbpos.bbdevice.BBDeviceController
import com.google.gson.Gson
import com.mobiversa.ezy2pay.MainActivity
import com.mobiversa.ezy2pay.base.BaseActivity
import com.mobiversa.ezy2pay.databinding.ActivityPrinterBinding
import com.mobiversa.ezy2pay.network.response.ReceiptModel
import com.mobiversa.ezy2pay.network.response.ResponseData
import com.mobiversa.ezy2pay.ui.ezyWire.MyBBPosController
import com.mobiversa.ezy2pay.utils.Constants
import com.mobiversa.ezy2pay.utils.Fields
import com.mobiversa.ezy2pay.utils.PreferenceHelper
import com.mobiversa.ezy2pay.utils.PreferenceHelper.get


class PrinterActivity : BaseActivity() {

    private var printerReceipts = ArrayList<ByteArray>()
    private lateinit var binding: ActivityPrinterBinding
    private var receiptData: ReceiptModel? = null
    private var service: String = ""
    private var trxId: String = ""
    private var amount: String = ""
    private var phoneNumber: String = ""
    private var isFromCardPayment: Boolean = false
    private var isSendReceipt: Boolean = false
    lateinit var wisePadController: BBDeviceController
    lateinit var listener: MyBBPosController
    private lateinit var viewModel: PrintReceiptViewModel
    private fun getBindingView(): View {
        binding = ActivityPrinterBinding.inflate(layoutInflater)
        return binding.root
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getBindingView())
        viewModel = ViewModelProviders.of(this).get(PrintReceiptViewModel::class.java)
        supportActionBar?.title = "Print Receipt"
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        service = intent.getStringExtra(Fields.Service) ?: ""
        trxId = intent.getStringExtra(Fields.trxId) ?: ""
        amount = intent.getStringExtra(Fields.Amount) ?: ""
        isFromCardPayment = intent.getBooleanExtra(Constants.CARD_PAYMENT, false)
        binding.transactionTd.text = trxId
        binding.paymentComplete.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        listener = MyBBPosController.getInstance(this, printerHandler)!!
        wisePadController = BBDeviceController.getInstance(this, listener)

        if (Build.VERSION.SDK_INT > 9) {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }
        wisePadController.startSerial()
        printerInit()
        jsonSendReceipt()
    }
    fun getSharedString(value: String): String {
        val prefs: SharedPreferences = PreferenceHelper.defaultPrefs(this)
        return prefs.getString(value, "").toString()
    }
    fun getLoginResponse(): ResponseData {
        val prefs: SharedPreferences = PreferenceHelper.defaultPrefs(this)
        val response: String? = prefs[Constants.LoginResponse]
        val result = Gson()
        return result.fromJson(response, ResponseData::class.java)
    }
    private fun jsonSendReceipt() {
        showDialog("Loading")
        val paymentParams = HashMap<String, String>()
        paymentParams[Fields.Service] = service
        paymentParams[Fields.username] = getSharedString(Constants.UserName)
        paymentParams[Fields.sessionId] = getLoginResponse().sessionId
        paymentParams[Fields.HostType] = getLoginResponse().hostType
        paymentParams[Fields.trxId] = trxId
        paymentParams[Fields.MobileNo] = phoneNumber
        paymentParams[Fields.email] = ""
        paymentParams[Fields.WhatsApp] = getIsWhatsapp(isSendReceipt)
        viewModel.getReceipt(paymentParams)
        viewModel.printReceiptData.observe(this, androidx.lifecycle.Observer {
            cancelDialog()
            if (it.responseCode.equals("0000", true)) {
                receiptData = it
            }else{
                shortToast(it.responseDescription)
                closePrintScreen()
            }
        })
    }
    private fun closePrintScreen() {
        if (isFromCardPayment) {
            startActivity(Intent(this@PrinterActivity, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
        } else {
            finish()
        }
    }
    private fun getIsWhatsapp(whatsApp: Boolean): String {
        return if (whatsApp) "Yes" else "No"
    }
    private fun printerInit() {
        binding.printMerchantCopy.setOnClickListener {
            it.visibility = View.GONE
            printReceiptData(true)
        }
        binding.printCustomerCopy.setOnClickListener {
            it.visibility = View.GONE
            printReceiptData(false)
        }
        binding.paymentComplete.setOnClickListener {
            closePrintScreen()
        }
        binding.sendCustomerCopy.setOnClickListener {
            it.visibility = View.GONE
            isSendReceipt = true
            val li = LayoutInflater.from(this)
            val promptsView: View = li.inflate(com.mobiversa.ezy2pay.R.layout.phone_number_dialog, null)
            val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(
                this
            )
            alertDialogBuilder.setView(promptsView)

            val userInput = promptsView
                .findViewById<View>(com.mobiversa.ezy2pay.R.id.editTextDialogUserInput) as EditText

            alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, id -> // get user input and set it to result
                    val phone = userInput.text.toString()
                    if (phone.isNotEmpty() && phone.length > 9) {
                        phoneNumber = "65" + userInput.text.toString()
                        jsonSendReceipt()
                        dialog.dismiss()
                        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    }
                }
                .setNegativeButton("Cancel") { dialog, id -> dialog.cancel() }

            val alertDialog: AlertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }
    }

    @SuppressLint("HandlerLeak")
    var printerHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.obj.toString()) {
                Constants.DeviceConnected -> {
                }
                Constants.PRINT_RECEIPT -> {
                    wisePadController.sendPrintData(printerReceipts[0])
                }
            }
        }
    }

    override fun onBackPressed() {
        if (!isFromCardPayment) {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        wisePadController.stopSerial()
    }

    private fun printReceiptData(isMerchantCopy: Boolean) {
        try {
            printerReceipts = java.util.ArrayList()
            printerReceipts.add(ReceiptUtility.genReceipt4(this, receiptData, isMerchantCopy))
            wisePadController.startPrint(1, 120)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
