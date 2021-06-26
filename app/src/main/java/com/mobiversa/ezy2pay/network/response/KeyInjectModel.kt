package com.mobiversa.ezy2pay.network.response

data class KeyInjectModel(
    val responseCode: String,
    val responseData: KeyInjectResponseData,
    val responseDescription: String,
    val responseMessage: String
)

data class KeyInjectResponseData(
    val MEK: String,
    val MEKKCV: String,
    val TEK: String,
    val TEKKCV: String,
    val TPK: String,
    val TPKKCV: String
)