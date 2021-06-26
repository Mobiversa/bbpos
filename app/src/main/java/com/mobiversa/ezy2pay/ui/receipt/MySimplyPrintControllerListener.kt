package com.mobiversa.ezy2pay.ui.receipt

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Handler
import android.util.Log
import android.widget.ArrayAdapter
import com.bbpos.simplyprint.SimplyPrintController
import com.bbpos.simplyprint.SimplyPrintController.PrinterResult
import com.bbpos.simplyprint.SimplyPrintController.SimplyPrintControllerListener
import com.mobiversa.ezy2pay.utils.Constants.Companion.isPrinterConnected
import com.mobiversa.ezy2pay.utils.Constants.Companion.printerfoundDevices
import java.util.*


/**
 * Created by sampath_k on 22/09/15.
 */
class MySimplyPrintControllerListener : SimplyPrintControllerListener {
    private var noPaper = false
    private fun SendNotification(handlerMessage: String) {
        val message =
            myHandler!!.obtainMessage()
        message.obj = handlerMessage
        myHandler!!.sendMessage(message)
    }

    override fun onBTv2Detected() { //        appendStatus(getString(R.string.bluetooth_2_detected));
    }

    override fun onBTv2DeviceListRefresh(foundDevices: List<BluetoothDevice>) {
        printerfoundDevices = foundDevices
        if (arrayAdapter != null) {
            arrayAdapter!!.clear()
            for (i in foundDevices.indices) {
                arrayAdapter!!.add(foundDevices[i].name)
            }
            arrayAdapter!!.notifyDataSetChanged()
        }
    }

    override fun onBTv2Connected(bluetoothDevice: BluetoothDevice) {
        Log.e("wisepad printer", "printer connected")
        isPrinterConnected = true
        SendNotification("printer connected")
    }

    override fun onBTv2Disconnected() {
        Log.e("wisepad printer", "printer dis-connected")
        isPrinterConnected = false
    }

    override fun onBTv2ScanStopped() { // Scan stoped
    }

    override fun onBTv2ScanTimeout() { // printer scan timeout :: dismiss dialog if needed
    }

    override fun onBTv4DeviceListRefresh(foundDevices: List<BluetoothDevice>) {
       printerfoundDevices = foundDevices
        if (arrayAdapter != null) {
            arrayAdapter!!.clear()
            for (i in foundDevices.indices) {
                arrayAdapter!!.add(foundDevices[i].name)
            }
            arrayAdapter!!.notifyDataSetChanged()
        }
    }

    override fun onBTv4Connected() { // btv4;
    }

    override fun onBTv4Disconnected() { //
    }

    override fun onBTv4ScanStopped() { //
    }

    override fun onBTv4ScanTimeout() { //
    }

    override fun onReturnDeviceInfo(deviceInfoTable: Hashtable<String, String>) {
        val productId = deviceInfoTable["productId"]
        val firmwareVersion = deviceInfoTable["firmwareVersion"]
        val bootloaderVersion = deviceInfoTable["bootloaderVersion"]
        val hardwareVersion = deviceInfoTable["hardwareVersion"]
        val isUsbConnected = deviceInfoTable["isUsbConnected"]
        val isCharging = deviceInfoTable["isCharging"]
        val batteryLevel = deviceInfoTable["batteryLevel"]
        Log.e("Wisepad Printer", deviceInfoTable.toString() + "")
    }

    override fun onReturnPrinterResult(printerResult: PrinterResult) {
        when (printerResult) {
            PrinterResult.SUCCESS -> { // success
            }
            PrinterResult.NO_PAPER -> {
                SendNotification("No Paper")
                noPaper = true
            }
            PrinterResult.WRONG_CMD -> { // incorrect command
            }
            PrinterResult.OVERHEAT -> { // over heat
            }
        }
    }

    override fun onReturnGetDarknessResult(value: Int) { // darkness result
        Log.e("wisepad printer", "darkness  $value")
    }

    override fun onReturnSetDarknessResult(isSuccess: Boolean) {
        Log.e("wisepad printer", "darkness result $isSuccess")
        SendNotification("darkness done")
    }

    override fun onRequestPrinterData(
        index: Int,
        isReprint: Boolean
    ) { //        controller.sendPrinterData(receipts.get(index));
// handler to send print data
        SendNotification("Printdata$index")
    }

    override fun onPrinterOperationEnd() { //printer completed
        SendNotification("Print completed")
    }

    override fun onBatteryLow(batteryStatus: SimplyPrintController.BatteryStatus) { //
    }

    override fun onBTv2DeviceNotFound() { // handler to indicate the device unavailability
    }

    override fun onError(errorState: SimplyPrintController.Error) {
        Log.e("wisepad printer", "error$errorState")
        if (!noPaper) {
            if (errorState == SimplyPrintController.Error.UNKNOWN) {
                SendNotification("Kindly check printer and try again.")
            } else if (errorState == SimplyPrintController.Error.CMD_NOT_AVAILABLE) {
                SendNotification("Command not available, please contact Ezywire.")
            } else if (errorState == SimplyPrintController.Error.TIMEOUT) {
                SendNotification("Printer timmed out please try again.")
            } else if (errorState == SimplyPrintController.Error.DEVICE_BUSY) {
                SendNotification("Printer busy please try again.")
            } else if (errorState == SimplyPrintController.Error.INPUT_OUT_OF_RANGE) {
                SendNotification("Printer input out of range..")
            } else if (errorState == SimplyPrintController.Error.INPUT_INVALID) {
                SendNotification("Invalid input.")
            } else if (errorState == SimplyPrintController.Error.CRC_ERROR) {
                SendNotification("CRS error please try again.")
            } else {
                SendNotification("Error on connecting printer.")
            } /*if (errorState == SimplyPrintController.Error.FAIL_TO_START_BTV2) {
            } else if (errorState == SimplyPrintController.Error.COMM_LINK_UNINITIALIZED) {
            } else if (errorState == SimplyPrintController.Error.BTV2_ALREADY_STARTED) {
            }*/
        }
        noPaper = false
    }

    companion object {
        private var activity: Context? = null
        private var arrayAdapter: ArrayAdapter<String>? = null
        private var myHandler: Handler? = null
        private var printerListener: MySimplyPrintControllerListener? = null
        fun getInstance(
            baseActivity: Context?, baseArrayAdapter: ArrayAdapter<String>?
            , myBaseHandler: Handler?
        ): MySimplyPrintControllerListener? {
            activity = baseActivity
            arrayAdapter = baseArrayAdapter
            myHandler = myBaseHandler
            if (printerListener == null) {
                printerListener =
                    MySimplyPrintControllerListener()
            }
            return printerListener
        }
    }
}