package com.mobiversa.ezy2pay.ui.ezyWire

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import com.bbpos.bbdevice.BBDeviceController.*
import com.bbpos.bbdevice.CAPK
import com.mobiversa.ezy2pay.utils.Constants
import com.mobiversa.ezy2pay.utils.Constants.Companion.BBDeviceVersion
import com.mobiversa.ezy2pay.utils.Fields
import com.mobiversa.ezy2pay.utils.PreferenceHelper
import com.mobiversa.ezy2pay.utils.PreferenceHelper.set
import java.util.*


/**
 * Created by Sangavi on 28/11/2019.
 */
class MyBBPosController : BBDeviceControllerListener {
    private var isDeviceConnected = false
    private val isAmountAccpted = false
    var appList: ArrayList<String>? = null

    fun SendNotification(handlerMessage: String) {
        val message = myHandler!!.obtainMessage()
        message.obj = handlerMessage
        myHandler!!.sendMessage(message)
    }

    override fun onWaitingForCard(checkCardMode: CheckCardMode) {
        when (checkCardMode) {
            CheckCardMode.INSERT -> {
                Log.e("TEST ", "INSERT")
            }
            CheckCardMode.SWIPE -> {
                Log.e("TEST ", "SWIPE")
            }
            CheckCardMode.SWIPE_OR_INSERT -> {
                Log.e("TEST ", "SWIPE_OR_INSERT")
            }
            CheckCardMode.TAP -> {
                Log.e("TEST ", "TAP")
            }
            CheckCardMode.SWIPE_OR_TAP -> {
                Log.e("TEST ", "SWIPE_OR_TAP")
            }
            CheckCardMode.INSERT_OR_TAP -> {
                Log.e("TEST ", "INSERT_OR_TAP")
            }
            CheckCardMode.SWIPE_OR_INSERT_OR_TAP -> {
                Log.e("TEST ", "SWIPE_OR_INSERT_OR_TAP")
            }
            else -> {
            }
        }
    }

    override fun onWaitingReprintOrPrintNext() {}
    override fun onBTReturnScanResults(list: List<BluetoothDevice>) {
        Constants.foundDevices = list
        if (arrayAdapter != null) {
            arrayAdapter!!.clear()
            for (i in Constants.foundDevices!!.indices) {
                arrayAdapter!!.add(Constants.foundDevices!![i].name)

                Log.e("Found Device", Constants.foundDevices!![i].name)
            }
            arrayAdapter!!.notifyDataSetChanged()
        }
    }

    override fun onBTScanTimeout() {}
    override fun onBTScanStopped() {}
    override fun onBTConnected(bluetoothDevice: BluetoothDevice) {
        try {
            isDeviceConnected = true
            Constants.isDeviceConnected = true
            val prefs: SharedPreferences = PreferenceHelper.defaultPrefs(activity!!.applicationContext)
            prefs[Fields.DeviceId] = bluetoothDevice.name
            SendNotification(Constants.DeviceConnected)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isDeviceConnected() : Boolean{
        return isDeviceConnected
    }


    override fun onBTDisconnected() {
        isDeviceConnected = false
        Constants.isDeviceConnected = false
        Constants.batteryLevel = "Device Disconnected"
        SendNotification(Constants.DeviceDisconnected)
        SendNotification("battery")
    }

    override fun onUsbConnected() {}
    override fun onUsbDisconnected() {}
    override fun onSerialConnected() {}
    override fun onSerialDisconnected() {}
    override fun onReturnCheckCardResult(checkCardResult: CheckCardResult,decodeData: Hashtable<String, String>?) {
        Toast.makeText(activity,checkCardResult.toString() + "",Toast.LENGTH_SHORT).show()
//                SendNotification("Card Swiped");
// Log.v("swipe card tlv", decodeData.toString());
        Constants.isICC = "N"
        if (checkCardResult == CheckCardResult.NO_CARD) {
            Log.e("wisepad", "No card interseted")
        } else if (checkCardResult == CheckCardResult.INSERTED_CARD) {
            Constants.isICC = ""
            SendNotification(Constants.StartEMV)
        } else if (checkCardResult == CheckCardResult.NOT_ICC) {
            Log.e("wisepad", "Non ICC card inserted")
            SendNotification("Not ICC Card")
        } else if (checkCardResult == CheckCardResult.BAD_SWIPE) {
            Log.e("wisepad", "Bad swipe")
            SendNotification("Bad Swipe")
        } else if (checkCardResult == CheckCardResult.MSR) { // magstripe card is swiped
            val maskedPAN = decodeData?.get("maskedPAN")
            val PAN = decodeData?.get("PAN")
            Constants.PAN = if (PAN == "") maskedPAN.toString() else PAN.toString()
            Constants.ExpiryDate = decodeData?.get("expiryDate").toString()
            Constants.cardholderName = decodeData?.get("cardholderName").toString()
            val ksn = decodeData?.get("ksn")
            val serviceCode = decodeData?.get("serviceCode")
            Log.v("--serviceCode--", ""+serviceCode)
            val encTrack1 = decodeData?.get("encTrack1")
            val encTrack2 = decodeData?.get("encTrack2")
            Constants.TLV = "$encTrack1#$encTrack2"
            Log.v("--serviceCode", ""+serviceCode)
            if (serviceCode != null) {
                Constants.isSwipe = true
                SendNotification(Constants.EnterPIN)
            } else {
                SendNotification(Constants.CardCompleted)
            }
            /* if (serviceCode.endsWith("0") || serviceCode.endsWith("6")) {
                SendNotification(Constants.EnterPIN);
            } else {
                SendNotification(Constants.CardCompleted);
            }*/
        } else if (checkCardResult == CheckCardResult.TAP_CARD_DETECTED) {
            Constants.isICC = ""
            //            SendNotification("Start EMV");
        } else if (checkCardResult == CheckCardResult.USE_ICC_CARD) {
            val maskedPAN = decodeData?.get("maskedPAN")
            val PAN = decodeData?.get("PAN")
            Constants.PAN = if (PAN == "") maskedPAN.toString() else PAN.toString()
            Constants.ExpiryDate = decodeData?.get("expiryDate").toString()
            Constants.cardholderName = decodeData?.get("cardholderName").toString()
            val serviceCode = decodeData?.get("serviceCode")
            val encTrack1 = decodeData?.get("encTrack1")
            val encTrack2 = decodeData?.get("encTrack2")

            Log.e("Track1 ", ""+encTrack1)
            Log.e("Track2 ", ""+encTrack2)

            Constants.TLV = "$encTrack1#$encTrack2"
            if (serviceCode!!.endsWith("0") || serviceCode.endsWith("6")) {
                Constants.isSwipe = false
                SendNotification(Constants.EnterPIN)
            } else {
                SendNotification(Constants.CardCompleted)
            }
        } else {
            SendNotification("Invalid Card")
        }
    }

    @SuppressLint("LongLogTag")
    override fun onReturnCancelCheckCardResult(b: Boolean) {
        Log.v("onReturnCancelCheckCardResult", "")
        val cancelResult = b.toString()
        if (cancelResult.equals("true", ignoreCase = true)) {
            SendNotification("Payment Cancelled")
        }
    }

    override fun onReturnDeviceInfo(deviceInfoData: Hashtable<String, String>) {
        if (!deviceInfoData.isEmpty) {
            Constants.batteryLevel = deviceInfoData["batteryPercentage"].toString()
            val isSupportedTrack1 = deviceInfoData["isSupportedTrack1"]
            val isSupportedTrack2 = deviceInfoData["isSupportedTrack2"]
            val isSupportedTrack3 = deviceInfoData["isSupportedTrack3"]
            val bootloaderVersion = deviceInfoData["bootloaderVersion"]
            val firmwareVersion = deviceInfoData["firmwareVersion"]
            val mainProcessorVersion = deviceInfoData["mainProcessorVersion"]
            val coprocessorVersion = deviceInfoData["coprocessorVersion"]
            val coprocessorBootloaderVersion =
                deviceInfoData["coprocessorBootloaderVersion"]
            val isUsbConnected = deviceInfoData["isUsbConnected"]
            val isCharging = deviceInfoData["isCharging"]
            val batteryLevel = deviceInfoData["batteryLevel"]
            val batteryPercentage = deviceInfoData["batteryPercentage"]
            val hardwareVersion = deviceInfoData["hardwareVersion"]
            val productId = deviceInfoData["productId"]
            val pinKsn = deviceInfoData["pinKsn"]
            val emvKsn = deviceInfoData["emvKsn"]
            val trackKsn = deviceInfoData["trackKsn"]
            val terminalSettingVersion = deviceInfoData["terminalSettingVersion"]
            val deviceSettingVersion = deviceInfoData["deviceSettingVersion"]
            val formatID = deviceInfoData["formatID"]
            val vendorID = deviceInfoData["vendorID"]
            val csn = deviceInfoData["csn"]
            val uid = deviceInfoData["uid"]
            val serialNumber = deviceInfoData["serialNumber"]
            val modelName = deviceInfoData["modelName"]
            val macKsn = deviceInfoData["macKsn"]
            val nfcKsn = deviceInfoData["nfcKsn"]
            val messageKsn = deviceInfoData["messageKsn"]
            val bID = deviceInfoData["bID"]
            val publicKeyVersion = deviceInfoData["publicKeyVersion"]
            val firmwareID = deviceInfoData["firmwareID"]
            val bootloaderID = deviceInfoData["bootloaderID"]
            val backupBatteryVoltage = deviceInfoData["backupBatteryVoltage"]
            val btRssi = deviceInfoData["rssi"]
            //            infoListener.callback("Testing");
            val editor = pref!!.edit()
            editor.putString("firmwareVersion_BBP", firmwareVersion) // Saving string
            editor.putString("deviceSettingVersion_BBP", deviceSettingVersion)
            editor.apply()
            BBDeviceVersion = firmwareVersion.toString()
        }
        // device information
    }

    override fun onReturnTransactionResult(transactionResult: TransactionResult) {
        Log.v("transactionResult", transactionResult.toString())
        val approved = false
        /*if (transactionResult == BBDeviceController.TransactionResult.APPROVED) {
            //Log.v("result!!", transactionResult.toString());
            approved = true;
            SendNotification(transactionResult.toString());
        } else if (transactionResult == BBDeviceController.TransactionResult.TERMINATED) {
            //Log.v("result!!", transactionResult.toString());
            SendNotification(transactionResult.toString());
        } else if (transactionResult == BBDeviceController.TransactionResult.DECLINED) {
            //Log.v("result!!", transactionResult.toString());
            SendNotification(transactionResult.toString());
        }*/
        SendNotification(transactionResult.toString())
        Log.v("result_traxn", transactionResult.toString())
        //SendNotification(!isAmountAccpted ? "Amount Declined" : (approved ? Constants.CardCompleted : "Card Declined"));
    }

    override fun onReturnBatchData(s: String) {}
    override fun onReturnReversalData(s: String) {
        SendNotification("Reversal Data")
    }

    override fun onReturnAmountConfirmResult(b: Boolean) {}
    override fun onReturnPinEntryResult(pinEntryResult: PinEntryResult,data: Hashtable<String, String>?) {
        Log.v("pinenter", pinEntryResult.toString())
        Constants.isPinVerified = false
        //        Log.e("Constants.PIN_DATA", data.toString() + "");
        if (pinEntryResult == PinEntryResult.ENTERED) {
            Log.v("pin", pinEntryResult.toString())
            Constants.isPinVerified = true
            if (data != null) {
                if (data.isNotEmpty()) {
                    Constants.PIN_DATA = data["epb"].toString()
                    if (Constants.isICC.equals("N", false)) {
                        SendNotification(Constants.CardCompleted)
                    }
                }
            }
        } else if (pinEntryResult == PinEntryResult.BYPASS) {
            SendNotification(Constants.CardCompleted)
        } else if (pinEntryResult == PinEntryResult.INCORRECT_PIN) {
            SendNotification(Constants.IncorrectPin)
        } else {
            Log.v("pin", pinEntryResult.toString())
            Constants.PIN_DATA = ""
            /*if (Constants.isICC.equalsIgnoreCase("n")) {
                SendNotification(Constants.CardCompleted);
            }*/SendNotification("isPinCanceled")
        }
    }

    override fun onReturnPrintResult(printResult: PrintResult) {
        Log.v("--onReturnPrintResult--", "")
    }

    @SuppressLint("LongLogTag")
    override fun onReturnAccountSelectionResult(accountSelectionResult: AccountSelectionResult,i: Int) {
        Log.v("--onReturnAccountSelectionResult--", "")
    }

    override fun onReturnAmount(hashtable: Hashtable<String, String>) {
        Log.v("--onReturnAmount--", "")
    }

    @SuppressLint("LongLogTag")
    override fun onReturnUpdateAIDResult(hashtable: Hashtable<String, TerminalSettingStatus>) {
        Log.v("--onReturnUpdateAIDResult--", "")
    }

    override fun onReturnUpdateGprsSettingsResult(b: Boolean,hashtable: Hashtable<String, TerminalSettingStatus>) {
    }

    override fun onReturnUpdateTerminalSettingResult(terminalSettingStatus: TerminalSettingStatus) {}
    override fun onReturnUpdateWiFiSettingsResult(b: Boolean,hashtable: Hashtable<String, TerminalSettingStatus>) {
    }

    override fun onReturnReadAIDResult(hashtable: Hashtable<String, Any>) {}
    override fun onReturnReadGprsSettingsResult(b: Boolean,hashtable: Hashtable<String, Any>) {
    }

    override fun onReturnReadTerminalSettingResult(hashtable: Hashtable<String, Any>) {}
    /*  @Override
    public void onReturnReadTerminalSettingResult(BBDeviceController.TerminalSettingStatus terminalSettingStatus, String s) {
    }*/
    override fun onReturnReadWiFiSettingsResult(b: Boolean,hashtable: Hashtable<String, Any>) {
    }

    override fun onReturnEnableAccountSelectionResult(b: Boolean) {}
    override fun onReturnEnableInputAmountResult(b: Boolean) {}
    override fun onReturnCAPKList(list: List<CAPK>) {}
    override fun onReturnCAPKDetail(capk: CAPK) {}
    override fun onReturnCAPKLocation(s: String) {}
    override fun onReturnUpdateCAPKResult(b: Boolean) {}
    override fun onReturnRemoveCAPKResult(b: Boolean) {}
    override fun onReturnEmvReportList(hashtable: Hashtable<String, String>) {}
    override fun onReturnEmvReport(s: String) {
        Log.v("--onReturnEmvReport--", s)
    }

    override fun onReturnDisableAccountSelectionResult(b: Boolean) {}
    override fun onReturnDisableInputAmountResult(b: Boolean) {}
    override fun onReturnPhoneNumber(phoneEntryResult: PhoneEntryResult,s: String) {
    }

    override fun onReturnEmvCardDataResult(b: Boolean,s: String) {
        Log.v("tlv", s)
    }

    override fun onReturnEmvCardNumber(b: Boolean, cardNumber: String) {
        Constants.PAN = cardNumber
    }

    @SuppressLint("LongLogTag")
    override fun onReturnEncryptPinResult(b: Boolean,hashtable: Hashtable<String, String>) {
    }

    override fun onReturnEncryptDataResult(b: Boolean,hashtable: Hashtable<String, String>) {
    }

    override fun onReturnInjectSessionKeyResult(isSuccess: Boolean,s: Hashtable<String, String>) {
        SendNotification(if (isSuccess) "InjectSessionSuccess" else "InjectSessionFail")
    }

    override fun onReturnPowerOnIccResult(b: Boolean,s: String, s1: String,i: Int) {
    }

    override fun onReturnPowerOffIccResult(b: Boolean) {}
    override fun onReturnApduResult( b: Boolean, hashtable: Hashtable<String, Any>) {
    }

    override fun onRequestSelectApplication(appLists: ArrayList<String>) {
        appList = appLists
        SendNotification("select application")
    }

    override fun onRequestSetAmount() {
        SendNotification(Constants.SetAmount)
    }

    override fun onRequestPinEntry(pinEntrySource: PinEntrySource) {
        Log.v("wisepad Request", pinEntrySource.toString())
    }

    override fun onRequestOnlineProcess(tlv: String) {
        Constants.TLV = tlv
        SendNotification(Constants.OnlineProcess)
    }

    override fun onRequestTerminalTime() {}
    override fun onRequestDisplayText(displayText: DisplayText) {
        Log.v("pinverify", displayText.toString())

        if (displayText.toString().equals(Constants.EnterPIN, false)){
            SendNotification(Constants.PinScreen)
        }
    }

    override fun onRequestDisplayAsterisk(i: Int) {}
    override fun onRequestDisplayLEDIndicator(contactlessStatus: ContactlessStatus) {}
    override fun onRequestProduceAudioTone(contactlessStatusTone: ContactlessStatusTone) {}
    override fun onRequestClearDisplay() {}
    override fun onRequestFinalConfirm() {
        SendNotification("Final Confirm")
    }

    override fun onRequestPrintData(i: Int, b: Boolean) {}
    override fun onPrintDataCancelled() {}
    override fun onPrintDataEnd() {}
    override fun onBatteryLow(batteryStatus: BatteryStatus) {}
    override fun onAudioDevicePlugged() {}
    override fun onAudioDeviceUnplugged() {}
    override fun onError(errorState: Error,errorMessage: String) {
        Log.e("wisepad errorMessage",errorMessage + "::" + errorState + ":: " + errorState.name)
        SendNotification("Error::$errorMessage")
    }

    override fun onSessionInitialized() {}
    override fun onSessionError(sessionError: SessionError, s: String) {}
    override fun onAudioAutoConfigProgressUpdate(v: Double) {}
    override fun onAudioAutoConfigCompleted(b: Boolean,s: String) {
    }

    override fun onAudioAutoConfigError(audioAutoConfigError: AudioAutoConfigError) {}
    override fun onNoAudioDeviceDetected() {}
    override fun onDeviceHere(b: Boolean) {}
    override fun onPowerDown() {}
    override fun onPowerButtonPressed() {}
    override fun onDeviceReset() {}
    override fun onEnterStandbyMode() {}
    override fun onReturnNfcDataExchangeResult(b: Boolean,hashtable: Hashtable<String, String>) {
    }

    override fun onReturnNfcDetectCardResult(nfcDetectCardResult: NfcDetectCardResult,hashtable: Hashtable<String, Any>) {
    }

    override fun onReturnControlLEDResult(b: Boolean,s: String) {
    }

    override fun onReturnVasResult(vasResult: VASResult,hashtable: Hashtable<String, Any>) {
    }

    override fun onRequestStartEmv() {}
    override fun onDeviceDisplayingPrompt() {}
    override fun onRequestKeypadResponse() {}
    override fun onReturnDisplayPromptResult(displayPromptResult: DisplayPromptResult) {}
    override fun onBarcodeReaderConnected() {}
    override fun onBarcodeReaderDisconnected() {}
    override fun onReturnBarcode(s: String) {} //    @Override

    //    public void onBTRequestPairing() {
//        Log.e("BlueTooth","Request Pairing");
//    }
//
//    @Override
//    public void onReturnSetPinPadButtonsResult(boolean isSuccess) {
////        dismissDialog();
//    }
//
//    @Override
//    public void onReturnFunctionKey(BBDeviceController.FunctionKey funcKey) {
//        Log.e("BlueTooth","Function Key "+ funcKey);
////        setStatus(getString(R.string.function_key) + " : " + funcKey);
////        dismissDialog();
//    }
    companion object {
        private var activity: Context? = null
        private var arrayAdapter: ArrayAdapter<String>? = null
        private var myHandler: Handler? = null
        private var wisePadListener: MyBBPosController? = null
        var pref: SharedPreferences? = null
        fun getInstance(baseActivity: Context, baseArrayAdapter: ArrayAdapter<String>?, myBaseHandler: Handler?): MyBBPosController? {
            activity = baseActivity
            arrayAdapter = baseArrayAdapter
            myHandler = myBaseHandler
            pref = baseActivity.getSharedPreferences("Version", Context.MODE_PRIVATE)
            if (wisePadListener == null) {
                wisePadListener = MyBBPosController()
            }
            return wisePadListener
        }
    }
}