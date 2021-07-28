package com.mobiversa.ezy2pay.utils

import android.bluetooth.BluetoothDevice
import com.mobiversa.ezy2pay.ui.loginActivity.signup.BusinessDetailFragment
import java.util.*

class Constants {

    companion object {
        const val CHAT_URL : String = "https://gomobi.io/support/livechat?cm="
        const val LOGGED_CHAT_URL : String = "https://gomobi.io/support/livechat?nm="
        const val tcHtmlString : String = "https://gomobi.io/terms-condition/"
        const val mobiApi : String = "b07ad9f31df158edb188a41f725899bc"
        const val BB_POS: String = "BB_POS"
        const val Normal : String = "NORMAL"
        const val LITE : String = "LITE"
        const val Balance : String = "Balance"
        const val FIRE_BASE_TOKEN : String = "token"
        const val RememberMe : String = "RememberMe"
        const val IsLoggedIn : String = "IsLoggedIn"
        const val LastLogin : String = "LastLogin"
        const val UserName : String = "UserName"
        const val LoginResponse : String = "LoginResponse"
        const val ProductResponse : String = "ProductResponse"
        const val CountryResponse : String = "CountryResponse"
        const val RegisterResponseData : String = "RegisterResponseData"
        const val BusinessDetailData : String = "BusinessDetailData"
        const val MerchantDetailData : String = "MerchantDetailData"
        const val UpgradeStatus : String = "UpgradeStatus"
        const val UPGRADE : String = "UPGRADE"
        const val otp : String = "otp"
        const val PHONE : String = "PHONE"

        //Signup
        const val enteredMobilenumber: String = "entered_mobilenumber"
        const val FullName: String = "FullName"
        const val fb: String = "fb"
        const val fbname: String = "fbname"
        const val gmailname: String = "gmailname"
        const val gmail: String = "gmail"
        const val dob: String = "dob"
        const val merchantType: String = "merchantType"
        const val currencyCode: String = "currencyCode"

        const val categoryName = "categoryName"
        const val categoryCode = "categoryCode"

        const val Ezywire : String = "EZYWIRE"
        var EzyMoto : String = "EZYMOTO"
        const val EzyRec : String = "EZYREC"
        const val EzySplit : String = "EZYSPLIT"
        const val EzyAuth : String = "EZYAUTH"
        const val PREMOTO : String = "PREMOTO"
        const val PREWIRE : String = "PREWIRE"
        const val Boost : String = "BOOST"
        const val GrabPay : String = "GRABPAY"
        const val MobiPass : String = "MOBIPASS"
        const val MobiCash : String = "CASH"
        const val Product : String = "Product"
        const val DeviceStatus : String = "deviceStatus"

        const val ProductDisable : String = "You are not Subscribed to this Product"
        const val ENTER_PASSWORD : String = "Please enter the password to proceed void"
        const val PLEASE_ENTER_NAME : String = "Please enter the name"
        const val PLEASE_ENTER_MOBILE : String = "Please Enter the valid mobile number!"
        const val PLEASE_ENTER_VALID_MOBILE = "Please Enter valid mobile number"
        const val ENTERED_WRONG_OTP = "You entered wrong OTP number! Please check and try again!"
        const val PLEASE_ENTER_VALIDMOBILE = "Please Enter valid mobile number"
        const val ROOTED_DEVICE = "This device is rooted. You can't use this app"

        const val ENTER_UNAME = "Username is required"
        const val ENTER_PASSWORD_REG = "Password is required"
        const val ENTER_CPASSWORD_REG = "Confirm Password is required"
        const val PASSWORDS_NOT_MATCH = "Passwords not matching"
        const val ENTER_COMPANY_NAME = "Company Name required"
        const val ENTER_COMPANY_PHONE = "Company phonenumber required"
        const val ENTER_STREET = "Company Street is required"
        const val ENTER_CITY = "City is required"
        const val ENTER_STATE = "State is required"
        const val ENTER_POSTALCODE = "Postalcode is required"
        const val NAME_REQUIRED = "Name is required"

        const val app = "APP"
        const val YEAR = "year"
        const val MONTH = "month"
        const val DAY = "day"

        var latitudeStr = ""
        var longitudeStr = ""
        var countryStr = ""
        var pswdStr = ""

        var foundDevices: List<BluetoothDevice>? = null
        var printerfoundDevices: List<BluetoothDevice>? = null

        var apduMap: Hashtable<String, Any> = Hashtable<String, Any>()

        var TLV = ""
        var PAN = ""
        var isICC = ""
        var isSwipe = false
        var PIN_DATA = ""
        var TRANS_ID = ""
        var Signature = ""
        var isDeviceConnected =  false
        var isPrinterConnected: Boolean = false
        var isPinVerified: Boolean = false
        var connectedDevice: BluetoothDevice? = null
        var batteryLevel = "Device Disconnected"

        // Card Details
        var ExpiryDate = ""
        var cardholderName = ""
        var cardNumber = ""
        var BBDeviceVersion = ""

        //HandlerStatus
        const val DeviceConnected = "deviceconnected"
        const val InjectSessionFail = "InjectSessionFail"
        const val InjectSessionSuccess = "InjectSessionSuccess"
        const val CardSwiped = "Card Swiped"
        const val SwipeProcess = "Swipe Process"
        const val StartEMV = "Start EMV"
        const val EnterPIN = "Enter PIN"
        const val PRESENT_CARD = "PRESENT_CARD"
        const val SHOW_PIN_PAD = "SHOW_PIN_PAD"
        const val SHOW_ASTRIX = "SHOW_ASTRIX"
        const val PRINT_RECEIPT = "PRINT_RECEIPT"
        const val DEVICE_INFO = "DEVICE_INFO"
        const val PRINT_END = "PRINT_END"
        const val SetAmount = "set amount"
        const val SelectApplication = "select application"
        const val FinalConfirm = "Final Confirm"
        const val OnlineProcess = "online process"
        const val CardCompleted = "card completed"
        const val APPROVED = "APPROVED"
        const val PIN_CANCELLED = "PIN CANCELLED"
        const val PaymentCancelled = "Payment Cancelled"
        const val TERMINATED = "TERMINATED"
        const val DECLINED = "DECLINED"
        const val CANCEL_OR_TIMEOUT = "CANCEL_OR_TIMEOUT"
        const val TIMEOUT = "TIMEOUT"
        const val NOT_ICC = "NOT_ICC"
        const val CARD_BLOCKED = "CARD_BLOCKED"
        const val DEVICE_ERROR = "DEVICE_ERROR"
        const val ICC_CARD_REMOVED = "ICC_CARD_REMOVED"
        const val AmountDeclined = "Amount Declined"
        const val IsPinCanceled = "isPinCanceled"
        const val IncorrectPin = "Incorrect Pin"
        const val CardDeclined = "Card Declined"
        const val DeviceDisconnected = "Device Disconnected"
        const val Error = "Error:"
        const val NotICCCard = "Not ICC Card"
        const val BadSwipe = "Bad Swipe"
        const val InvalidCard = "Invalid Card"
        const val ReversalData = "ReversalData"

        const val PinScreen = "PinScreen"
        const val PIN_VERIFIED = "PIN_VERIFIED"
        const val APDU = "APDU"

        //Ezywire
        const val BTScreen : String = "BlueTooth"
        const val InsertCard : String = "InsertCart"
        const val EnterPin : String = "EnterPin"
        const val FailureTransaction : String = "FailureTransaction"
        const val SuccessTransaction : String = "SuccessTransaction"
        const val SignaturePage : String = "Signature"

        const val CARD_PAYMENT = "CARD_PAYMENT"
        const val ActivityName = "Activity"
        const val Redirect = "Redirect"
        const val MainAct = "MainActivity"
        const val EzywireAct = "EzywireActivity"
        const val History = "History"
        const val Home = "Home"

        //History
        const val Amount: String = "Amount"
        const val Date: String = "Date"

        //Profile Update
        const val NONEZYWIRE : String = "NONEZYWIRE"
        const val EZYWIRE : String = "EZYWIRE"

        const val Index = "index"
        const val EncSK = "encSK"
        const val Kcv = "kcv"

        //LITE Status
        const val approved = "APPROVED"
        const val processing = "PROCESSING"
        const val upgrade = "UPGRADE"


        val DEVICE_NAMES = arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "WisePad", "WP", "MPOS", "M36", "M188")
        val PRINTER_DEVICE_NAMES = arrayOf("BTPTR", "SIMPLYP")


    }
}