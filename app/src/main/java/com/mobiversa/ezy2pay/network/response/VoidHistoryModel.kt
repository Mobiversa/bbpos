package com.mobiversa.ezy2pay.network.response

data class VoidHistoryModel(
    val responseCode: String,
    val responseData: VoidResponseData,
    val responseDescription: String,
    val responseMessage: String
)

data class VoidResponseData(
    val responseCode: String,
    val trxId: String
)