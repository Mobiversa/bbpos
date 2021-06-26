package com.mobiversa.ezy2pay.ui.notifications

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.mobiversa.ezy2pay.network.ApiService
import com.mobiversa.ezy2pay.network.response.NotificationList
import com.mobiversa.ezy2pay.network.response.SuccessModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationsModel {

    private val deliveryApiService = ApiService.notificationRequest()
    val data = MutableLiveData<NotificationList>()
    val taskData = MutableLiveData<SuccessModel>()

    fun notificationList(postParam: HashMap<String, String>): MutableLiveData<NotificationList> {
        deliveryApiService.getNotificationList(postParam).enqueue(object :
            Callback<NotificationList> {
            override fun onFailure(call: Call<NotificationList>, t: Throwable) {
                notificationFailureResponse(data)
            }

            override fun onResponse(call: Call<NotificationList>, response: Response<NotificationList>) {
                if (response.isSuccessful) {
                    data.postValue(response.body())
                } else {
                    notificationFailureResponse(data)
                }
            }
        })
        return data
    }

    fun notificationTask(postParam: HashMap<String, String>): MutableLiveData<SuccessModel> {
        deliveryApiService.getNotificationTask(postParam).enqueue(object :
            Callback<SuccessModel> {
            override fun onFailure(call: Call<SuccessModel>, t: Throwable) {
                notificationFailureResponse(data)
            }

            override fun onResponse(call: Call<SuccessModel>, response: Response<SuccessModel>) {
                if (response.isSuccessful) {
                   taskData.postValue(response.body())
                } else {
                    notificationFailureResponse(data)
                }
            }
        })
        return taskData
    }

    private fun notificationFailureResponse(data: MutableLiveData<NotificationList>) {
        Log.e("NotificationVM ", ""+data)
    }
}