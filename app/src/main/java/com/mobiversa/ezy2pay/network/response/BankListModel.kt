package com.mobiversa.ezy2pay.network.response

data class BankListModel(
    val responseCode: String,
    val responseDataB2B: ResponseDataB2B,
    val responseDataB2C: ResponseDataB2C,
    val responseDataBT: ResponseDataBT,
    val responseDescription: String,
    val responseMessage: String
)

data class ResponseDataB2B(
    val bankList: List<Bank>
)

data class ResponseDataB2C(
    val bankList: List<BankX>
)

data class ResponseDataBT(
    val bankTypeList: List<BankType>
)

data class Bank(
    val Active: String,
    val BankCode: String,
    val BankDisplayName: String,
    val BankName: String,
    val ID: Int,
    val LastUpdatedOn: String,
    val Logo: String
)

data class BankX(
    val Active: String,
    val BankCode: String,
    val BankDisplayName: String,
    val BankName: String,
    val ID: Int,
    val LastUpdatedOn: String,
    val Logo: String
)

data class BankType(
    val bankTypeCode: String,
    val name: String
)