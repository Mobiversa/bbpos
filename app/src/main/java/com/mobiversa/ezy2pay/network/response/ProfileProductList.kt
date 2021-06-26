package com.mobiversa.ezy2pay.network.response

data class ProfileProductList(
    val responseCode: String,
    val responseData: ProductResponseData,
    val responseDescription: String,
    val responseMessage: String
)

data class ProductResponseData(
    val deviceExpiry: String,
    val deviceId: String,
    val deviceRenewal: String,
    val deviceStatus: String,
    val enableBoost: String,
    val enableEzypass: String,
    val enableEzyrec: String,
    val enableEzywire: String,
    val enableMoto: String,
    val ezypassDeviceExpiry: String,
    val ezypassDeviceRenewal: String,
    val ezypassDeviceStatus: String,
    val ezypassMid: String,
    val ezypassTid: String,
    val ezyrecMid: String,
    val ezyrecTid: String,
    val merchantType: String,
    val mid: String,
    val motoMid: String,
    val motoTid: String,
    val preAuth: String,
    val tid: String
)