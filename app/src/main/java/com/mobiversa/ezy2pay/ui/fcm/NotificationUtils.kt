package com.mobiversa.ezy2pay.ui.fcm

import android.annotation.SuppressLint
import android.app.*
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.text.Html
import androidx.core.app.NotificationCompat
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.network.response.NotificationVO
import com.mobiversa.ezy2pay.ui.chat.ChatNotifyActivity
import com.mobiversa.ezy2pay.ui.splash.SplashActivity
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


/**
 * Created by Karthik on 13 Nov 2017 013.
 */
class NotificationUtils(private val mContext: Context) {
    var activityMap: MutableMap<String, Class<*>?> =
        HashMap()

    /**
     * Displays notification based on parameters
     *
     * @param notificationVO
     * @param resultIntent
     */
    fun displayNotification(notificationVO: NotificationVO, resultIntent: Intent) {
        var resultIntent = resultIntent
        run {
            val message: String = notificationVO.message.toString()
            val title: String = notificationVO.title.toString()
            val iconUrl: String = notificationVO.url.toString()
            val action: String = notificationVO.action.toString()
            val destination: String = notificationVO.actionDestination.toString()
            var iconBitMap: Bitmap? = null
            if (iconUrl != null) {
                iconBitMap = getBitmapFromURL(iconUrl)
            }
            val icon: Int = R.drawable.logo_new_small
            val resultPendingIntent: PendingIntent
            if (URL == action) {
                val notificationIntent = Intent(Intent.ACTION_VIEW, Uri.parse(destination))
                resultPendingIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0)
            } else if (ACTIVITY == action && activityMap.containsKey(
                    destination
                )
            ) {
                resultIntent = Intent(mContext, activityMap[destination])
                resultPendingIntent = PendingIntent.getActivity(
                    mContext,
                    0,
                    resultIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT
                )
            } else {
                resultIntent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                resultPendingIntent = PendingIntent.getActivity(
                    mContext,
                    0,
                    resultIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT
                )
            }
            val mBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
                mContext,
                CHANNEL_ID
            )
            val notification: Notification
            notification =
                if (iconBitMap == null) { //When Inbox Style is applied, user can expand the notification
                    val inboxStyle: NotificationCompat.InboxStyle = NotificationCompat.InboxStyle()
                    inboxStyle.addLine(message)
                    mBuilder.setSmallIcon(icon).setTicker(title).setWhen(0)
                        .setAutoCancel(true)
                        .setContentTitle(title)
                        .setContentIntent(resultPendingIntent)
                        .setStyle(inboxStyle)
                        .setSmallIcon(R.drawable.logo_new_small)
                        .setLargeIcon(BitmapFactory.decodeResource(mContext.resources, icon))
                        .setContentText(message)
                        .build()
                } else { //If Bitmap is created from URL, show big icon
                    val bigPictureStyle: NotificationCompat.BigPictureStyle =
                        NotificationCompat.BigPictureStyle()
                    bigPictureStyle.setBigContentTitle(title)
                    bigPictureStyle.setSummaryText(Html.fromHtml(message).toString())
                    bigPictureStyle.bigPicture(iconBitMap)
                    mBuilder.setSmallIcon(icon).setTicker(title).setWhen(0)
                        .setAutoCancel(true)
                        .setContentTitle(title)
                        .setContentIntent(resultPendingIntent)
                        .setStyle(bigPictureStyle)
                        .setSmallIcon(R.drawable.logo_new_small)
                        .setLargeIcon(BitmapFactory.decodeResource(mContext.resources, icon))
                        .setContentText(message)
                        .build()
                }
            val notificationManager =
                mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            //All notifications should go through NotificationChannel on Android 26 & above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager.createNotificationChannel(channel)
            }
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    /**
     * Downloads push notification image before displaying it in
     * the notification tray
     *
     * @param strURL : URL of the notification Image
     * @return : BitMap representation of notification Image
     */
    private fun getBitmapFromURL(strURL: String): Bitmap? {
        return try {
            val url = URL(strURL)
            val connection =
                url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Playing notification sound
     */
    fun playNotificationSound() {
        try {
            val alarmSound = Uri.parse(
                ContentResolver.SCHEME_ANDROID_RESOURCE
                        + "://" + mContext.packageName + "/raw/notification"
            )
            val r = RingtoneManager.getRingtone(mContext, alarmSound)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 200
        private const val PUSH_NOTIFICATION = "pushNotification"
        private const val CHANNEL_ID = "myChannel"
        private const val CHANNEL_NAME = "myChannelName"
        private const val URL = "url"
        private const val ACTIVITY = "activity"
        /**
         * Method checks if the app is in background or not
         */
        @SuppressLint("NewApi")
        fun isAppIsInBackground(context: Context): Boolean {
            var isInBackground = true
            val am =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                val runningProcesses =
                    am.runningAppProcesses
                for (processInfo in runningProcesses) {
                    if (processInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        for (activeProcess in processInfo.pkgList) {
                            if (activeProcess == context.packageName) {
                                isInBackground = false
                            }
                        }
                    }
                }
            } else {
                val taskInfo = am.getRunningTasks(1)
                val componentInfo = taskInfo[0].topActivity
                if (componentInfo!!.packageName == context.packageName) {
                    isInBackground = false
                }
            }
            return isInBackground
        }
    }

    init {
        //Populate activity map
        activityMap["SplashScreen"] = SplashActivity::class.java
        activityMap["ChatWebview"] = ChatNotifyActivity::class.java
    }
}