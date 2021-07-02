package com.mobiversa.ezy2pay.utils

class Fields {

    companion object {

        var VERSION = "version"

        //Service
        const val Service : String = "service"
        const val mobiApiKey : String = "mobiApiKey"
        const val loginId : String = "loginId"
        const val Profile : String = "Profile"
        const val Keyboard : String = "Keyboard"
        const val ServiceLogin : String = "LOGIN_MOB"
        const val ServiceForgot : String = "RESETPASSWORD"
        const val ServiceForgotOTP : String = "REQ_FORGET_PASSWORD_OTP"
        const val UPDATEPASSWORD : String = "UPDATEPASSWORD"
        const val ProductList : String = "PRODUCT_LIST"
        const val CountryList : String = "COUNTRY_LIST"
        const val ReqOTP : String = "REQ_OTP"
        const val INT_MER_REG : String = "INT_MER_REG"
        const val MERCHANT_REG : String = "MERCHANT_REG"
        const val UPGRADE : String = "UPGRADE"
        const val GET_MERCHANT_DET : String = "GET_MERCHANT_DET"
        const val UPDATE_USER_DET : String = "UPDATE_USER_DET"
        const val LITE_APP_MERCHANT_REG : String = "LITE_APP_MERCHANT_REG"
        const val LITE_MERCHANT_UPGRADE : String = "LITE_MERCHANT_UPGRADE"
        const val INT_VERIFY_REG : String = "INT_VERIFY_REG"
        const val VALIDATE_TERMINAL : String = "VALIDATE_TERMINAL"
        //Transaction
        const val BoostQR : String = "BOOST_QRCODE"
        const val GPayQR : String = "GPAY_QRCODE_REQ"
        const val MobiPassQR : String = "MOBIPASS_QRCODE"
        const val EzyRec : String = "EZYREC_TXN_REQ"
        const val EzySplit : String = "SPLIT_TXN_REQ"
        const val Moto : String = "MOTO"
        const val EzyMoto : String = "MOTO_TXN_REQ"
        const val EzyMotoLite : String = "LITE_MOTO_TXN_REQ"
        const val PreAuthMoto : String = "MOTO_PREAUTH_TXN_REQ"
        const val DeviceConnect : String = "DEVICE_CONNECT"
        const val MultiOption : String = "multiOption"
        const val NEW_EXTERNAL_TXN_LINK_REQ : String = "NEW_EXTERNAL_TXN_LINK_REQ"

        //Notification Service
        const val NotificationList : String = "NOTIFICATION_LIST"
        const val NotificationClearAll : String = "CLEAR_ALL"
        const val NotificationReadAll : String = "READ_ALL"
        const val NotificationUnReadAll : String = "UNREAD_ALL"
        const val NotificationUpdate : String = "UPDATE_NOTIFICATION"
        const val NotificationDelete : String = "DELETE_NOTIFICATION"
        const val Device : String = "ANDROID"
        const val Success : String = "SUCCESS"

        //Login
        const val username : String = "username"
        const val customerName : String = "customerName"
        const val muid : String = "muid"
        const val date : String = "date"
        const val password : String = "password"
        const val authType : String = "authType"
        const val deviceToken : String = "deviceToken"
        const val deviceType : String = "deviceType"
        const val appVersionNum : String = "appVersionNum"
        const val biomerticKey : String = "biomerticKey"
        const val authGoogle : String = "GOOGLE"
        const val authFacebook : String = "FACEBOOK"

        //Forgot Password
        const val userId : String = "userId"
        const val email : String = "email"
        const val merchantType : String = "merchantType"
        const val activationCode: String = "activationCode"
        const val fbLogin: String = "fbLogin"
        const val facebookId: String = "facebookId"
        const val googleLogin: String = "googleLogin"
        const val googleId: String = "googleId"

        //Boost Params
        const val sessionId : String = "sessionId"
        const val tid : String = "tid"
        const val mobiId: String = "mobiId"
        const val Type : String = "type"
        const val merchantId : String = "merchantId"
        const val liteMid : String = "liteMid"
        const val mobiLiteTid : String = "mobiLiteTid"
        const val mid : String = "mid"
        const val Amount : String = "amount"
        const val txnAmount : String = "txnAmount"
        const val AdditionalAmount : String = "additionAmount"
        const val InvoiceId : String = "invoiceId"
        const val Latitude : String = "latitude"
        const val Longitude : String = "longitude"
        const val Location : String = "location"
        const val Currency : String = "currency"
        const val MYR : String = "MYR"
        const val AppVersionNumber : String = "appVersionNum"
        const val MobileNo : String = "mobileNo"
        const val ContactName : String = "contactName"
        const val Recurring : String = "recurring"
        const val Period : String = "period"
        const val InstallmentCount : String = "installmentCount"
        const val Rrn : String = "rrn"
        const val reqMode : String = "reqMode"

        const val CountryName : String = "CountryName"
        const val Country : String = "country"
        const val encAmount : String = "encAmount"
        const val callback : String = "callback"

        //KeyInjection
        const val KeyInjection : String = "PLUS_DEVICE_CONNECT"
        const val DeviceId : String = "deviceId"
        const val HostType : String = "hostType"
        const val trxId : String = "trxId"
        const val txnId : String = "txnId"
        const val Index : String = "index"
        const val EncSK : String = "encSK"
        const val KCV : String = "kcv"

        //Ezywire
        const val PRE_AUTH : String = "PRE_AUTH"
        const val START_PAY : String = "PLUS_START_PAY"
        const val MerchantId : String = "merchantId"
        const val PayId : String = "payId"
        const val AdditionAmount : String = "additionAmount"
        const val CardDetails : String = "carddetails"
        const val PanSequenceNum : String = "panSequenceNum"
        const val PinData : String = "pinData"
        const val Signature : String = "signature"
        //Acknowledgement
        const val PREAUTH_ACK : String = "PREAUTH_ACK"
        const val SALE_ACK : String = "SALE_ACK"

        const val TRX_HISTORY : String = "TXN_HISTORY"
        const val LITE_TXN_HISTORY : String = "LITE_TXN_HISTORY"
        const val TRX_TYPE : String = "trxType"
        const val TXN_REPRINT : String = "TXN_REPRINT"
        const val PRE_AUTH_RECEIPT : String = "PRE_AUTH_RECEIPT"
        const val REVERSAL : String = "REVERSAL"
        const val PREAUTH_REVERSAL : String = "PREAUTH_REVERSAL"
        const val RECEIPT : String = "RECEIPT"

        //History Type
        const val ALL : String = "ALL"
        const val CARD : String = "CARD"
        const val BOOST : String = "BOOST"
        const val EZYMOTO : String = "EZYMOTO"
        const val PREAUTH: String = "EZYAUTH"
        const val GRABPAY : String = "GRABPAY"
        const val EZYPASS : String = "EZYPASS"
        const val PERAUTHHIST : String = "PRE_AUTH_HIST"
        const val EZYREC : String = "EZYREC"
        const val EZYSPLIT : String = "EZYSPLIT"
        const val CASH : String = "CASH"
        const val FPX : String = "FPX"

        const val EZYWIRE : String = "EZYWIRE"
        //Void
        const val CASH_CANCEL : String = "CASH_CANCEL"
        const val BOOST_VOID : String = "BOOST_VOID"
        const val VOID : String = "VOID"
        const val VALIDATE_VOID : String = "VALIDATE_VOID"
        const val PRE_AUTH_VOID : String = "PRE_AUTH_VOID"
        const val PRE_AUTH_SALE : String = "PRE_AUTH_SALE"
        const val AID : String = "aid"
        const val COMPLETED : String = "COMPLETED"
        const val GPAY_REFUND : String = "GPAY_REFUND"
        const val GPAY_CANCEL : String = "GPAY_CANCEL"
        const val GPAY_INQUIRY : String = "GPAY_INQUIRY"
        //Boost
        const val BOOST_STATUS : String = "BOOST_STATUS"
        const val transactionStatus : String = "transactionStatus"
        //Cash
        const val CASH_TXN : String = "CASH_TXN"
        const val CASH_RECEIPT : String = "CASH_RECEIPT"
        const val WhatsApp : String = "whatsApp"
        //Settlement
        const val SETTLEMENT = "SETTLEMENT"
        const val LOGOUT_MOB = "LOGOUT_MOB"
        const val LITE_DTL = "LITE_DTL"

        //Register
        const val FULL_LIST = "FULL_LIST"
        const val state = "state"
        const val postalCode = "postalCode"
        const val street = "street"
        const val address = "address"
        const val city = "city"
        const val businessName = "businessName"
        const val contactName = "contactName"
        const val country = "country"
        const val categoryName = "categoryName"

        const val StateOrCountry = "StateOrCountry"
        const val CountryID = "CountryID"
        const val StateID = "StateID"
        const val State = "State"
        const val City = "City"

        //response success code
        val STATUS_SUCCESS = 0
        val STATUS_FAILED = 1


    }
}
