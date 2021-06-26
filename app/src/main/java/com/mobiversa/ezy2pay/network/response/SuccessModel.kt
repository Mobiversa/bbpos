package com.mobiversa.ezy2pay.network.response

data class SuccessModel(
    val responseCode: String,
    val responseData: BalanceResponseData,
    val responseDescription: String,
    val responseMessage: String
)

data class BalanceResponseData(
    val dtlLimit: String,
    val contactNo: String,
    val businessName: String,
    val username: String
)