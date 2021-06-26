package com.mobiversa.ezy2pay.network.response

import java.io.Serializable

data class TransactionHistoryModel(
    val responseCode: String,
    val responseData: HistoryResponseData,
    val responseDescription: String,
    val responseMessage: String
)

data class HistoryResponseData(
    val forSettlement: List<ForSettlement>?,
    val preAuthorization: List<ForSettlement>?
)

data class ForSettlement(
    val additionAmount: String,
    val amount: String?,
    val currency: String?,
    val date: String,
    val hostType: String,
    val invoiceId: String?,
    val latitude: String,
    val longitude: String,
    val mid: String,
    val rrn: String?,
    val stan: String,
    val aidResponse: String,
    val cardType: String?,
    val status: String,
    val tid: String,
    val time: String,
    val txnId: String,
    val pan: String?,
    val txnType: String?
) : Serializable

data class PreAuthorization(
    val additionAmount: String,
    val amount: String,
    val currency: String?,
    val date: String,
    val hostType: String,
    val invoiceId: String,
    val latitude: String,
    val longitude: String,
    val mid: String,
    val rrn: String?,
    val stan: String,
    val aidResponse: String,
    val cardType: String?,
    val status: String,
    val tid: String,
    val time: String,
    val txnId: String,
    val txnType: String?
) : Serializable