package com.mobiversa.ezy2pay.network.response

import java.io.Serializable

data class NotificationList (
        val responseCode: String,
        val responseDescription: String,
        val responseList: ArrayList<Response>,
        val responseMessage: String,
        val messageCount: String
){
    data class Response(
        val createdDate: String,
        val hid: Int,
        val modifiedDate: String,
        val msgDetails: String,
        var msgRead: Boolean,
        val msgTitle: String,
        val msgTrash: Boolean,
        val muid: Int,
        val timeStamp: String
    ) : Serializable, Comparable<Response> {
        override fun compareTo(other: Response) = when {
            createdDate < other.createdDate -> -1
            createdDate > other.createdDate -> 1
            else -> 0
        }
    }

}