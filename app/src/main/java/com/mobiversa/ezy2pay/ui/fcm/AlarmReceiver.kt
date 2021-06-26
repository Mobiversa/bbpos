package com.mobiversa.ezy2pay.ui.fcm

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.legacy.content.WakefulBroadcastReceiver.startWakefulService


/**
 * Created by Karthik on 09/04/17.
 */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(context, "Your Session has been expired", Toast.LENGTH_LONG).show()
        //Stop sound service to play sound for alarm
/*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);*/
//        } else {
        context.startService(Intent(context, AlarmSoundService::class.java))
        //        }
//This will send a notification message and show notification in notification tray
        val comp = ComponentName(
            context.packageName,
            AlarmNotificationService::class.java.name
        )
        startWakefulService(context, intent.setComponent(comp))
    }
}