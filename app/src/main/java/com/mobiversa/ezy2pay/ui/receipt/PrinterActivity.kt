package com.mobiversa.ezy2pay.ui.receipt

import android.annotation.SuppressLint
import android.app.Activity
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
import com.mobiversa.ezy2pay.ui.history.CountryCodeActivity
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
            startActivityForResult(Intent(this@PrinterActivity, CountryCodeActivity::class.java), 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            binding.sendCustomerCopy.visibility = View.GONE
            isSendReceipt = true
            phoneNumber = data?.getStringExtra(Constants.PHONE).toString()
            // this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            jsonSendReceipt()
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
