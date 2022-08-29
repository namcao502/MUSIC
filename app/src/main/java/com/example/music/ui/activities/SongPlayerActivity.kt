package com.example.music.ui.activities

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import com.example.music.R
import com.example.music.databinding.ActivitySongPlayer2Binding
import com.example.music.databinding.ActivitySongPlayerBinding
import com.example.music.models.Song
import com.example.music.services.MusicPlayerService
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class SongPlayerActivity : AppCompatActivity(), ServiceConnection {

    private lateinit var binding: ActivitySongPlayerBinding

    var songList: List<Song>? = null
    var songPosition = -1

    var audioManager: AudioManager? = null

    var playState = "Go"

//    var rotatingImageAnimation: ObjectAnimator? = null
    var musicPlayerService: MusicPlayerService? = null

    var isServiceConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySongPlayerBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        songList = intent.getSerializableExtra("songList") as List<Song>
        songPosition = intent.getIntExtra("songPosition", 0)

        initState()

//        setTime()
//        updateProgress()
        listener()
    }

    private fun listener() {
        binding.playStateBtn.setOnClickListener{
            if (playState == "Loop") {
                playState = "Shuffle"
                binding.playStateBtn.setImageResource(R.drawable.icons8_shuffle_64)
            } else {
                if (playState == "Shuffle") {
                    playState = "Go"
                    binding.playStateBtn.setImageResource(R.drawable.icons8_arrow_64)
                } else {
                    if (playState == "Go") {
                        playState = "Loop"
                        binding.playStateBtn.setImageResource(R.drawable.icons8_repeat_64)
                    }
                }
            }
        }
        binding.nextBtn.setOnClickListener{ next() }
        binding.previousBtn.setOnClickListener{ previous() }
        binding.playPauseBtn.setOnClickListener{
            if (musicPlayerService!!.isPlaying()) {
                pause()
            } else {
                play()
            }
        }
        binding.songSb.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                musicPlayerService!!.seekTo(seekBar.progress)
                updateProgress()
            }
        })
        try {
            audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            binding.volumeSb.max = audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            binding.volumeSb.progress = audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
            binding.volumeSb.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onStopTrackingTouch(arg0: SeekBar) {}
                override fun onStartTrackingTouch(arg0: SeekBar) {}
                override fun onProgressChanged(arg0: SeekBar, progress: Int, arg2: Boolean) {
                    audioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
//        setCompleteListener()
    }

    private fun previous() {
        songPosition -= 1
        val maxLength: Int = songList!!.size
        if (songPosition < 0) {
            songPosition = maxLength - 1
        }
        if (playState == "Shuffle") {
            createRandomTrackPosition()
        } else {
            if (playState == "Loop") {
                songPosition += 1
                musicPlayerService!!.reset()
            }
        }
        if (musicPlayerService!!.isPlaying()) {
            musicPlayerService!!.stop()
            musicPlayerService!!.release()
            try {
                musicPlayerService!!.createMediaPlayer(songList!![songPosition])
            } catch (e: IOException) {
                e.printStackTrace()
            }
            musicPlayerService!!.start()
            binding.playPauseBtn.setImageResource(R.drawable.icons8_pause_64)
        } else {
            try {
                musicPlayerService!!.createMediaPlayer(songList!![songPosition])
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        setTime()
        loadUI()
    }

    private fun next() {
        songPosition += 1
        val maxLength: Int = songList!!.size
        if (songPosition > maxLength - 1) {
            songPosition = 0
        }
        if (playState == "Shuffle") {
            createRandomTrackPosition()
        } else {
            if (playState == "Loop") {
                songPosition -= 1
                musicPlayerService!!.reset()
            }
        }
        if (musicPlayerService!!.isPlaying()) {
            musicPlayerService!!.stop()
            musicPlayerService!!.release()
            try {
                musicPlayerService!!.createMediaPlayer(songList!![songPosition])
            } catch (e: IOException) {
                e.printStackTrace()
            }
            musicPlayerService!!.start()
            binding.playPauseBtn.setImageResource(R.drawable.icons8_pause_64)
        } else {
            try {
                musicPlayerService!!.createMediaPlayer(songList!![songPosition])
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        setTime()
        loadUI()
    }

    private fun createRandomTrackPosition() {
        val limit: Int = songList!!.size
        val random = Random()
        val randomNumber = random.nextInt(limit)
        songPosition = randomNumber
    }

    private fun play(){
        musicPlayerService!!.start()
        binding.playPauseBtn.setImageResource(R.drawable.icons8_pause_64)
        setTime()
        updateProgress()
    }

    private fun pause(){
        musicPlayerService!!.pause()
        binding.playPauseBtn.setImageResource(R.drawable.icons8_play_64)
        setTime()
        updateProgress()
    }

    private fun initState() {
        val intent = Intent(this, MusicPlayerService::class.java)
        intent.putExtra("songService", songList!![songPosition])
        startService(intent)
        bindService(intent, this, BIND_AUTO_CREATE)
    }

    private fun setTime() {
        val sdf = SimpleDateFormat("mm:ss")
        binding.endTxt.text = sdf.format(musicPlayerService!!.getDuration())
        binding.songSb.max = musicPlayerService!!.getDuration()
    }

    private fun loadUI(){
        binding.titleTxt.text = songList!![songPosition].name
        binding.artistTxt.text = songList!![songPosition].artists
        binding.songSb.max = musicPlayerService!!.getDuration()
        updateProgress()
    }

    private fun updateProgress() {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run(){
                val currentPosition = musicPlayerService!!.getCurrentDuration()
                val sdf = SimpleDateFormat("mm:ss")
                binding.startTxt.text = sdf.format(currentPosition)
                binding.songSb.progress = currentPosition
                handler.postDelayed(this, 1000)
            }
        }, 1000)
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        stopService(Intent(this, MusicPlayerService::class.java))
//        if (isServiceConnected){
//            unbindService(this)
//            isServiceConnected = false
//        }
//    }

    private fun setCompleteListener(){
        musicPlayerService!!.mediaPlayer!!.setOnCompletionListener {
            if (playState == "Loop"){
                musicPlayerService!!.createMediaPlayer(songList!![songPosition])
                musicPlayerService!!.start()
            }
            else {
                if (playState == "Shuffle") {
                    createRandomTrackPosition()
                    try {
                        musicPlayerService!!.createMediaPlayer(songList!![songPosition])
                        musicPlayerService!!.start()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } else {
                    if (playState == "Go") {
                        next()
                        musicPlayerService!!.start()
                    }
                }
            }
        }
    }

    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        val myBinder = p1 as MusicPlayerService.MyBinder
        musicPlayerService = myBinder.getService()
        isServiceConnected = true
        setTime()
        loadUI()
        musicPlayerService!!.start()
        setCompleteListener()
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
//        MusicPlayerService = null
        isServiceConnected = false
    }
}