package com.mobiversa.ezy2pay.network.response

data class VerifyUserModel(
    val responseCode: String,
    val responseData: VerifyResponseData,
    val responseDescription: String,
    val responseMessage: String
)

data class VerifyResponseData(
    val description: String,
    val type: String
)