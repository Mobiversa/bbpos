package com.mobiversa.ezy2pay.network.response

data class ProductList(
    val productName: String,
    val productImage: Int,
    val disableProductImage: Int,
    val tID: String?,
    val mID: String?,
    val isEnable: Boolean,
    val historyName: String,
    val displayName: String
)