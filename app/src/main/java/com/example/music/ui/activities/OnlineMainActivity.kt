package com.example.music.ui.activities

import android.content.*
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.example.music.R
import com.example.music.databinding.ActivityMainBinding
import com.example.music.databinding.ActivityOnlineMainBinding
import com.example.music.models.OnlinePlaylist
import com.example.music.models.OnlineSong
import com.example.music.models.Song
import com.example.music.services.MusicPlayerService
import com.example.music.services.OnlineMusicPlayerService
import com.example.music.ui.adapters.OnlineSongInPlaylistAdapter
import com.example.music.ui.adapters.ViewPagerAdapter
import com.example.music.ui.fragments.OnlinePlaylistFragment
import com.example.music.ui.fragments.OnlineSongFragment
import com.example.music.ui.fragments.PlaylistFragment
import com.example.music.ui.fragments.SongFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.lang.IllegalStateException
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class OnlineMainActivity
    : AppCompatActivity(),
    ServiceConnection,
    OnlineSongFragment.SongFromAdapterClick,
    OnlineSongInPlaylistAdapter.ItemSongInPlaylistClickListener{

    private lateinit var binding: ActivityOnlineMainBinding

    private lateinit var viewPagerChart: ViewPagerAdapter

    private var songFragment: OnlineSongFragment = OnlineSongFragment(this)
    private var playlistFragment: OnlinePlaylistFragment = OnlinePlaylistFragment(this)
    private var fragmentList: MutableList<Fragment> = mutableListOf(songFragment, playlistFragment)

    private val tabLayoutTitles: ArrayList<String> = arrayListOf("Song", "Playlist")

    var songList: List<OnlineSong>? = null
    var songPosition = -1

    var audioManager: AudioManager? = null

    private var playState = "Go"

    var musicPlayerService: OnlineMusicPlayerService? = null

    var isServiceConnected = false

    private var iBinder: OnlineMusicPlayerService.MyBinder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnlineMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //SET ADAPTER
        viewPagerChart = ViewPagerAdapter(supportFragmentManager, lifecycle, fragmentList)
        binding.viewPagerMain.adapter = viewPagerChart

        //SET TAB TITLE AND MAP WITH FRAGMENT
        TabLayoutMediator(binding.tabLayoutMain, binding.viewPagerMain) { tab, position ->
            tab.text = tabLayoutTitles[position]
        }.attach()

        BottomSheetBehavior.from(binding.bottomSheet).apply {

            peekHeight = 200
            this.state = BottomSheetBehavior.STATE_COLLAPSED

            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback(){
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_COLLAPSED){
                        showMiniMenu(true)
                        this@OnlineMainActivity
                    }
                    if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                        showMiniMenu(false)
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {

                }

            })
        }


    }

    private fun showMiniMenu(check: Boolean){
        if (check){
            binding.miniPlayerLayout.visibility = View.VISIBLE
        }
        else {
            binding.miniPlayerLayout.visibility = View.GONE
        }
    }

    private fun listener() {

        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)

        binding.miniNextBtn.setOnClickListener {
            next()
        }

        binding.miniPreviousBtn.setOnClickListener {
            previous()
        }

        binding.miniPlayPauseBtn.setOnClickListener {
            if (musicPlayerService!!.isPlaying()) {
                pause()
            } else {
                play()
            }
        }

        binding.miniPlayerLayout.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

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
        binding.songSb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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
            binding.volumeSb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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

        } else {
            try {
                musicPlayerService!!.createMediaPlayer(songList!![songPosition])
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        binding.playPauseBtn.setImageResource(R.drawable.icons8_pause_64)
        binding.miniPlayPauseBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
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

        } else {
            try {
                musicPlayerService!!.createMediaPlayer(songList!![songPosition])
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        binding.playPauseBtn.setImageResource(R.drawable.icons8_pause_64)
        binding.miniPlayPauseBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
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
        binding.miniPlayPauseBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
        setTime()
        updateProgress()
    }

    private fun pause(){
        musicPlayerService!!.pause()
        binding.playPauseBtn.setImageResource(R.drawable.icons8_play_64)
        binding.miniPlayPauseBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
        setTime()
        updateProgress()
    }

    private fun initState() {
        val intent = Intent(this, OnlineMusicPlayerService::class.java)
        intent.putExtra("songService", songList!![songPosition])
        Log.i("TAG502", "initState: ${songList!![songPosition]}")
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
//        binding.artistTxt.text = songList!![songPosition].artists
        binding.songSb.max = musicPlayerService!!.getDuration()
        binding.miniSongTitle.text = songList!![songPosition].name
//        binding.miniSongArtist.text = songList!![songPosition].artists
        updateProgress()
    }

    private fun updateProgress() {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run(){
                try{
                    val currentPosition = musicPlayerService!!.getCurrentDuration()
                    val sdf = SimpleDateFormat("mm:ss")
                    binding.startTxt.text = sdf.format(currentPosition)
                    binding.songSb.progress = currentPosition
                    handler.postDelayed(this, 1000)
                }
                catch (error: IllegalStateException){
                    handler.removeCallbacksAndMessages(null)
                }
            }
        }, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (musicPlayerService != null){
            musicPlayerService!!.pause()
        }
        stopService(Intent(this, OnlineMusicPlayerService::class.java))
        if (isServiceConnected){
            unbindService(this)
            isServiceConnected = false
        }
    }

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
        val myBinder = p1 as OnlineMusicPlayerService.MyBinder
        iBinder = myBinder
        musicPlayerService = myBinder.getService()
        isServiceConnected = true
        setTime()
        loadUI()
        musicPlayerService!!.start()
        setCompleteListener()
        listener()

        registerReceiver(broadcastReceiver, IntentFilter("TRACKS_TRACKS"))
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        musicPlayerService = null
        isServiceConnected = false
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.extras!!.getInt("action_music")) {
                OnlineMusicPlayerService.ACTION_PREVIOUS -> previous()
                OnlineMusicPlayerService.ACTION_PAUSE -> {
                    if (musicPlayerService!!.isPlaying()) {
                        pause()
                    } else {
                        play()
                    }
                }
                OnlineMusicPlayerService.ACTION_NEXT -> next()
            }
        }
    }

    override fun callBackFromSongFragment(songs: List<OnlineSong>, position: Int) {
        songList = songs
        songPosition = position
        binding.miniPlayPauseBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
        if (!isServiceConnected){
            initState()
            binding.bottomSheet.visibility = View.VISIBLE
        }
        else{
            musicPlayerService!!.stop()
            musicPlayerService!!.release()
            musicPlayerService!!.createMediaPlayer(songList!![songPosition])
            musicPlayerService!!.start()
            setTime()
            loadUI()
            setCompleteListener()
            listener()
        }
        registerReceiver(broadcastReceiver, IntentFilter("TRACKS_TRACKS"))
    }

    override fun callBackFromSongInPlaylist(songList: List<OnlineSong>, position: Int) {
        this.songList = songList
        songPosition = position
        binding.miniPlayPauseBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
        if (!isServiceConnected){
            initState()
            binding.bottomSheet.visibility = View.VISIBLE
        }
        else{
            musicPlayerService!!.stop()
            musicPlayerService!!.release()
            musicPlayerService!!.createMediaPlayer(this.songList!![songPosition])
            musicPlayerService!!.start()
            setTime()
            loadUI()
            setCompleteListener()
            listener()
        }
    }

    override fun callBackFromMenuSongInPlaylist(
        action: String,
        songList: List<OnlineSong>,
        position: Int,
        playlist: OnlinePlaylist
    ) {
        TODO("Not yet implemented")
    }

}