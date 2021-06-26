package com.mobiversa.ezy2pay.ui.ezyWire

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.*
import android.text.Selection
import android.util.Base64
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.databinding.library.BuildConfig
import androidx.lifecycle.ViewModelProviders
import com.bbpos.bbdevice.BBDeviceController
import com.bbpos.bbdevice.BBDeviceController.CurrencyCharacter
import com.mobiversa.ezy2pay.MainActivity
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.base.BaseActivity
import com.mobiversa.ezy2pay.network.ApiService
import com.mobiversa.ezy2pay.network.response.KeyInjectModel
import com.mobiversa.ezy2pay.network.response.PaymentInfoModel
import com.mobiversa.ezy2pay.ui.receipt.PrintReceiptFragment
import com.mobiversa.ezy2pay.utils.Constants
import com.mobiversa.ezy2pay.utils.Fields
import com.mobiversa.ezy2pay.utils.Fields.Companion.InvoiceId
import com.mobiversa.ezy2pay.utils.LocationService
import kotlinx.android.synthetic.main.activity_ezy_wire.*
import retrofit2.Call
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.security.Key
import java.security.KeyStore
import java.util.*
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.HashMap
import kotlin.experimental.and


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class EzyWireActivity : BaseActivity(), View.OnClickListener {

    private var cancelTracking: Int = 0
    private var pinEntry: String = ""
    private var SWIPE_PINDATA: String = ""
    private val ALGO = "AES"
    private var preAuth: Boolean = false
    private var isEzyrecSale: Boolean = false
    private lateinit var deviceStatus: String
    private var totalPrice: Double = 0.0
    lateinit var wisePadController: BBDeviceController
    lateinit var listener: MyBBPosController
    lateinit var arrayAdapter: ArrayAdapter<String>

    private lateinit var ezyWireViewModel: EzyWireViewModel
    //Next KeyInjection
    private var encryptedPinSessionKey = ""
    private var pinKcv: String? = ""
    private var encryptedDataSessionKey: String? = ""
    private var dataKcv: String? = ""
    private var encryptedTrackSessionKey: String? = ""
    private var trackKcv: String? = ""
    private var encryptedMacSessionKey = ""
    private var macKcv: String? = ""

    private val printReceiptFragment = PrintReceiptFragment()
    private val deliverApiService = ApiService.serviceRequest()

    var service : String? = ""
    var amount : String? = ""
    var invoiceId : String? = ""

    var isKeyInjected = false

    var sessionId = ""
    var tid = ""
    var hostType = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ezy_wire)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        if(resources.getBoolean(R.bool.portrait_only)){
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }

        ezyWireViewModel = ViewModelProviders.of(this@EzyWireActivity)[EzyWireViewModel::class.java]

        if (intent != null) {
            service = intent.getStringExtra(Fields.Service)
            amount = intent.getStringExtra(Fields.Amount)
            invoiceId = intent.getStringExtra(InvoiceId)
        }

        initialize()

        LocationService.init(this)
    }

    @SuppressLint("ObsoleteSdkInt", "SetTextI18n")
    private fun initialize() {
        Constants.isICC = ""

        deviceStatus = getLoginResponse(applicationContext).deviceStatus

        sessionId = getLoginResponse(applicationContext).sessionId
        tid = getLoginResponse(applicationContext).tid
        hostType = getLoginResponse(applicationContext).hostType

        amount_txt_ezywire.text = "RM $amount"
        insert_card_amount_txt.text = "RM $amount"
        enter_pin_amount_txt.text = "RM $amount"
        sign_amt_txt.text = "RM $amount"
        connect_bluetooth_txt.setOnClickListener(this)
        failure_try_again_btn.setOnClickListener(this)
        btn_sign_clear.setOnClickListener(this)
        btn_sign_payment.setOnClickListener(this)
        success_print_btn.setOnClickListener(this)

        arrayAdapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1)
        listener = MyBBPosController.getInstance(this, arrayAdapter, handler)!!
        wisePadController = BBDeviceController.getInstance(this, listener)

        if (Build.VERSION.SDK_INT > 9) {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }
    }


    private fun connectDevice() {
        if (deviceStatus.equals("ACTIVE", ignoreCase = true)) {
            if (service.equals(Fields.START_PAY, true)){
                preAuth = false
            }else if (service.equals(Fields.PRE_AUTH, true)){
                preAuth = true
            }

            totalPrice = amount?.toDouble() ?: 0.00
            if (!listener.isDeviceConnected()) {
                promptForConnection()
            } else {
                device_name_txt.text = getSharedString(Fields.DeviceId, applicationContext)
                enter_pin_device_name_txt.text = getSharedString(Fields.DeviceId, applicationContext)
                swipeCard()
            }
        } else {
            showDialog("EZYWIRE", "Your EZYWIRE subscription expired")
        }
    }

    private fun promptForConnection() {
        val pairedObjects: Array<Any> =
            BluetoothAdapter.getDefaultAdapter().bondedDevices.toTypedArray()
        val pairedDevices = arrayOfNulls<BluetoothDevice>(pairedObjects.size)
        for (i in pairedObjects.indices) {
            pairedDevices[i] = pairedObjects[i] as BluetoothDevice
        }
        val mArrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)
        for (i in pairedDevices.indices) {
            mArrayAdapter.add(pairedDevices[i]!!.name)
        }
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.bluetooth_2_device_list_dialog)
        dialog.setTitle(R.string.bluetooth_devices)
        val listView1 = dialog.findViewById<View>(R.id.pairedDeviceList) as ListView
        listView1.adapter = mArrayAdapter
        listView1.onItemClickListener =
            OnItemClickListener { parent, view, position, id ->
                dialog.dismiss()
                Constants.connectedDevice = pairedDevices[position]
                wisePadController.connectBT(pairedDevices[position])
                showDialog("Loading")
            }
        val listView2 =
            dialog.findViewById<View>(R.id.discoveredDeviceList) as ListView
        listView2.adapter = arrayAdapter
        listView2.onItemClickListener =
            OnItemClickListener { parent, view, position, id ->
                if (Constants.foundDevices!!.size>position){
                    wisePadController.connectBT(Constants.foundDevices?.get(position))
                    dialog.dismiss()
                    showDialog("Loading")
                }

            }
        dialog.findViewById<View>(R.id.cancelButton)
            .setOnClickListener {
                wisePadController.disconnectBT()
                if (isEzyrecSale) {
                    startActivity(
                        Intent(
                            applicationContext, MainActivity::class.java)
                    )
                } else {
                    dialog.dismiss()
                }
            }
        dialog.setCancelable(false)
        dialog.show()
        wisePadController.startBTScan(Constants.DEVICE_NAMES, 120)
    }

    var handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val status = msg!!.obj.toString()
            checkStatus(status)
        }
    }

    fun checkStatus(status: String) {

        Log.v("Status ", status)

        when (status) {
            Constants.DeviceConnected -> {
                cancelDialog()
                Log.e("Device Connected", "Success")
                wisePadController.getDeviceInfo()
                device_name_txt.text = getSharedString(Fields.DeviceId, applicationContext)
                enter_pin_device_name_txt.text = getSharedString(Fields.DeviceId, applicationContext)
                jsonKeyInjection()//KeyInjection --> For Live
            }
            Constants.InjectSessionSuccess -> {
                shortToast(status)
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                isKeyInjected = true
                nextKeyInjection()
            }
            Constants.InjectSessionFail -> {
                cancelDialog()
                /*For Live*/
                if (!BuildConfig.DEBUG) {
                    wisePadController.disconnectBT()
                }
                isKeyInjected = false
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                /*For Demo*/
//                nextKeyInjection()

                shortToast(status)
            }
            Constants.CardSwiped -> {
                Log.e("Test ","Card Swiped")
                showHideLayout(Constants.EnterPin, status)
            }
            Constants.PinScreen -> {
                showHideLayout(Constants.EnterPin, status)
            }
            Constants.StartEMV -> {
                Log.e("Test ","Start EMV")
                showHideLayout(Constants.EnterPin, status)
                startEMVProcess()
            }
            Constants.SwipeProcess ->{
                showServiceCharge()
            }
            Constants.EnterPIN -> {
                showHideLayout(Constants.EnterPin, status)
                val data = Hashtable<String, Any>()
                data["pinEntryTimeout"] = 120
                data["randomNumber"] = "0123456789ABCDEF"
                wisePadController.startPinEntry(data)
            }
            Constants.SetAmount -> {
                promptForAmount(totalPrice.toString())
            }
            Constants.SelectApplication -> {
                chooseApplication()
            }
            Constants.FinalConfirm -> {
                wisePadController.sendFinalConfirmResult(true)
            }
            Constants.OnlineProcess -> {
                Log.e("TEST", Constants.OnlineProcess)
                if (isOnline(applicationContext)) {
                    sendPaymentInformation()
                } else {
                    networkError(applicationContext)
                }
            }
            Constants.CardCompleted -> {
                Log.d("EZYWIRE", " CardCompleted")
                Log.e("IsPin", ""+Constants.isPinVerified)
                if (Constants.isICC.isEmpty()) {
                    if (Constants.isPinVerified) {
                        Constants.Signature = ""
                        Constants.isPinVerified = false
                        showHideLayout(Constants.SuccessTransaction, Constants.CardCompleted)
                    } else {
                        getSignature("RM " + String.format("%.2f", totalPrice))
                    }
                } else {
                    if (isOnline(applicationContext)) {
                        Log.e("TEST", Constants.CardCompleted)
                        if (Constants.isSwipe)
                            showServiceCharge()
                        else
                            sendPaymentInformation()
                    } else {
                        networkError(applicationContext)
                    }
                }
            }
            Constants.APPROVED -> {
                callAcknowledgementAPI(Constants.TRANS_ID)
            }
            Constants.PIN_CANCELLED -> {
                transactionFailed("Transaction cancelled")
            }
            Constants.PaymentCancelled -> {
//                wisePadController.cancelPinEntry()
                transactionFailed("Transaction cancelled")
            }
            Constants.TERMINATED -> {
                callAcknowledgementAPI(Constants.TRANS_ID)
                if (Constants.TRANS_ID.isNotEmpty()){
                    callAcknowledgementAPI(Constants.TRANS_ID)
                }else{
                    transactionFailed("Transaction Terminated")
                }
//                transactionFailed("Transaction Terminated")
            }
            Constants.DECLINED -> {
                transactionFailed("Transaction Declined")
                if (Constants.TRANS_ID.isNotEmpty()) {
                    Log.v("test", "inside cond")
                    sendDeclinedNotification()
                }
            }
            Constants.CANCEL_OR_TIMEOUT -> {
                transactionFailed("Device Timeout")
            }
            Constants.NOT_ICC -> {
                transactionFailed("A non-chip card is inserted")
            }
            Constants.CARD_BLOCKED -> {
                transactionFailed("Card is blocked")
            }
            Constants.DEVICE_ERROR -> {
                transactionFailed("Device error")
            }
            Constants.ICC_CARD_REMOVED -> {
                transactionFailed("Unexpected card removal during transaction")
                if (Constants.TRANS_ID.isNotEmpty()) {
                    Log.v("test", "inside cond")
                    sendDeclinedNotification()
                }
            }
            Constants.AmountDeclined -> {
                transactionFailed("Transaction amount declined")
            }
            Constants.IsPinCanceled -> {
                //wisePadController.cancelPinEntry();
                transactionFailed("Transaction Declined")
            }
            Constants.IncorrectPin -> {
                Log.v("wrong pin", "")
            }
            Constants.CardDeclined -> {
                transactionFailed("Transaction declined by card")
                if (Constants.TRANS_ID.isNotEmpty()) {
                    sendDeclinedNotification()
                }
            }
            Constants.DeviceDisconnected -> {
//                cancelDialog()
                if (isKeyInjected) {
                    transactionFailed("Device disconnected, please connect and try again for new transactions.")
                    if (Constants.TRANS_ID.isNotEmpty()) {
                        sendDeclinedNotification()
                    }
                }
            }
            Constants.Error -> {
                Log.e("EZYWIRE", Constants.Error)
            }
            Constants.NotICCCard, Constants.BadSwipe, Constants.InvalidCard -> {
                val message = "$status, please try again with valid Card."
                transactionFailed(message)
            }
            Constants.ReversalData -> {
                if (Constants.TRANS_ID.isNotEmpty()) {
                    sendDeclinedNotification()
                }
            }
        }
    }

    private fun jsonKeyInjection() {

        showDialog("KeyInjection")

        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        val keyParams: HashMap<String, String> = HashMap()
        keyParams[Fields.Service] = Fields.KeyInjection
        keyParams[Fields.sessionId] = getLoginResponse(applicationContext).sessionId
        keyParams[Fields.appVersionNum] = "5.4"
        keyParams[Fields.DeviceId] = getSharedString(Fields.DeviceId, applicationContext)
        keyParams[Fields.tid] = getLoginResponse(applicationContext).tid
        keyParams[Fields.HostType] = getLoginResponse(applicationContext).hostType

        deliverApiService.run {
            getKeyInjection(keyParams).enqueue(object : retrofit2.Callback<KeyInjectModel> {
                override fun onFailure(call: Call<KeyInjectModel>, t: Throwable) {
                    Log.e("EzywireModel ", t.message!!)
                }
                override fun onResponse(call: Call<KeyInjectModel>,response: Response<KeyInjectModel>) {
                    if (response.isSuccessful) {
                        val it = response.body()!!
                        if (it.responseCode.equals("0000", true)) {
                            cancelDialog()
                            encryptedPinSessionKey = it.responseData.TPK
                            pinKcv = it.responseData.TPKKCV
                            encryptedDataSessionKey = ""
                            dataKcv = ""
                            encryptedTrackSessionKey = it.responseData.TEK
                            trackKcv = it.responseData.TEKKCV
                            encryptedMacSessionKey = it.responseData.MEK
                            macKcv = it.responseData.MEKKCV

                            nextKeyInjection()

                        }else{
                            cancelDialog()
                            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                            showDialog("EZYWIRE WARNING", it.responseDescription)
                            isKeyInjected = false
                            wisePadController.disconnectBT()
                        }
                    } else {
                        Log.e("EzywireModel ", response.message())
                    }
                }
            })
        }

    }

    private fun nextKeyInjection() {

        if (pinKcv != "" && !pinKcv.equals("000000", ignoreCase = true)) {
            val data = Hashtable<String, String>()
            data["index"] = "1"
            data["encSK"] = encryptedPinSessionKey
            data["kcv"] = pinKcv
            pinKcv = ""
            wisePadController.injectSessionKey(data)
            return
        } else if (dataKcv != "" && !dataKcv.equals("000000", ignoreCase = true)) {
            val data = Hashtable<String?, String?>()
            data["index"] = "2"
            data["encSK"] = encryptedDataSessionKey
            data["kcv"] = dataKcv
            dataKcv = ""
            wisePadController.injectSessionKey(data)
            return
        } else if (trackKcv != "" && !trackKcv.equals("000000", ignoreCase = true)) {
            val data = Hashtable<String?, String?>()
            data["index"] = "3"
            data["encSK"] = encryptedTrackSessionKey
            data["kcv"] = trackKcv
            trackKcv = ""
            wisePadController.injectSessionKey(data)
            return
        } else if (macKcv != "" && !macKcv.equals("000000", ignoreCase = true)) {
            val data = Hashtable<String?, String?>()
            data["index"] = "4"
            data["encSK"] = encryptedMacSessionKey
            data["kcv"] = macKcv
            macKcv = ""
            wisePadController.injectSessionKey(data)
            return
        } else {
            cancelDialog()
            showDialog("Loading...")
            val handler = Handler()
            handler.postDelayed({
                //Do something after 100ms
                cancelDialog()
                swipeCard()
            }, 3000)
        }

    }

    private fun swipeCard() {
        showHideLayout(Constants.InsertCard, "Swipe Card")

        cancelTracking = 1

        val device_id: String = getSharedString(Fields.DeviceId, applicationContext)
        if (device_id.startsWith("WPC")) {
            val data = Hashtable<String, Any>()
            data["checkCardMode"] = BBDeviceController.CheckCardMode.SWIPE_OR_INSERT_OR_TAP
            Log.v("--wisepad2--", "--startEmv--")
            wisePadController.startEmv(data)
        } else {
            val data = Hashtable<String, Any>()
            data["checkCardMode"] = BBDeviceController.CheckCardMode.SWIPE_OR_INSERT
            Log.v("--wisepad1--", "--checkcard--")
            wisePadController.checkCard(data)
        }

    }

    private fun startEMVProcess() {
        val data = Hashtable<String, Any>()
        data["emvOption"] = BBDeviceController.EmvOption.START
        data["checkCardMode"] = BBDeviceController.CheckCardMode.SWIPE_OR_INSERT
        wisePadController.startEmv(data)
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    private fun showServiceCharge() {
        lateinit var mAlertDialog: AlertDialog

        val inflater = layoutInflater
        val alertLayout: View = inflater.inflate(R.layout.alert_swipe, null)
        val btnConfirm = alertLayout.findViewById<View>(R.id.button_confirm) as Button
        val btnCancel = alertLayout.findViewById<View>(R.id.button_cancel) as Button
        val alert: AlertDialog.Builder = AlertDialog.Builder(this)
        alert.setView(alertLayout)
        alert.setCancelable(false)
        Constants.isSwipe = false

        btnConfirm.setOnClickListener {
            sendPaymentInformation()
            mAlertDialog.dismiss()
        }

        btnCancel.setOnClickListener {
            transactionFailed("Transaction cancelled")
            mAlertDialog.dismiss()
        }

        mAlertDialog = alert.create()
        mAlertDialog.show()
    }


    private fun promptForAmount(price: String) {

        cancelTracking = 2

        Log.e("wisepad", "promptForAmount success  $price")
        val currencyCharacters = arrayOf(CurrencyCharacter.R, CurrencyCharacter.M)
        if (wisePadController.setAmount(
                price, "0.00", "458",
                BBDeviceController.TransactionType.GOODS, currencyCharacters
            )
        ) {
            Log.e("wisepad", "promptForAmount success$price")
        }
    }

    private fun chooseApplication() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.application_dialog)
        dialog.setTitle(R.string.please_select_app)
        val appNameList =
            arrayOfNulls<String>(listener.appList!!.size)
        for (i in appNameList.indices) {
            appNameList[i] = listener.appList!![i]
        }
        val appListView =
            dialog.findViewById<View>(R.id.appList) as ListView
        appListView.adapter =
            ArrayAdapter(this@EzyWireActivity, android.R.layout.simple_list_item_1, appNameList)
        appListView.onItemClickListener =
            OnItemClickListener { parent, view, position, id ->
                wisePadController.selectApplication(position)
                dialog.dismiss()
            }
        dialog.findViewById<View>(R.id.cancelButton)
            .setOnClickListener {
                wisePadController.cancelSelectApplication()
                dialog.dismiss()
            }
        dialog.show()
    }

    private fun transactionFailed(status: String) {
        Log.e("Failed", status)
        showHideLayout(Constants.FailureTransaction, status)
    }

    private fun getSignature(extra: String?) {
        Log.e("Signature", ""+extra)
        showHideLayout(Constants.Signature, "Signature")
    }

    private fun sendPaymentInformation() {

        val requestVal: HashMap<String, String> = HashMap()
        showDialog("Loading...")
        val trustStore = KeyStore.getInstance(KeyStore.getDefaultType())
        trustStore.load(null, null)
        if (preAuth) {
            requestVal[Fields.Service] = Fields.PRE_AUTH
        } else {
            requestVal[Fields.Service] = Fields.START_PAY
        }

        val amtCrypto: String?
        var amtHex: String? = null

        try {
            amtCrypto = encrypt(getAmount(amount!!))
            println(" Encrypted Data : $amtCrypto")
            amtHex = hexaToAscii(amtCrypto!!, true)
            println(" Hex Data : $amtHex")
        } catch (e: java.lang.Exception) { // TODO Auto-generated catch block
            e.printStackTrace()
        }

        requestVal[Fields.sessionId] = getLoginResponse(applicationContext).sessionId
        requestVal[Fields.MerchantId] = getLoginResponse(applicationContext).merchantId
        requestVal[Fields.DeviceId] = getSharedString(Fields.DeviceId, applicationContext)
        requestVal[Fields.HostType] = getLoginResponse(applicationContext).hostType
        requestVal[Fields.PayId] = amtHex!! // 12 digit price (zero padded in front of original number)

        requestVal[Fields.AdditionAmount] = getAmount("00")
        requestVal[Fields.CardDetails] = Constants.TLV // card tlv data

        if (Constants.isICC.equals("N", true)) {
            requestVal["iccCard"] = Constants.isICC // icc card SWIPE
        } else {
            Constants.isICC = ""
            requestVal["iccCard"] = Constants.isICC // INSERT OR TAP
        }

        requestVal[Fields.Latitude] = Constants.latitudeStr
        requestVal[Fields.Longitude] = Constants.longitudeStr
        //Remove special characters in Country name
        requestVal[Fields.Location] = if(Pattern.matches(".*[a-zA-Z]+.*[a-zA-Z]", Constants.countryStr)) Constants.countryStr else ""
        requestVal[Fields.PanSequenceNum] =
            if (Constants.PAN.isEmpty()) "02" else Constants.PAN // if PAN is empty then 02 sent as default

        if (Constants.PIN_DATA.isEmpty()) {
            SWIPE_PINDATA = "SWIPEPIN"
        }
        requestVal[Fields.PinData] = Constants.PIN_DATA
        requestVal[InvoiceId] = invoiceId!!

        deliverApiService.run {
            getPaymentInfo(requestVal).enqueue(object : retrofit2.Callback<PaymentInfoModel> {
                override fun onFailure(call: Call<PaymentInfoModel>, t: Throwable) {
                    cancelDialog()
                    Log.e("PaymentInfoModel ", "" + t.message)
                }
                override fun onResponse(
                    call: Call<PaymentInfoModel>,
                    response: Response<PaymentInfoModel>
                ) {
                    cancelDialog()
                    if (response.isSuccessful) {
                        val it = response.body()!!
                        if (it.responseCode.equals("0000", true)) {
                            Constants.TRANS_ID = it.responseData.trxId
                            if (Constants.isICC.isEmpty()) {
                                if (it.responseData.pinEntry != null) {
                                    pinEntry = it.responseData.pinEntry
                                    if (it.responseData.pinEntry.equals("NO",true))
                                        Constants.isICC = "N"
                                }
                            } else {
                                pinEntry = "SWIPE"
                            }

                            if (it.responseData.chipData != null) {
                                wisePadController.sendOnlineProcessResult(it.responseData.chipData)
                            } else {
                                callAcknowledgementAPI(it.responseData.trxId)
                            }
                            cancelTracking = 0

                        } else {
                            if (it.responseData.chipData != null) {
                                wisePadController.sendOnlineProcessResult(it.responseData.chipData)
                            }
                            Constants.TRANS_ID = ""

                            //Have to include Dialog Interface
                            cancelTrackingData()
                            showHideLayout(Constants.FailureTransaction,it.responseDescription)
                        }
                    } else {
                        Log.e("PaymentInfoModel ", "" + response.message())
                    }
                }
            })
        }

//        ezyWireViewModel.requestPaymentInfo(requestVal)
//        ezyWireViewModel.paymentInfoData.observe(this, androidx.lifecycle.Observer {
//            if (it.responseCode.equals("0000", true)) {
//                Constants.TRANS_ID = it.responseData.trxId
//                if (Constants.isICC.isEmpty()) {
//                    if (it.responseData.pinEntry != null) {
//                        pinEntry = it.responseData.pinEntry
//                        if (it.responseData.pinEntry.equals("NO",true))
//                            Constants.isICC = "N"
//                    }
//                } else {
//                    pinEntry = "SWIPE"
//                }
//
//                if (it.responseData.chipData != null) {
//                    wisePadController.sendOnlineProcessResult(it.responseData.chipData)
//                } else {
//                    callAcknowledgementAPI(it.responseData.trxId)
//                }
//                cancelTracking = 0
//
//            } else {
//                if (it.responseData.chipData != null) {
//                    wisePadController.sendOnlineProcessResult(it.responseData.chipData)
//                }
//                Constants.TRANS_ID = ""
//
//                //Have to include Dialog Interface
//                cancelTrackingData()
//                showHideLayout(Constants.FailureTransaction,it.responseDescription)
//            }
//
//        })

    }

    private fun callAcknowledgementAPI(trxId: String) {

        showDialog("Loading")
        val trustStore = KeyStore.getInstance(KeyStore.getDefaultType())
        trustStore.load(null, null)
        val requestVal: HashMap<String, String> = HashMap()
        if (preAuth) {
            requestVal[Fields.Service] = Fields.PREAUTH_ACK
        } else {
            requestVal[Fields.Service] = Fields.SALE_ACK
        }
        requestVal[Fields.sessionId] = getLoginResponse(applicationContext).sessionId
        requestVal[Fields.tid] = getLoginResponse(applicationContext).tid
        requestVal[Fields.HostType] = getLoginResponse(applicationContext).hostType
        requestVal[Fields.trxId] = trxId

        ezyWireViewModel.requestCallAck(requestVal)
        ezyWireViewModel.callAckData.observe(this, androidx.lifecycle.Observer {
            if (it.responseCode.equals("0000", true)) {
                cancelDialog()

                cancelTracking = 0
                Constants.TRANS_ID = it.responseData.trxId

                if (Constants.isICC.isEmpty()) { //openReceiptScreen();
                    showHideLayout(Constants.SuccessTransaction, "")
                } else {
                    if (Constants.isPinVerified) {
                        Constants.Signature = ""
                        Constants.isPinVerified = false
                        showHideLayout(Constants.SuccessTransaction, "")
                    } else {
                        getSignature("RM " + String.format("%.2f", totalPrice))
                    }
                }
                cancelTracking = 0
            }else{
                transactionFailed("Transaction Terminated")
            }
        })

    }

    private fun sendDeclinedNotification() {
        showDialog("Loading")
        val trustStore = KeyStore.getInstance(KeyStore.getDefaultType())
        trustStore.load(null, null)
        val requestVal = HashMap<String, String>()

        if (preAuth) {
            requestVal[Fields.Service] = Fields.PREAUTH_REVERSAL
        } else {
            requestVal[Fields.Service] = Fields.REVERSAL
        }
        requestVal[Fields.sessionId] = sessionId
        requestVal[Fields.trxId] = Constants.TRANS_ID
        requestVal[Fields.tid] = tid
        requestVal[Fields.HostType] = hostType

        ezyWireViewModel.requestSendNotifyDecline(requestVal)
        ezyWireViewModel.sendDeclineNotifyData.observe(this, androidx.lifecycle.Observer {
            if (it.responseCode.equals("0000", true)) {
                cancelDialog()
                if (it.responseData != null) {
                    Constants.TRANS_ID = ""
                }
            }
        })
    }

    private fun cancelTrackingData() {
        when (cancelTracking) {
            1 -> {
                wisePadController.cancelCheckCard()
            }
            2 -> {
                wisePadController.cancelSetAmount()
            }
        }
        cancelTracking = 0
    }

    @SuppressLint("GetInstance")
    @Throws(Exception::class)
    fun encrypt(Data: String): String? { //Log.v("dataa-->", Data);
        val key = generateKey()
        val c =
            Cipher.getInstance(ALGO)
        c.init(Cipher.ENCRYPT_MODE, key)
        val encVal = c.doFinal(Data.toByteArray())
        //String encryptedValue = Base64.getEncoder().encodeToString(encVal);// new
// BASE64Encoder().encode(encVal);
        return Base64.encodeToString(encVal, Base64.DEFAULT)
    }

    @Throws(Exception::class)
    private fun generateKey(): Key {
        val key_to_encrypt = getSharedString(Fields.DeviceId, applicationContext)
        val key: Key
        var final_key = key_to_encrypt.replace("-LE".toRegex(), "")
        if (final_key.length < 16) { //Log.v("keyy1", final_key);
            final_key = final_key + "0"
            //            Log.v("keyy11", final_key);
            val keyValue = final_key.toByteArray()
            key = SecretKeySpec(keyValue, ALGO)
        } else { //            Log.v("keyy2", final_key);
            val keyValue = final_key.toByteArray()
            key = SecretKeySpec(keyValue, ALGO)
        }
        return key
    }

    private fun hexaToAscii(s: String, stringData: Boolean): String? {
        var retString: String? = ""
        var tempString: String
        var offset = 0
        if (stringData) {
            for (i in s.indices) {
                tempString = s.substring(offset, offset + 1)
                retString += encodeHexString(tempString)
                offset += 1
            }
        }
        return retString
    }

    @SuppressLint("DefaultLocale")
    private fun encodeHexString(sourceText: String): String? {
        val rawData = sourceText.toByteArray()
        val hexText = StringBuffer()
        var initialHex: String? = null
        var initHexLength: Int
        for (i in rawData.indices) {
            val positiveValue: Int = (rawData[i] and 0x000000FF.toByte()).toInt()
            initialHex = Integer.toHexString(positiveValue)
            initHexLength = initialHex.length
            while (initHexLength++ < 2) {
                hexText.append("0")
            }
            hexText.append(initialHex)
        }
        return hexText.toString().toUpperCase()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.connect_bluetooth_txt -> {
                connectDevice()
            }
            R.id.failure_try_again_btn -> {
                startActivity(Intent(this, MainActivity::class.java))
            }
            R.id.success_print_btn ->{
                val bundle = Bundle()
                if (preAuth)
                    bundle.putString(Fields.Service, Fields.PRE_AUTH_RECEIPT)
                else
                    bundle.putString(Fields.Service, Fields.TXN_REPRINT)
                bundle.putString(Fields.trxId, Constants.TRANS_ID)
                bundle.putString(Fields.Amount, amount)
                bundle.putString(Fields.Signature, "")
                bundle.putString(Constants.ActivityName, Constants.EzywireAct)
                addFragment(printReceiptFragment,bundle,R.id.coordinatorLayout)
            }
            R.id.btn_sign_clear -> {
                signature_view.clearSignature()
            }
            R.id.btn_sign_payment -> {

                val bitMap = signature_view.signature
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitMap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()

                if (signature_view.isSignatureDrawn){
                    val bundle = Bundle()
                    bundle.putString(Fields.Service, Fields.TXN_REPRINT)
                    bundle.putString(Fields.trxId, Constants.TRANS_ID)
                    bundle.putString(Constants.Redirect, Constants.Home)
                    bundle.putString(Constants.ActivityName, Constants.EzywireAct)
                    bundle.putString(Fields.Signature, Base64.encodeToString(byteArray,Base64.DEFAULT))
                    addFragment(printReceiptFragment,bundle,R.id.coordinatorLayout)
                }else{
                    shortToast("Signature is mandatory.")
                }


            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                when {
                    insert_card_relative.isVisible -> {
                        wisePadController.cancelPinEntry()
                        wisePadController.cancelCheckCard()
                        wisePadController.cancelSetAmount()
                    }
                    pin_enter_relative.isVisible -> {
                        wisePadController.cancelPinEntry()
                        wisePadController.cancelCheckCard()
                        wisePadController.cancelSetAmount()
                        wisePadController.cancelSelectApplication()
                        wisePadController.cancelDisplayPrompt()
                    }
                    connect_bluetooth_relative.isVisible -> {
                        finish()
                    }
                    transaction_failure_relative.isVisible -> {
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                    signature_relative.isVisible -> {
                        shortToast("Signature is Mandatory to Complete Payment")
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showHideLayout(layout: String, message: String) {

        when (layout) {
            Constants.BTScreen -> {
                connect_bluetooth_relative.visibility = View.VISIBLE
                insert_card_relative.visibility = View.GONE
                pin_enter_relative.visibility = View.GONE
                transaction_failure_relative.visibility = View.GONE
                transaction_success_relative.visibility = View.GONE
                signature_relative.visibility = View.GONE
            }
            Constants.InsertCard -> {
                connect_bluetooth_relative.visibility = View.GONE
                insert_card_relative.visibility = View.VISIBLE
                pin_enter_relative.visibility = View.GONE
                transaction_failure_relative.visibility = View.GONE
                transaction_success_relative.visibility = View.GONE
                signature_relative.visibility = View.GONE
            }
            Constants.EnterPin -> {
                connect_bluetooth_relative.visibility = View.GONE
                insert_card_relative.visibility = View.GONE
                pin_enter_relative.visibility = View.VISIBLE
                transaction_failure_relative.visibility = View.GONE
                transaction_success_relative.visibility = View.GONE
                signature_relative.visibility = View.GONE
            }
            Constants.FailureTransaction -> {
                failure_txt.text = message
                connect_bluetooth_relative.visibility = View.GONE
                insert_card_relative.visibility = View.GONE
                pin_enter_relative.visibility = View.GONE
                transaction_failure_relative.visibility = View.VISIBLE
                transaction_success_relative.visibility = View.GONE
                signature_relative.visibility = View.GONE
            }
            Constants.SuccessTransaction -> {
                connect_bluetooth_relative.visibility = View.GONE
                insert_card_relative.visibility = View.GONE
                pin_enter_relative.visibility = View.GONE
                transaction_failure_relative.visibility = View.GONE
                transaction_success_relative.visibility = View.VISIBLE
                signature_relative.visibility = View.GONE
            }
            Constants.Signature -> {
                connect_bluetooth_relative.visibility = View.GONE
                insert_card_relative.visibility = View.GONE
                pin_enter_relative.visibility = View.GONE
                transaction_failure_relative.visibility = View.GONE
                transaction_success_relative.visibility = View.GONE
                signature_relative.visibility = View.VISIBLE
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {

    }

    override fun onResume() {
        super.onResume()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onBackPressed() {
//        super.onBackPressed()
    }


}