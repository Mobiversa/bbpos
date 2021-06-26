package com.mobiversa.ezy2pay.network.response

data class OTPResponse(
    val responseCode: String,
    val responseData: OTPResponseData,
    val responseDescription: String,
    val responseMessage: String
)

data class OTPResponseData(
    val countryCurPhone: CountryCurPhone,
    val date: String,
    val opt: String,
    val mobileNo: String,
    val username: String,
    val trace: String
)

data class CountryCurPhone(
    val countryIso2: String,
    val countryIso3: String,
    val countryName: String,
    val curAlphaCode: String,
    val currencyCode: String,
    val currencyName: String
)