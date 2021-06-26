package com.mobiversa.ezy2pay.ui.fcm

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log


/**
 * Created by Karthik N on 10/04/17.
 */
class AlarmSoundService : Service() {
    //private MediaPlayer mediaPlayer;
/*@Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.v("startAlarm","started");
        //Start media player
        */
/*mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound);
        mediaPlayer.start();
        mediaPlayer.setLooping(true);//set looping true to run it infinitely*/
/*
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.v("AlarmDestroyed","stopped");
        //On destory stop and release the media player
       */
/* if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }*/
/*
    }*/
    override fun onStartCommand(
        intent: Intent,
        flags: Int,
        startId: Int
    ): Int { // if we are currently trying to get a location and the alarm manager has called this again,
        return START_STICKY
    }

    override fun onCreate() { // TODO Auto-generated method stub
        Log.v("<---AlarmService--->", "onCreate()")
    }

    override fun onBind(intent: Intent): IBinder? { // TODO Auto-generated method stub
        Log.v("<---AlarmService--->", "onBind()")
        return null
    }

    override fun onDestroy() { // TODO Auto-generated method stub
        super.onDestroy()
        Log.v("<---AlarmService--->", "onDestroy()")
    }

    override fun onUnbind(intent: Intent): Boolean { // TODO Auto-generated method stub
        Log.v("<---AlarmService--->", "onUnbind()")
        return super.onUnbind(intent)
    }
}
