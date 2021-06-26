package com.mobiversa.ezy2pay.network.response

data class QRModel(
    val responseCode: String,
    val responseData: QRResponseData,
    val responseDescription: String,
    val responseMessage: String
)

data class QRResponseData(
    val base64ImageQRCode: String,
    val qrcode: String
)