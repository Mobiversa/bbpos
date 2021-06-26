package com.mobiversa.ezy2pay.network.response

class CallAckModel (
    val responseCode: String,
    val responseData: CallAckResponseData,
    val responseDescription: String,
    val responseMessage: String
)
data class CallAckResponseData(
    val trxId: String
)