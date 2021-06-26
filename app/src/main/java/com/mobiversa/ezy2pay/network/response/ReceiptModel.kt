package com.mobiversa.ezy2pay.network.response

data class ReceiptModel(
    val responseCode: String,
    val responseData: ReceiptResponseData,
    val responseDescription: String,
    val responseMessage: String
)

data class ReceiptResponseData(
    val aid: String?,
    val approveCode: String?,
    val batchNo: String?,
    val cardHolderName: String?,
    val cardNo: String?,
    val cardType: String?,
    val rrn: String?,
    val tc: String?,
    val amount: String,
    val date: String,
    val invoiceId: String,
    val latitude: String,
    val longitude: String,
    val merchantAddr: String,
    val merchantCity: String,
    val merchantName: String,
    val merchantPhone: String,
    val merchantPostCode: String,
    val mid: String,
    val tid: String,
    val time: String,
    val tips: String,
    val trace: String,
    val txnType: String

)