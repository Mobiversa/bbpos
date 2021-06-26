package com.mobiversa.ezy2pay.network.response

import java.io.Serializable

data class RegisterDetailResponse(
    val responseCode: String,
    val responseData: RegisterResponseData,
    val responseDescription: String,
    val responseMessage: String
)

data class RegisterResponseData(
    val listCategoryData: List<CategoryData>,
    val merchantAddr: String,
    val merchantCity: String,
    val merchantName: String?,
    val merchantPostCode: String,
    val merchantState: String,
    val merchantType: String,
    val officeNo: String
) : Serializable

data class CategoryData(
    val categoryCode: String,
    val categoryDesc: String,
    val categoryName: String
) : Serializable