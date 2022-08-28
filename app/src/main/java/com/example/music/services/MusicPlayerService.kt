package com.example.music.services

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.music.MainApplication.Companion.CHANNEL_ID_1
import com.example.music.R
import com.example.music.models.Song
import com.example.music.ui.activities.SongPlayerActivity
import java.io.IOException
import java.util.*

class MusicPlayerService: Service() {

    var mediaPlayer: MediaPlayer? = null

    private val myBinder = MyBinder()

    private var song: Song? = null

    override fun onCreate() {
        super.onCreate()
    }

    inner class MyBinder : Binder() {
        fun getService(): MusicPlayerService = this@MusicPlayerService
    }

    override fun onBind(p0: Intent?): IBinder {
        return myBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer!!.stop()
        mediaPlayer!!.release()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        //get data from intent
        song = intent.getSerializableExtra("songService") as Song
        //create new media player
        createMediaPlayer(song!!)

        return START_NOT_STICKY
    }

    fun createMediaPlayer(song: Song){
        mediaPlayer = MediaPlayer()
        with(mediaPlayer!!) {
            setDataSource(song.filePath)
            prepare()
        }
        sendNotification(song)
    }

    private fun sendNotification(song: Song) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID_1)
            .setContentTitle(song.name)
            .setContentText(song.artists)
            .setSmallIcon(R.drawable.icons8_musical_notes_48)
            .build()
        startForeground(1, notification)
    }

    fun pause() {
        mediaPlayer!!.pause()
    }

    fun reset(){
        mediaPlayer!!.reset()
    }

    fun stop(){
        mediaPlayer!!.stop()
    }

    fun start(){
        mediaPlayer!!.start()
    }

    fun release(){
        mediaPlayer!!.release()
    }

    fun getCurrentDuration(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    fun getDuration(): Int{
        return mediaPlayer!!.duration
    }

    fun seekTo(position: Int){
        mediaPlayer!!.seekTo(position)
    }

    fun isPlaying(): Boolean{
        return mediaPlayer!!.isPlaying
    }

}