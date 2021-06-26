package com.mobiversa.ezy2pay.network.response

data class EzyMotoRecPayment(
    val responseCode: String,
    val responseData: EzyMotoRecResponseData,
    val responseDescription: String,
    val responseMessage: String
)

data class EzyMotoRecResponseData(
    val invoiceId: String,
    val opt: String
)