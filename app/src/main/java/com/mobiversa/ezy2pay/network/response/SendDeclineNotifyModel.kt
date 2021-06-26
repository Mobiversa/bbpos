package com.mobiversa.ezy2pay.network.response

class SendDeclineNotifyModel (
    val responseCode: String,
    val responseData: PaymentInfoResponseData?,
    val responseDescription: String,
    val responseMessage: String
)
data class SendDeclineNotifyResponseData(
    val trxId: String,
    val chipData: String,
    val pinEntry: String
)