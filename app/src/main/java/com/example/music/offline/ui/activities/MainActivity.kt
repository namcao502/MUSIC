package com.example.music.offline.ui.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.*
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.music.R
import com.example.music.databinding.ActivityMainBinding
import com.example.music.offline.data.models.Playlist
import com.example.music.offline.data.models.Song
import com.example.music.offline.data.models.SongPlaylistCrossRef
import com.example.music.offline.services.MusicPlayerService
import com.example.music.offline.ui.adapters.DialogPlaylistAdapter
import com.example.music.offline.ui.adapters.SongInPlaylistAdapter
import com.example.music.offline.ui.adapters.ViewPagerAdapter
import com.example.music.offline.ui.fragments.PlaylistFragment
import com.example.music.offline.ui.fragments.SongFragment
import com.example.music.offline.viewModels.PlaylistViewModel
import com.example.music.offline.viewModels.SongInPlaylistViewModel
import com.example.music.online.services.OnlineMusicPlayerService
import com.example.music.utils.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MainActivity:
    AppCompatActivity(),
    ServiceConnection,
    SongFragment.SongFromAdapterClick,
    SongInPlaylistAdapter.ItemSongInPlaylistClickListener,
    DialogPlaylistAdapter.ItemClickListener{

    private lateinit var binding: ActivityMainBinding

    private var songFragment: SongFragment = SongFragment(this)
    private var playlistFragment: PlaylistFragment = PlaylistFragment(this)
    private var fragmentList: MutableList<Fragment> = mutableListOf(songFragment, playlistFragment)

    private lateinit var viewPagerChart: ViewPagerAdapter

    private val tabLayoutTitles: ArrayList<String> = arrayListOf("Song", "Playlist")

    var songList: List<Song>? = null
    var songPosition = -1

    var audioManager: AudioManager? = null

    private var playState = "Go"

    var musicPlayerService: MusicPlayerService? = null

    var isServiceConnected = false

    private var iBinder: MusicPlayerService.MyBinder? = null

    private val playlistViewModel: PlaylistViewModel by viewModels()
    private val songInPlaylistViewModel: SongInPlaylistViewModel by viewModels()

    private val dialogPlaylistAdapter: DialogPlaylistAdapter by lazy {
        DialogPlaylistAdapter(this, this, this, songInPlaylistViewModel) }

    private var doubleBackToExitPressedOnce = false

    var handler: Handler = Handler(Looper.getMainLooper())

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            handler.removeCallbacksAndMessages(null)
            stopService()
            finish()
            return
        }

        this.doubleBackToExitPressedOnce = true
        toast("Please click BACK again to return to Online mode")

        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false }
            , 2000)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //SET ADAPTER
        viewPagerChart = ViewPagerAdapter(supportFragmentManager, lifecycle, fragmentList)
        binding.viewPagerMain.adapter = viewPagerChart

        //SET TAB TITLE AND MAP WITH FRAGMENT
        TabLayoutMediator(binding.tabLayoutMain, binding.viewPagerMain) { tab, position ->
            tab.text = tabLayoutTitles[position]
        }.attach()

        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }

//        window.navigationBarColor = resources.getColor(R.color.main_color, this.theme)
//        window.statusBarColor = resources.getColor(R.color.main_color, this.theme)

        BottomSheetBehavior.from(binding.bottomSheet).apply {

            peekHeight = 200
            this.state = BottomSheetBehavior.STATE_COLLAPSED

            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback(){
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_COLLAPSED){
                        showMiniMenu(true)
                    }
                    if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                        showMiniMenu(false)
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    showMiniMenu(false)
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

        binding.miniPlayerLayout.setOnClickListener {
            BottomSheetBehavior.from(binding.bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }

        binding.addToPlaylistBtn.setOnClickListener {
            val dialog = createDialog(R.layout.playlist_dialog)

            val recyclerView = dialog.findViewById<RecyclerView>(R.id.playlist_recyclerView)
            recyclerView.adapter = dialogPlaylistAdapter
            recyclerView.layoutManager = LinearLayoutManager(dialog.context)

            playlistViewModel.readAllPlaylists().observe(this) {
                dialogPlaylistAdapter.setData(it)
            }
            dialog.show()
        }


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

        binding.playStateBtn.setOnClickListener{
            if (playState == "Loop") {
                playState = "Shuffle"
                binding.playStateBtn.setImageResource(R.drawable.ic_baseline_shuffle_24)
                Toast.makeText(this, "Switched to $playState", Toast.LENGTH_SHORT).show()
            } else {
                if (playState == "Shuffle") {
                    playState = "Go"
                    binding.playStateBtn.setImageResource(R.drawable.ic_baseline_arrow_forward_24)
                    Toast.makeText(this, "Switched to $playState", Toast.LENGTH_SHORT).show()
                } else {
                    if (playState == "Go") {
                        playState = "Loop"
                        binding.playStateBtn.setImageResource(R.drawable.ic_baseline_repeat_24)
                        Toast.makeText(this, "Switched to $playState", Toast.LENGTH_SHORT).show()
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
            audioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2, 0)
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed(object : Runnable{
                override fun run() {
                    binding.volumeSb.progress = audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
                    handler.postDelayed(this, 500)
                }

            }, 500)


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
        binding.playPauseBtn.setImageResource(R.drawable.ic_baseline_pause_24)
        binding.miniPlayPauseBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
        setTime()
        loadUI()
        setCompleteListener()
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
        binding.playPauseBtn.setImageResource(R.drawable.ic_baseline_pause_24)
        binding.miniPlayPauseBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
        setTime()
        loadUI()
        setCompleteListener()
    }

    private fun createRandomTrackPosition() {
        val limit: Int = songList!!.size
        val random = Random()
        val randomNumber = random.nextInt(limit)
        songPosition = randomNumber
    }

    private fun play(){
        musicPlayerService!!.start()
        binding.playPauseBtn.setImageResource(R.drawable.ic_baseline_pause_24)
        binding.miniPlayPauseBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
        setTime()
        updateProgress()
        setCompleteListener()
    }

    private fun pause(){
        musicPlayerService!!.pause()
        binding.playPauseBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        binding.miniPlayPauseBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
        setTime()
        updateProgress()
        setCompleteListener()
    }

    private fun initState() {
        val intent = Intent(this, MusicPlayerService::class.java)
        intent.putExtra("songService", songList!![songPosition])
        startService(intent)
        bindService(intent, this, BIND_AUTO_CREATE)
    }

    @SuppressLint("SimpleDateFormat")
    private fun setTime() {
        val sdf = SimpleDateFormat("mm:ss")
        binding.endTxt.text = sdf.format(musicPlayerService!!.getDuration())
        binding.songSb.max = musicPlayerService!!.getDuration()
        binding.miniPb.max = musicPlayerService!!.getDuration()
    }

    private fun loadUI(){
        binding.titleTxt.text = songList!![songPosition].name
        binding.artistTxt.text = songList!![songPosition].artists
        binding.songSb.max = musicPlayerService!!.getDuration()
        binding.miniPb.max = musicPlayerService!!.getDuration()
        binding.miniSongTitle.text = songList!![songPosition].name
        binding.miniSongArtist.text = songList!![songPosition].artists

        GlobalScope.launch {
            val albumId: String = songList!![songPosition].album_id
            val albumUri: Uri = Uri.parse("content://media/external/audio/albumart")
            val uri: Uri = ContentUris.withAppendedId(albumUri, albumId.toLong())
            val image = withContext(Dispatchers.IO) {
                try {
                    Glide.with(this@MainActivity).asBitmap().load(uri).submit().get()
                }
                catch (e: Exception){
                    BitmapFactory.decodeResource(resources, R.drawable.default_music_neon_icon)
                }
            }
            runOnUiThread {
                binding.songImg.setImageBitmap(image)
            }
        }

        updateProgress()
    }

    private fun setCompleteListener(){
        musicPlayerService!!.mediaPlayer!!.setOnCompletionListener {
            if (playState == PlayState.LOOP){
                preparePlayer()
            }
            else {
                if (playState == PlayState.SHUFFLE) {
                    createRandomTrackPosition()
                    preparePlayer()
                } else {
                    if (playState == PlayState.GO) {
                        next()
                    }
                }
            }
        }
    }

    private fun updateProgress() {
        handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            @SuppressLint("SimpleDateFormat")
            override fun run(){
                try{
                    val currentPosition = musicPlayerService!!.getCurrentDuration()
                    val sdf = SimpleDateFormat("mm:ss")
                    binding.startTxt.text = sdf.format(currentPosition)
                    binding.songSb.progress = currentPosition
                    binding.miniPb.progress = currentPosition
                    handler.postDelayed(this, 1000)
                }
                catch (error: IllegalStateException){
                    handler.removeCallbacksAndMessages(this)
                }
            }
        }, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (musicPlayerService != null){
            musicPlayerService = null
//            musicPlayerService!!.pause()
//            musicPlayerService!!.stop()
        }
        stopService(Intent(this, OnlineMusicPlayerService::class.java))
        if (isServiceConnected){
            unbindService(this)
            isServiceConnected = false
        }
        Log.i("TAG502", "onDestroy: Player")
    }

    private fun stopService(){
        if (musicPlayerService != null){
            musicPlayerService = null
//            musicPlayerService!!.stop()
//            musicPlayerService!!.pause()
        }
        stopService(Intent(this, MusicPlayerService::class.java))
        if (isServiceConnected){
            unbindService(this)
            isServiceConnected = false
        }
        binding.miniPlayerLayout.visibility = View.GONE
    }

    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        GlobalScope.launch {
            val myBinder = p1 as MusicPlayerService.MyBinder
            iBinder = myBinder
            musicPlayerService = myBinder.getService()
            isServiceConnected = true
            musicPlayerService!!.start()
            registerReceiver(broadcastReceiver, IntentFilter("TRACKS_TRACKS"))
            runOnUiThread {
                setTime()
                loadUI()
                listener()
                setCompleteListener()
            }
        }
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        musicPlayerService = null
        isServiceConnected = false
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.extras!!.getInt("action_music")) {
                MusicPlayerService.ACTION_PREVIOUS -> previous()
                MusicPlayerService.ACTION_PAUSE -> {
                    if (musicPlayerService!!.isPlaying()) {
                        pause()
                    } else {
                        play()
                    }
                }
                MusicPlayerService.ACTION_NEXT -> next()
            }
        }
    }

    private fun preparePlayer(){
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
        }
        registerReceiver(broadcastReceiver, IntentFilter("TRACKS_TRACKS"))
    }

    override fun callBackFromSongFragment(songs: List<Song>, position: Int) {
        this.songList = songs
        this.songPosition = position
        preparePlayer()
    }

    override fun callBackFromSongInPlaylist(songList: List<Song>, position: Int) {
        this.songList = songList
        this.songPosition = position
        preparePlayer()
    }

    override fun callBackFromMenuSongInPlaylist(action: String, songList: List<Song>, position: Int, playlist: Playlist) {

    }

    override fun onMenuClick(action: String, playlist: Playlist) {
        if (action == "Rename"){
            createDialogForRenamePlaylist(playlist)
        }
        if (action == "Delete"){
            createDialogForDeletePlaylist(playlist)
        }
    }

    private fun createDialogForRenamePlaylist(playlist: Playlist){

        val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
        val inflater = this.layoutInflater
        val view = inflater.inflate(R.layout.menu_playlist_dialog, null)

        view.findViewById<EditText>(R.id.title_et_menu_playlist_dialog).setText(playlist.name)

        builder.setMessage("Rename")
            .setTitle("")
            .setView(view)
            .setPositiveButton("Rename") { _, _ ->

                val title =
                    view.findViewById<EditText>(R.id.title_et_menu_playlist_dialog).text.toString()

                if (title.isEmpty()) {
                    Toast.makeText(this, "Name can not be empty", Toast.LENGTH_SHORT).show()
                } else {
                    val updatedPlaylist = Playlist(playlist.playlist_id, title)
                    playlistViewModel.updatePlaylist(updatedPlaylist)
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                // User cancelled the dialog
            }
        // Create the AlertDialog object and return it
        builder.create().show()
    }

    private fun createDialogForDeletePlaylist(playlist: Playlist){

        val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
        builder.setMessage("Delete ${playlist.name} playlist?")
            .setTitle("")
            .setPositiveButton("Delete") { _, _ ->
                playlistViewModel.deletePlaylist(playlist)
            }
            .setNegativeButton("Cancel") { _, _ ->
                // User cancelled the dialog
            }
        // Create the AlertDialog object and return it

        builder.create().show()
    }

    override fun onItemPlaylistClick(playlist: Playlist) {
        val currentSong = songList!![songPosition]
        val songPlaylistCrossRef = SongPlaylistCrossRef(currentSong.song_id, playlist.playlist_id)
        songInPlaylistViewModel.addSongPlaylistCrossRef(songPlaylistCrossRef)
        toast("Song ${currentSong.name} added to ${playlist.name} playlist")
    }

}