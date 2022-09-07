package com.example.music.services

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.example.music.R
import com.example.music.models.Song
import com.example.music.ui.activities.MainActivity

class MusicPlayerService: Service() {

    companion object {
        const val CHANNEL_ID_1 = "channel_1"
        const val CHANNEL_ID_2 = "channel_2"
        const val ACTION_PREVIOUS = 500
        const val ACTION_NEXT = 501
        const val ACTION_PLAY = 502
        const val ACTION_PAUSE = 503
    }

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
        val bundle: Bundle? = intent.extras
        if (bundle?.get("songService") != null){
            song = bundle.get("songService") as Song
            if (song != null){
                //create new media player
                createMediaPlayer(song!!)
            }
        }

        return START_NOT_STICKY
    }

    fun createMediaPlayer(song: Song){
        mediaPlayer = MediaPlayer()
        with(mediaPlayer!!) {
            setDataSource(song.filePath)
            prepare()
            start()
        }
        sendNotification(song)
    }

    private fun sendNotification(song: Song) {

        val mediaSessionCompat = MediaSessionCompat(this, "tag")

        // Create an explicit intent for an Activity in your app
//        val intent = Intent(this, MainActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        }
//        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID_1)
            .setContentTitle(song.name)
            .setContentText(song.artists)
            .setSmallIcon(R.drawable.icons8_musical_notes_48)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2)
                .setMediaSession(mediaSessionCompat.sessionToken))

        if (isPlaying()){
            notification
                .addAction(R.drawable.ic_baseline_skip_previous_24, "Previous", pendingIntent(this, ACTION_PREVIOUS))
                .addAction(R.drawable.ic_baseline_pause_circle_outline_24, "Pause", pendingIntent(this, ACTION_PAUSE))
                .addAction(R.drawable.ic_baseline_skip_next_24, "Next", pendingIntent(this, ACTION_NEXT))
        }
        else {
            notification
                .addAction(R.drawable.ic_baseline_skip_previous_24, "Previous", pendingIntent(this, ACTION_PREVIOUS))
                .addAction(R.drawable.ic_baseline_play_circle_outline_24, "Play", pendingIntent(this, ACTION_PLAY))
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