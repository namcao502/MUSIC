package com.example.music

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication: Application() {

    companion object{
        val CHANNEL_ID_1 = "channel_1"
        val CHANNEL_ID_2 = "channel_2"
    }

    val ACTION_PREVIOUS = "action_previous"
    val ACTION_NEXT = "action_next"
    val ACTION_PLAY = "action_play"

    override fun onCreate() {
        super.onCreate()
        //for push notification
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel1 = NotificationChannel(CHANNEL_ID_1, "Channel(1)", NotificationManager.IMPORTANCE_HIGH)
            channel1.description = "Channel 1 des"

            val channel2 = NotificationChannel(CHANNEL_ID_2, "Channel(2)", NotificationManager.IMPORTANCE_HIGH)
            channel2.description = "Channel 2 des"

            val manager = getSystemService(NotificationManager::class.java)
            if (manager != null){
                manager.createNotificationChannel(channel1)
                manager.createNotificationChannel(channel2)
            }

        }
    }
}