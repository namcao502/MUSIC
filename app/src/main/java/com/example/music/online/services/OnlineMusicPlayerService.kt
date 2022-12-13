package com.example.music.online.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.example.music.R
import com.example.music.online.data.models.OnlineArtist
import com.example.music.online.data.models.OnlineSong
import com.example.music.online.viewModels.OnlineArtistViewModel
import com.example.music.utils.MusicPlayerReceiver
import com.example.music.utils.PlayerState
import com.example.music.utils.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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
            setDataSource(song.filePath)
            prepare()
            start()
        }
        sendNotification(currentSong!!)
    }

    fun recreateNotification(artist: String){

        val channelId = createNotificationChannel(CHANNEL_ID_1, "My Background Service")

        GlobalScope.launch {

            val image = withContext(Dispatchers.IO) {
                Glide.with(this@OnlineMusicPlayerService).asBitmap().load(currentSong!!.imgFilePath).submit().get()
            }

            val notification = NotificationCompat.Builder(this@OnlineMusicPlayerService, channelId)
                .setContentTitle(currentSong!!.name)
                .setContentText(artist)
                .setLargeIcon(image)
                .setSmallIcon(R.drawable.ic_baseline_music_note_24)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2))
                .setStyle(NotificationCompat.BigPictureStyle()
                    .bigPicture(image)
                    .bigLargeIcon(null))

            if (isPlaying()){
                notification.addAction(R.drawable.ic_baseline_skip_previous_24,
                    "Previous",
                    pendingIntent(this@OnlineMusicPlayerService, ACTION_PREVIOUS))
                    .addAction(R.drawable.ic_baseline_pause_circle_outline_24,
                        "Pause",
                        pendingIntent(this@OnlineMusicPlayerService, ACTION_PAUSE))
                    .addAction(R.drawable.ic_baseline_skip_next_24,
                        "Next",
                        pendingIntent(this@OnlineMusicPlayerService, ACTION_NEXT))
            }
            else {
                notification.addAction(R.drawable.ic_baseline_skip_previous_24,
                    "Previous",
                    pendingIntent(this@OnlineMusicPlayerService, ACTION_PREVIOUS))
                    .addAction(R.drawable.ic_baseline_play_circle_outline_24,
                        "Play",
                        pendingIntent(this@OnlineMusicPlayerService, ACTION_PAUSE))
                    .addAction(R.drawable.ic_baseline_skip_next_24,
                        "Next",
                        pendingIntent(this@OnlineMusicPlayerService, ACTION_NEXT))
            }

            val fNotification = notification.build()
            startForeground(1, fNotification)
        }
    }

    private fun sendNotification(song: OnlineSong) {

        val channelId = createNotificationChannel(CHANNEL_ID_1, "My Background Service")

        GlobalScope.launch {

            val image = withContext(Dispatchers.IO) {
                Glide.with(this@OnlineMusicPlayerService).asBitmap().load(song.imgFilePath).submit().get()
            }

            val notification = NotificationCompat.Builder(this@OnlineMusicPlayerService, channelId)
                .setContentTitle(song.name)
                .setContentText(PlayerState.artistText)
                .setLargeIcon(image)
                .setSmallIcon(R.drawable.ic_baseline_music_note_24)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2))
                .setStyle(NotificationCompat.BigPictureStyle()
                    .bigPicture(image)
                    .bigLargeIcon(null))
//            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
//                .setShowActionsInCompactView(0, 1, 2)
//                .setMediaSession(mediaSessionCompat.sessionToken))

            if (isPlaying()){
                notification.addAction(R.drawable.ic_baseline_skip_previous_24,
                        "Previous",
                        pendingIntent(this@OnlineMusicPlayerService, ACTION_PREVIOUS))
                    .addAction(R.drawable.ic_baseline_pause_circle_outline_24,
                        "Pause",
                        pendingIntent(this@OnlineMusicPlayerService, ACTION_PAUSE))
                    .addAction(R.drawable.ic_baseline_skip_next_24,
                        "Next",
                        pendingIntent(this@OnlineMusicPlayerService, ACTION_NEXT))
            }
            else {
                notification.addAction(R.drawable.ic_baseline_skip_previous_24,
                        "Previous",
                        pendingIntent(this@OnlineMusicPlayerService, ACTION_PREVIOUS))
                    .addAction(R.drawable.ic_baseline_play_circle_outline_24,
                        "Play",
                        pendingIntent(this@OnlineMusicPlayerService, ACTION_PAUSE))
                    .addAction(R.drawable.ic_baseline_skip_next_24,
                        "Next",
                        pendingIntent(this@OnlineMusicPlayerService, ACTION_NEXT))
            }

            val fNotification = notification.build()
            startForeground(1, fNotification)
        }
    }

    private fun pendingIntent(context: Context, action: Int): PendingIntent{
        val intent = Intent(this, MusicPlayerReceiver::class.java)
        intent.putExtra("action_music", action)
        return PendingIntent.getBroadcast(context.applicationContext, action, intent, PendingIntent.FLAG_IMMUTABLE)
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

    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val channel = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_NONE)

        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)
        return channelId
    }

}