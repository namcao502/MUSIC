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
import java.io.IOException
import java.util.*

class MusicPlayerService: Service() {

    private var mediaPlayer: MediaPlayer? = null

    private val myBinder = MyBinder()

    private var listSong: List<Song>? = null

    private var songPosition = -1

    var playState = "Go"

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
        listSong = intent.getSerializableExtra("songService") as List<Song>
        songPosition = intent.getIntExtra("songPositionService", 0)
        playState = intent.getStringExtra("playState") as String
        //create new media player
        createMediaPlayer()

        return START_NOT_STICKY
    }


    private fun createMediaPlayer(){
        mediaPlayer = MediaPlayer()
        with(mediaPlayer!!) {
            setDataSource(listSong!![songPosition].filePath)
            prepare()
            setOnPreparedListener {
                start()
            }
        }
        sendNotification(songPosition)
    }

    private fun sendNotification(songPosition: Int) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID_1)
            .setContentTitle("MUSIC")
            .setContentText(listSong!![songPosition].name)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .build()
        startForeground(1, notification)
    }

    fun resume() {
        mediaPlayer!!.start()
    }

    fun pause() {
        mediaPlayer!!.pause()
    }

    fun previous() {
        songPosition -= 1
        val maxLength: Int = listSong!!.size
        if (songPosition < 0) {
            songPosition = maxLength - 1
        }
        if (playState == "Shuffle") {
            createRandomTrackPosition()
        } else {
            if (playState == "Loop") {
                songPosition += 1
                mediaPlayer!!.reset()
            }
        }
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
            try {
                createMediaPlayer()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            try {
                createMediaPlayer()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun next() {
        songPosition += 1
        val maxLength: Int = listSong!!.size
        if (songPosition > maxLength - 1) {
            songPosition = 0
        }
        if (playState == "Shuffle") {
            createRandomTrackPosition()
        } else {
            if (playState == "Loop") {
                songPosition -= 1
                mediaPlayer!!.reset()
            }
        }
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
            try {
                createMediaPlayer()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            try {
                createMediaPlayer()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun createRandomTrackPosition() {
        val limit = listSong!!.size
        val random = Random()
        val randomNumber = random.nextInt(limit)
        songPosition = randomNumber
    }

    fun getCurrentPosition(): Int{
        return mediaPlayer!!.currentPosition
    }

    fun getDuration(): Int{
        return mediaPlayer!!.duration
    }

    fun getCurrentSong(): Song{
        return listSong!![songPosition]
    }

    fun seekTo(position: Int){
        mediaPlayer!!.seekTo(position)
    }

    fun isPlaying(): Boolean{
        return mediaPlayer!!.isPlaying
    }
}