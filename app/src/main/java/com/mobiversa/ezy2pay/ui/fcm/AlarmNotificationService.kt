package com.mobiversa.ezy2pay.ui.fcm

import android.app.IntentService
import android.app.NotificationManager
import android.content.Intent
import android.util.Log


/**
 * Created by Karthik on 10/04/17.
 */
class AlarmNotificationService : IntentService("AlarmNotificationService") {
    private val alarmNotificationManager: NotificationManager? = null
    public override fun onHandleIntent(intent: Intent?) { //Send notification
        sendNotification("Your session has been expired")
    }

    //handle notification
    private fun sendNotification(msg: String) {
//        EzywireUtils.SignOut(this)
        Log.v("<---LogoutHere--->", "LogoutDone")
    }

    companion object {
        //Notification ID for Alarm
        const val NOTIFICATION_ID = 1
    }
}