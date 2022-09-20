package com.example.music.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.music.R
import com.example.music.data.models.online.OnlineSong

class OnlineMusicPlayerService: Service() {

    companion object {
        const val CHANNEL_ID_1 = "channel_2"
        const val ACTION_PREVIOUS = 400
        const val ACTION_NEXT = 401
        const val ACTION_PAUSE = 403
    }

    var mediaPlayer: MediaPlayer? = null
    private val myBinder = MyBinder()
    private var initialSong: OnlineSong? = null
    private var currentSong: OnlineSong? = null

    override fun onCreate() {
        super.onCreate()
    }

    inner class MyBinder : Binder() {
        fun getService(): OnlineMusicPlayerService = this@OnlineMusicPlayerService
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
        val bundle: Bundle? = intent.extras
        if (bundle?.get("songService") != null){
            initialSong = bundle.get("songService") as OnlineSong
            if (initialSong != null){
                //create new media player
                createMediaPlayer(initialSong!!)
            }
        }

        return START_NOT_STICKY
    }

    fun createMediaPlayer(song: OnlineSong){
        currentSong = song
        mediaPlayer = MediaPlayer()
        with(mediaPlayer!!) {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            Log.i("TAG502", "createMediaPlayer: ${song.filePath}")
            setDataSource(song.filePath)
            prepare()
            start()
        }
        sendNotification(currentSong!!)
    }

    private fun sendNotification(song: OnlineSong) {

        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(CHANNEL_ID_1, "My Background Service")
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }

        val mediaSessionCompat = MediaSessionCompat(this, "tag")

        // Create an explicit intent for an Activity in your app
//        val intent = Intent(this, MainActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        }
//        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(song.name)
//            .setContentText(song.artists)
            .setSmallIcon(R.drawable.ic_baseline_music_note_24)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2)
                .setMediaSession(mediaSessionCompat.sessionToken))

        if (isPlaying()){
            notification.addAction(R.drawable.ic_baseline_skip_previous_24, "Previous", pendingIntent(this, ACTION_PREVIOUS))
                .addAction(R.drawable.ic_baseline_pause_circle_outline_24, "Pause", pendingIntent(this, ACTION_PAUSE))
                .addAction(R.drawable.ic_baseline_skip_next_24, "Next", pendingIntent(this, ACTION_NEXT))
        }
        else {
            notification.addAction(R.drawable.ic_baseline_skip_previous_24, "Previous", pendingIntent(this, ACTION_PREVIOUS))
                .addAction(R.drawable.ic_baseline_play_circle_outline_24, "Pause", pendingIntent(this, ACTION_PAUSE))
                .addAction(R.drawable.ic_baseline_skip_next_24, "Next", pendingIntent(this, ACTION_NEXT))
        }

        val fNotification = notification.build()

        startForeground(1, fNotification)
    }

    private fun pendingIntent(context: Context, action: Int): PendingIntent{
        val intent = Intent(this, MusicPlayerReceiver::class.java)
        intent.putExtra("action_music", action)
        return PendingIntent.getBroadcast(context.applicationContext, action, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun pause() {
        mediaPlayer!!.pause()
        sendNotification(currentSong!!)
    }

    fun reset(){
        mediaPlayer!!.reset()
        sendNotification(currentSong!!)
    }

    fun stop(){
        mediaPlayer!!.stop()
    }

    fun start(){
        mediaPlayer!!.start()
        sendNotification(currentSong!!)
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val channel = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_NONE)

        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)
        return channelId
    }

}