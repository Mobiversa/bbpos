package com.mobiversa.ezy2pay.network.response

data class MerchantDetailModel(
    val responseCode: String,
    val responseData: MerchantDetailResponseData,
    val responseDescription: String,
    val responseMessage: String
)

data class MerchantDetailResponseData(
    val listCategoryData: List<CategoryData>?,
    val businessName: String,
    val categoryName: String,
    val city: String,
    val dob: String,
    val facebookId: String,
    val fbLogin: String,
    val googleId: String,
    val googleLogin: String,
    val merchantType: String,
    val mid: String,
    val officeNo: String,
    val postalCode: String,
    val state: String,
    val street: String,
    val tid: String,
    val username: String
)