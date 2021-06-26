package com.mobiversa.ezy2pay.ui.fcm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.*


/**
 * Created by Karthik N
 * For Mobiversa Sdn Bhd
 */
object CommonStartStop {
    //Pending intent instance
    private var pendingIntent: PendingIntent? = null
    //Alarm Request Code
    private const val ALARM_REQUEST_CODE = 133
    private const val INT_MAX_SESSION_TIMEOUT = 15
    fun runTimerService(ctx: Context) { /* Retrieve a PendingIntent that will perform a broadcast */
        val alarmIntent = Intent(ctx, AlarmReceiver::class.java)
        pendingIntent =
            PendingIntent.getBroadcast(ctx,
                ALARM_REQUEST_CODE, alarmIntent, 0)
        // get a Calendar object with current time
        val cal = Calendar.getInstance()
        // add alarmTriggerTime seconds to the calendar object
        cal.add(Calendar.MINUTE,
            INT_MAX_SESSION_TIMEOUT
        )
        val manager =
            ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager //get instance of alarm manager
        manager[AlarmManager.RTC_WAKEUP, cal.timeInMillis] =
            pendingIntent //set alarm manager with entered timer by converting into milliseconds
        Log.v("<---TimerStarted--->", "started")
    }

    //Stop/Cancel alarm manager
    fun stopAlarmManager(ctx: Context) {
        val manager =
            ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.cancel(pendingIntent) //cancel the alarm manager of the pending intent
        //Stop the Media Player Service to stop sound
        ctx.stopService(Intent(ctx, AlarmSoundService::class.java))
        Log.v("<---TimerStopped--->", "stopped")
    }
}