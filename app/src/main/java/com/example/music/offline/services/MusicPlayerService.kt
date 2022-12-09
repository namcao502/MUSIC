package com.example.music.offline.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.example.music.R
import com.example.music.offline.data.models.Song
import com.example.music.utils.MusicPlayerReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MusicPlayerService: Service() {

    companion object {
        const val CHANNEL_ID_1 = "channel_1"
        const val ACTION_PREVIOUS = 500
        const val ACTION_NEXT = 501
        const val ACTION_PAUSE = 503
    }

    var mediaPlayer: MediaPlayer? = null
    private val myBinder = MyBinder()
    private var initialSong: Song? = null
    private var currentSong: Song? = null

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
        val bundle: Bundle? = intent.extras
        if (bundle?.get("songService") != null){
            initialSong = bundle.get("songService") as Song
            if (initialSong != null){
                //create new media player
                createMediaPlayer(initialSong!!)
            }
        }

        return START_NOT_STICKY
    }

    fun createMediaPlayer(song: Song){
        currentSong = song
        mediaPlayer = MediaPlayer()
        with(mediaPlayer!!) {
            setDataSource(song.filePath)
            prepare()
            start()
        }
        sendNotification(currentSong!!)
    }

    private fun sendNotification(song: Song) {

        GlobalScope.launch {
            val channelId = createNotificationChannel(CHANNEL_ID_1, "My Background Service")

            val albumId: String = song.album_id
            val albumUri: Uri = Uri.parse("content://media/external/audio/albumart")
            val uri: Uri = ContentUris.withAppendedId(albumUri, albumId.toLong())

            val image = withContext(Dispatchers.IO) {
                try {
                    Glide.with(this@MusicPlayerService).asBitmap().load(uri).submit().get()
                }
                catch (e: Exception){
                    BitmapFactory.decodeResource(resources, R.drawable.music_default)
                }
            }

            val notification = NotificationCompat.Builder(this@MusicPlayerService, channelId)
                .setContentTitle(song.name)
                .setContentText(song.artists)
                .setSmallIcon(R.drawable.ic_baseline_music_note_24)
                .setLargeIcon(image)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setStyle(NotificationCompat.BigPictureStyle()
                    .bigPicture(image)
                    .bigLargeIcon(null))

            if (isPlaying()){
                notification.addAction(R.drawable.ic_baseline_skip_previous_24,
                        "Previous",
                        pendingIntent(this@MusicPlayerService, ACTION_PREVIOUS))
                    .addAction(R.drawable.ic_baseline_pause_circle_outline_24,
                        "Pause",
                        pendingIntent(this@MusicPlayerService, ACTION_PAUSE))
                    .addAction(R.drawable.ic_baseline_skip_next_24,
                        "Next",
                        pendingIntent(this@MusicPlayerService, ACTION_NEXT))
            }
            else {
                notification.addAction(R.drawable.ic_baseline_skip_previous_24,
                    "Previous",
                    pendingIntent(this@MusicPlayerService, ACTION_PREVIOUS))
                    .addAction(R.drawable.ic_baseline_play_circle_outline_24,
                        "Play",
                        pendingIntent(this@MusicPlayerService, ACTION_PAUSE))
                    .addAction(R.drawable.ic_baseline_skip_next_24,
                        "Next",
                        pendingIntent(this@MusicPlayerService, ACTION_NEXT))
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{

        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE)

        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)
        return channelId
    }

}