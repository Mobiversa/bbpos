package com.mobiversa.ezy2pay.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mobiversa.ezy2pay.network.response.NotificationList
import com.mobiversa.ezy2pay.network.response.SuccessModel

class NotificationsViewModel : ViewModel() {

    private val service = NotificationsModel()
    var notificationList = MutableLiveData<NotificationList>()
    var notificationTask = MutableLiveData<SuccessModel>()

    fun notificationList(postParam: HashMap<String, String>) {
        notificationList = service.notificationList(postParam)
    }
    fun notificationTask(postParam: HashMap<String, String>) {
        notificationTask = service.notificationTask(postParam)
    }

    private val _text = MutableLiveData<String>().apply {
        value = "This is notifications Fragment"
    }
    val text: LiveData<String> = _text
}