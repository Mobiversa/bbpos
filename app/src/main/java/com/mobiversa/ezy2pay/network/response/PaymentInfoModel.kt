package com.mobiversa.ezy2pay.network.response

class PaymentInfoModel (
    val responseCode: String,
    val responseData: PaymentInfoResponseData,
    val responseDescription: String,
    val responseMessage: String
)
data class PaymentInfoResponseData(
    val trxId: String,
    val chipData: String?,
    val pinEntry: String?
)