package com.mobiversa.ezy2pay.ui.receipt

import android.annotation.SuppressLint
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import com.bbpos.simplyprint.SimplyPrintController
import com.mobiversa.ezy2pay.MainActivity
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.base.BaseActivity
import com.mobiversa.ezy2pay.utils.Constants
import com.mobiversa.ezy2pay.utils.Constants.Companion.isPrinterConnected
import com.mobiversa.ezy2pay.utils.Constants.Companion.printerfoundDevices
import kotlinx.android.synthetic.main.activity_printer.*
import org.json.JSONObject


class PrinterActivity : BaseActivity() {

    private var printerController: SimplyPrintController? = null
    protected var printerarrayAdapter: ArrayAdapter<String>? = null
    private var printerReceipts = ArrayList<ByteArray>()

    var receiptData: JSONObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_printer)

        supportActionBar?.title = "Print Receipt"
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        val data = intent.getStringExtra("receiptData")

        receiptData = JSONObject(data)

        Log.e("Test", "" + receiptData)

        cancelPrint.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        printerInit()
    }

    private fun printerInit() {

        printerarrayAdapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1)
        val mySimplyPrintControllerListener =
            MySimplyPrintControllerListener.getInstance(this, printerarrayAdapter, printerHandler)
        printerController = SimplyPrintController(this, mySimplyPrintControllerListener)

        if (!isPrinterConnected || !printerController!!.isDevicePresent) {
            promptForPrinterConnection()
        }

    }

    @SuppressLint("HandlerLeak")
    protected var printerHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            val status = msg.obj.toString()
            if (status.equals("printer connected", ignoreCase = false)) {
                Log.e("Printer Connected", "Success")
                printerController!!.setDarkness(0)
                showDialog("Loading")
            } else if (status.equals("darkness done", ignoreCase = false)) {
                cancelDialog()
                PrintReceiptData(false)
            } else if (status.contains("Printdata")) {
                val index = status.replace("Printdata", "").toInt()
                printerController!!.sendPrinterData(printerReceipts[index])
                showDialog("Loading")
            } else if (status.equals(
                    "Print completed",
                    ignoreCase = false
                )
            ) { // success receipt of printer data.
                if (printerstatus != null) {
                    (printerstatus as TextView).text = "Print Completed..."
                }
                cancelDialog()
            } else if (status.equals("no paper", ignoreCase = true)) {
                (printerstatus as TextView).text =
                    "Kindly feed paper and try again."
                printerButtons.setVisibility(View.VISIBLE)
            } else {
                (printerstatus as TextView).text = status
                printerButtons.setVisibility(View.VISIBLE)
            }
        }
    }

    fun PrintReceipt(button: View) {
        if (isPrinterConnected) {
            PrintReceiptData(button.id == R.id.printMerchant)
        } else {
            promptForPrinterConnection()
        }
    }

    fun PrintReceiptData(isMerchantCopy: Boolean) {
        try {
            printerReceipts = java.util.ArrayList()
            printerReceipts.add(ReceiptUtility.genReceipt(this, receiptData, isMerchantCopy))
            printerController!!.startPrinting(printerReceipts.size, 120, 120)
        } catch (e: Exception) {
            (printerstatus as TextView).text =
                "Unable to print, kindly try again later."
            printerButtons.visibility = View.VISIBLE
        }
    }

    private fun promptForPrinterConnection() {
        try {
            val pairedObjects: Array<Any> =
                BluetoothAdapter.getDefaultAdapter().bondedDevices.toTypedArray()
            val pairedDevices =
                arrayOfNulls<BluetoothDevice>(pairedObjects.size)
            for (i in pairedObjects.indices) {
                pairedDevices[i] = pairedObjects[i] as BluetoothDevice
            }
            val mArrayAdapter =
                ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)
            for (i in pairedDevices.indices) {
                mArrayAdapter.add(pairedDevices[i]!!.name)
            }
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.bluetooth_2_device_list_dialog)
            dialog.setTitle(R.string.bluetooth_devices)
            val listView =
                dialog.findViewById<View>(R.id.pairedDeviceList) as ListView
            listView.adapter = mArrayAdapter
            listView.onItemClickListener =
                OnItemClickListener { parent, view, position, id ->
                    printerController!!.startBTv2(pairedDevices[position])
                    dialog.dismiss()
                }
            val listView2 =
                dialog.findViewById<View>(R.id.discoveredDeviceList) as ListView
            listView2.adapter = printerarrayAdapter
            listView2.onItemClickListener =
                OnItemClickListener { parent, view, position, id ->
                    printerController?.startBTv2(printerfoundDevices?.get(position))
                    dialog.dismiss()
                }
            dialog.findViewById<View>(R.id.cancelButton)
                .setOnClickListener {
                    printerController!!.stopScanBTv2()
                    (printerstatus as TextView).text =
                        "Unable to print, kindly try again later."
                    printerButtons.visibility = View.VISIBLE
                    dialog.dismiss()
                }
            dialog.setCancelable(false)
            dialog.show()
            printerController!!.scanBTv2(Constants.DEVICE_NAMES, 120)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}
