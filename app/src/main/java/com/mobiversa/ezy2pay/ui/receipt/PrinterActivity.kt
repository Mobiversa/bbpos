package com.mobiversa.ezy2pay.ui.receipt

import android.annotation.SuppressLint
import android.content.Intent
import android.os.*
import android.util.Log
import android.view.View
import com.bbpos.bbdevice.BBDeviceController
import com.mobiversa.ezy2pay.MainActivity
import com.mobiversa.ezy2pay.base.BaseActivity
import com.mobiversa.ezy2pay.databinding.ActivityPrinterBinding
import com.mobiversa.ezy2pay.ui.ezyWire.MyBBPosController
import com.mobiversa.ezy2pay.utils.Constants
import kotlinx.android.synthetic.main.activity_printer.*
import org.json.JSONObject


class PrinterActivity : BaseActivity() {

    private var printerReceipts = ArrayList<ByteArray>()
    private lateinit var binding: ActivityPrinterBinding
    var receiptData: JSONObject? = null

    lateinit var wisePadController: BBDeviceController
    lateinit var listener: MyBBPosController

    private fun getBindingView(): View {
        binding = ActivityPrinterBinding.inflate(layoutInflater)
        return binding.root
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getBindingView())

        supportActionBar?.title = "Print Receipt"
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        val data = intent.getStringExtra("receiptData")

        receiptData = JSONObject(data)

        Log.e("Test", "" + receiptData)

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
    }

    private fun printerInit() {
        binding.printMerchantCopy.setOnClickListener {
            it.isEnabled = false
            printReceiptData(true)
        }
        binding.printCustomerCopy.setOnClickListener {
            it.isEnabled = false
            printReceiptData(false)
        }
        binding.paymentComplete.setOnClickListener {
            fragmentManager.popBackStack()
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

    private fun printReceiptData(isMerchantCopy: Boolean) {
        try {
            printerReceipts = java.util.ArrayList()
            printerReceipts.add(ReceiptUtility.genReceipt(this, receiptData, isMerchantCopy))
            wisePadController.startPrint(1, 120)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
