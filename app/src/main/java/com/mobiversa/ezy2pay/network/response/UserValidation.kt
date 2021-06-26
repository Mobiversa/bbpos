package com.mobiversa.ezy2pay.network.response

data class UserValidation(
    val responseCode: String,
    val responseData: ValidateResponseData,
    val responseDescription: String,
    val responseMessage: String
)

data class ValidateResponseData(
    val deviceId: String,
    val deviceType: String,
    val mid: String,
    val status: String?,
    val description: String?,
    val tid: String
)