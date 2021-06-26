package com.mobiversa.ezy2pay.network.response

data class StateModel(
    val responseCode: String,
    val responseData: ArrayList<StateResponseData>,
    val responseDescription: String,
    val responseMessage: String
)

data class StateResponseData(
    val Code: String,
    val Country_Id: String,
    val Id: Int,
    val Name: String,
    val City: String
)