package com.mobiversa.ezy2pay.utils

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mobiversa.ezy2pay.network.response.NotificationVO
import com.mobiversa.ezy2pay.ui.fcm.Config
import com.mobiversa.ezy2pay.ui.fcm.NotificationUtils
import com.mobiversa.ezy2pay.ui.notifications.notificationFcm.DialogActivityForChat
import com.mobiversa.ezy2pay.ui.notifications.notificationFcm.ShowNotificationAcknowledgement
import com.mobiversa.ezy2pay.ui.splash.SplashActivity
import com.mobiversa.ezy2pay.utils.Constants.Companion.FIRE_BASE_TOKEN
import com.mobiversa.ezy2pay.utils.PreferenceHelper.set


class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMsgingService"
    private val TITLE = "title"
    private val EMPTY = ""
    private val MESSAGE = "message"
    private val URL = "url"
    private val ACTION = "action"
    private val DATA = "data"
    private val ACTION_DESTINATION = "action_destination"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.from!!)

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)

        }
        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.notification!!.body!!)
        }

        /*// Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            Map<String, String> data = remoteMessage.getData();
            handleData(data);

        } else if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
//            handleNotification(remoteMessage.getNotification());
            handleNotification(remoteMessage.getNotification().getBody(), remoteMessage.getNotification().getTitle());
        }// Check if message contains a notification payload.*/
        if (remoteMessage == null) return

        // Check if message contains a notification payload.
        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            Log.e(
                TAG,
                "Notification Body: " + remoteMessage.notification!!.body
            )
            handleNotification(
                remoteMessage.notification!!.body.toString(),
                remoteMessage.notification!!.title.toString(),
                remoteMessage.notification!!.tag.toString()
            )
        }

        // Check if message contains a data payload.
        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.e(
                TAG,
                "Data Payload: " + remoteMessage.data.toString()
            )
            try {
                val data =
                    remoteMessage.data
                handleData(data)
            } catch (e: Exception) {
                Log.e(TAG, "Exception: " + e.message)
            }
        }
    }

    @SuppressLint("NewApi")
    private fun handleNotification(
        message: String,
        title: String,
        tag: String
    ) {
        if (!NotificationUtils.isAppIsInBackground(applicationContext)) { // app is in foreground, broadcast the push message
            val pushNotification = Intent(Config.PUSH_NOTIFICATION)
            pushNotification.putExtra("message", message)
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification)
            val uiHandler = Handler(Looper.getMainLooper())
            uiHandler.post {
                if (!title.equals("MobiChat", ignoreCase = true)) {
                    showAlertMessage(message, title)
                } else { //No on screen message
                    val prefs =
                        getSharedPreferences("LoggedIn", Context.MODE_PRIVATE)
                    val restoredText = prefs.getString("loggedIn", null)
                    if (restoredText.equals("Yes", ignoreCase = true)) {
                        val am =
                            getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                        val taskInfo =
                            am.getRunningTasks(1)
                        val componentInfo = taskInfo[0].topActivity
                        Log.d(
                            TAG,
                            "CURRENT Activity ::" + taskInfo[0].topActivity!!.className + "   Package Name :  " + componentInfo!!.packageName
                        )
                        if (!taskInfo[0].topActivity!!.className.equals(
                                "com.mobiversa.ezy2pay.Chat.ChatWebview",
                                ignoreCase = true
                            ) && !taskInfo[0].topActivity!!.className.equals(
                                "com.mobiversa.ezy2pay.Chat.ChatFromNotification",
                                ignoreCase = true
                            )
                        ) {
                            showPopup(message, title)
                        }
                        //                            showAlertMessage(message, title);
                    }
                }
            }
        } else { // If the app is in background, firebase itself handles the notification
            Log.e("Firebase","Working in BackGround")
        }
        // play notification sound
        val notificationUtils =
            NotificationUtils(applicationContext)
        notificationUtils.playNotificationSound()
    }

    private fun showPopup(message: String, title: String) {
        try {
            val intent = Intent(this, DialogActivityForChat::class.java)
            intent.putExtra("notify_msg", message)
            intent.putExtra("notify_title", title)
            startActivity(intent)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun showAlertMessage(message: String, title: String) {
        try {
            val intent = Intent(this, ShowNotificationAcknowledgement::class.java)
            intent.putExtra("notify_msg", message)
            intent.putExtra("notify_title", title)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /*private void handleNotification(RemoteMessage.Notification RemoteMsgNotification) {
        String message = RemoteMsgNotification.getBody();
        String title = RemoteMsgNotification.getTitle();
        NotificationVO notificationVO = new NotificationVO();
        notificationVO.setTitle(title);
        notificationVO.setMessage(message);

        Intent resultIntent = new Intent(getApplicationContext(), SplashScreen.class);
        NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
        notificationUtils.displayNotification(notificationVO, resultIntent);
        notificationUtils.playNotificationSound();


    }*/
    private fun handleData(data: Map<String, String>) {
        val title = data[TITLE]
        val message = data[MESSAGE]
        val iconUrl = data[URL]
        val action = data[ACTION]
        val actionDestination =
            data[ACTION_DESTINATION]
        val notificationVO = NotificationVO()
        notificationVO.title = title
        notificationVO.message = message
        notificationVO.url = iconUrl
        notificationVO.action = action
        notificationVO.actionDestination = actionDestination
        Log.v("--handleData--", ""+title)
        if (actionDestination.equals("SplashScreen", ignoreCase = true)) {
            val resultIntent = Intent(applicationContext, SplashActivity::class.java)
            val notificationUtils =
                NotificationUtils(applicationContext)
            notificationUtils.displayNotification(notificationVO, resultIntent)
            notificationUtils.playNotificationSound()
        } else { // No on Screen Notification for Chat //Only Sound
            val notificationUtils =
                NotificationUtils(applicationContext)
            notificationUtils.playNotificationSound()
        }
    }



    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: " + token)

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(refreshedToken: String?) {
        val prefs = PreferenceHelper.defaultPrefs(this)
        prefs[FIRE_BASE_TOKEN] = refreshedToken
    }


}



