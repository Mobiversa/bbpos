package com.mobiversa.ezy2pay.network.response

data class CountryList(
    val responseCode: String,
    val responseData: CountryResponseData,
    val responseDescription: String,
    val responseMessage: String
)

data class CountryResponseData(
    val country: ArrayList<Country>
)

data class Country(
    val countryCode: String,
    val countryIso: String,
    val countryName: String,
    val phoneCode: String
)