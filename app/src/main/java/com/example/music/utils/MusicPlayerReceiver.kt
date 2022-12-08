package com.example.music.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MusicPlayerReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val actionMusic = intent.getIntExtra("action_music", 0)
//
//        val intentService = Intent(context, MainActivity::class.java)
//        intentService.putExtra("action_music_service", actionMusic)
//
//        context.startService(intentService)

        context.sendBroadcast(Intent("TRACKS_TRACKS").putExtra("action_music", actionMusic))
    }
}