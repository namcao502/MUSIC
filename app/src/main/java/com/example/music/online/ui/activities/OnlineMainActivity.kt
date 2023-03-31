package com.example.music.online.ui.activities

import android.annotation.SuppressLint
import android.content.*
import android.graphics.Color
import android.os.*
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.music.R
import com.example.music.databinding.ActivityOnlineMainBinding
import com.example.music.offline.ui.activities.MainActivity
import com.example.music.online.data.dao.ConnectivityObserver
import com.example.music.online.data.models.OnlineComment
import com.example.music.online.data.models.OnlinePlaylist
import com.example.music.online.data.models.OnlineSong
import com.example.music.online.services.OnlineMusicPlayerService
import com.example.music.online.ui.adapters.CommentDialogAdapter
import com.example.music.online.ui.adapters.OnlineDialogPlaylistAdapter
import com.example.music.online.ui.fragments.*
import com.example.music.online.viewModels.*
import com.example.music.utils.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.music.utils.NetworkConnectivityObserver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.io.IOException
import java.lang.Runnable
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class OnlineMainActivity: AppCompatActivity(),
    ServiceConnection,
    OnlineSongFragment.SongFromAdapterClick,
    OnlineDialogPlaylistAdapter.ItemClickListener,
    HomeFragment.ClickSongFromDetail,
    SearchFragment.ClickSongFromDetail,
    OnlinePlaylistFragment.ClickSongFromDetail,
    CommentDialogAdapter.ClickAComment{

    private lateinit var binding: ActivityOnlineMainBinding

    private var songFragment = OnlineSongFragment(this)
    private var playlistFragment = OnlinePlaylistFragment(this)
    private var homeFragment = HomeFragment(this)
    private var searchFragment = SearchFragment(this)
    private var userFragment = UserFragment()
    private var activeFragment: Fragment = homeFragment

    var songList: List<OnlineSong>? = null
    var comments: List<OnlineComment> = emptyList()
    private var songPosition = -1
    private var playState = PlayState.GO
    private var currentArtists: String = ""

    var musicPlayerService: OnlineMusicPlayerService? = null
    private var isServiceConnected = false
    private var iBinder: OnlineMusicPlayerService.MyBinder? = null

    private val onlineSongViewModel: OnlineSongViewModel by viewModels()
    private val onlinePlaylistViewModel: OnlinePlaylistViewModel by viewModels()
    private val onlineArtistViewModel: OnlineArtistViewModel by viewModels()
    private val onlineCommentViewModel: OnlineCommentViewModel by viewModels()
    private val firebaseViewModel: FirebaseViewModel by viewModels()
    private val onlineViewViewModel: OnlineViewViewModel by viewModels()

    private val onlineDialogPlaylistAdapter: OnlineDialogPlaylistAdapter by lazy {
        OnlineDialogPlaylistAdapter(this, this) }

    private var doubleBackToExitPressedOnce = false

    private lateinit var connectivityObserver: ConnectivityObserver

    override fun onBackPressed() {

        if (PlayerState.isOn){
            PlayerState.isOn = false
            binding.playerSheet.playerLayout.fadeVisibility(View.GONE)
            binding.bottomCard.fadeVisibility(View.VISIBLE)
            return
        }

        if (DetailFragmentState.isOn){
            DetailFragmentState.isOn = false
            supportFragmentManager.beginTransaction().remove(DetailFragmentState.instance!!).commit()
            super.onBackPressed()
            return
        }

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        toast("Please click BACK again to exit")

        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false }
            , 2000)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnlineMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.online_home_menu -> {
                    if (DetailFragmentState.isOn){
                        supportFragmentManager.popBackStack()
                        DetailFragmentState.isOn = false
                    }
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.fade, 0, 0, 0)
                        .hide(activeFragment)
                        .show(homeFragment).commit()
                    activeFragment = homeFragment
                    return@setOnItemSelectedListener true
                }
                R.id.online_search_menu -> {
                    if (DetailFragmentState.isOn){
                        supportFragmentManager.popBackStack()
                        DetailFragmentState.isOn = false
                    }
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.fade, 0, 0, 0)
                        .hide(activeFragment)
                        .show(searchFragment).commit()
                    activeFragment = searchFragment
                    return@setOnItemSelectedListener true
                }
                R.id.online_song_menu -> {
                    if (DetailFragmentState.isOn){
                        supportFragmentManager.popBackStack()
                        DetailFragmentState.isOn = false
                    }
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.fade, 0, 0, 0)
                        .hide(activeFragment)
                        .show(songFragment).commit()
                    activeFragment = songFragment
                    return@setOnItemSelectedListener true
                }
                R.id.online_collection_menu -> {
                    if (DetailFragmentState.isOn){
                        supportFragmentManager.popBackStack()
                        DetailFragmentState.isOn = false
                    }
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.fade, 0, 0, 0)
                        .hide(activeFragment)
                        .show(playlistFragment).commit()
                    activeFragment = playlistFragment
                    return@setOnItemSelectedListener true
                }
                R.id.online_user_menu -> {
                    if (DetailFragmentState.isOn){
                        supportFragmentManager.popBackStack()
                        DetailFragmentState.isOn = false
                    }
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.fade, 0, 0, 0)
                        .hide(activeFragment)
                        .show(userFragment).commit()
                    activeFragment = userFragment
                    return@setOnItemSelectedListener true
                }
            }
            return@setOnItemSelectedListener false
        }

        with(supportFragmentManager.beginTransaction()){
            add(R.id.fragment_container, userFragment).hide(userFragment)
            add(R.id.fragment_container, playlistFragment).hide(playlistFragment)
            add(R.id.fragment_container, songFragment).hide(songFragment)
            add(R.id.fragment_container, searchFragment).hide(searchFragment)
            add(R.id.fragment_container, homeFragment)
            commit()
        }

        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }

        window.navigationBarColor = resources.getColor(R.color.main_color, this.theme)
        window.statusBarColor = resources.getColor(R.color.main_color, this.theme)

        GlobalScope.launch {
            connectivityObserver = NetworkConnectivityObserver(this@OnlineMainActivity)
            connectivityObserver.observe().collect { value ->
                when (value){
                    ConnectivityObserver.Status.Available -> {
                        runOnUiThread {
                            with(binding.internetTxt){
//                                setBackgroundColor(Color.GREEN)
                                setTextColor(Color.GREEN)
                                text = ConnectionType.BACK_ONLINE
                                visibility = View.VISIBLE
                                Handler(Looper.getMainLooper()).postDelayed({
                                    this@with.visibility = View.GONE
                                }, 2000)
                            }
                        }
                    }
                    else -> {
                        runOnUiThread {
                            with(binding.internetTxt){
//                                setBackgroundColor(Color.BLACK)
                                setTextColor(Color.RED)
                                text = ConnectionType.NO_INTERNET
                                visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }
        }

//        checkNetwork(binding.internetTxt)

    }

    private fun listener() {

        binding.playerSheet.optionBtn.setOnClickListener {
            val bottomDialog = createBottomSheetDialog(R.layout.option_player_layout)

            val img = bottomDialog.findViewById<ImageView>(R.id.imageView)
            val title = bottomDialog.findViewById<TextView>(R.id.title_txt)
            val artists = bottomDialog.findViewById<TextView>(R.id.author_txt)

            Glide.with(this).load(songList!![songPosition].imgFilePath).into(img!!)
            title!!.text = songList!![songPosition].name
            artists!!.text = currentArtists

            val addToPlaylistBtn = bottomDialog.findViewById<LinearLayout>(R.id.add_to_playlist_layout)
            val commentsBtn = bottomDialog.findViewById<LinearLayout>(R.id.comment_layout)
            val downloadBtn = bottomDialog.findViewById<LinearLayout>(R.id.download_layout)
            val playStateBtn = bottomDialog.findViewById<LinearLayout>(R.id.play_state_layout)
            val playStateTxt = bottomDialog.findViewById<TextView>(R.id.play_state_txt)

            commentsBtn!!.setOnClickListener {
                //create bottom sheet dialog
                val bottomSheetDialog = createBottomSheetDialog(R.layout.comment_dialog)

                val commentRv = bottomSheetDialog.findViewById<RecyclerView>(R.id.comment_rv)
                val postBtn = bottomSheetDialog.findViewById<Button>(R.id.post_cmt_btn)
                val message = bottomSheetDialog.findViewById<EditText>(R.id.message_et_cmt_dialog)

                val commentDialogAdapter: CommentDialogAdapter by lazy {
                    CommentDialogAdapter(this, this)
                }

                with(commentRv!!){
                    adapter = commentDialogAdapter
                    layoutManager = LinearLayoutManager(this@OnlineMainActivity)
                }

                //prepare data for listView
                onlineCommentViewModel.getAllCommentForSong(songList!![songPosition])
                onlineCommentViewModel.comment.observe(this){ comment ->
                    when (comment) {
                        is UiState.Loading -> {

                        }
                        is UiState.Failure -> {

                        }
                        is UiState.Success -> {
                            comments = comment.data
                            commentDialogAdapter.setData(comments)
                        }
                    }
                }

                postBtn!!.setOnClickListener {

                    val text = message!!.text.toString()
                    if (text.isEmpty()){
                        toast("Please type something to post...")
                        return@setOnClickListener
                    }

                    val comment = OnlineComment("", text, songList!![songPosition].id, Firebase.auth.currentUser!!.uid)
                    onlineCommentViewModel.addComment(comment)
                    onlineCommentViewModel.addComment.observe(this){
                        when (it) {
                            is UiState.Loading -> {

                            }
                            is UiState.Failure -> {

                            }
                            is UiState.Success -> {
                                toast(it.data)
                                message.clearFocus()
                            }
                        }
                    }
                }
                bottomSheetDialog.show()
            }

            addToPlaylistBtn!!.setOnClickListener {
                val addDialog = createBottomSheetDialog(R.layout.fragment_online_playlist)

                val recyclerView = addDialog.findViewById<RecyclerView>(R.id.playlist_recyclerView)
                recyclerView!!.adapter = onlineDialogPlaylistAdapter
                recyclerView.layoutManager = LinearLayoutManager(addDialog.context)

                FirebaseAuth.getInstance().currentUser?.let { user ->
                    onlinePlaylistViewModel.getAllPlaylistOfUser(user)
                }

                onlinePlaylistViewModel.playlist.observe(this) {
                    when (it) {
                        is UiState.Loading -> {

                        }
                        is UiState.Failure -> {

                        }
                        is UiState.Success -> {
                            onlineDialogPlaylistAdapter.setData(it.data)
                        }
                    }
                }

                addDialog.findViewById<FloatingActionButton>(R.id.add_btn)!!.setOnClickListener {
                    createDialogForAddPlaylist(onlinePlaylistViewModel)
                }

                addDialog.show()
            }

            downloadBtn!!.setOnClickListener {
                firebaseViewModel.downloadSingleSongFile(this,
                    songList!![songPosition].name!!.plus(" - $currentArtists"),
                    songList!![songPosition].filePath!!){
                    when(it){
                        is UiState.Loading -> {

                        }
                        is UiState.Failure -> {

                        }
                        is UiState.Success -> {
                            toast(it.data)
                        }
                    }
                }
            }

            playStateBtn!!.setOnClickListener {
                if (playState == PlayState.LOOP) {
                    playState = PlayState.SHUFFLE
                } else {
                    if (playState == PlayState.SHUFFLE) {
                        playState = PlayState.GO
                    } else {
                        if (playState == PlayState.GO) {
                            playState = PlayState.LOOP
                        }
                    }
                }
                playStateTxt!!.text = "Mode: $playState"
                toast("Switched to $playState")
            }

            bottomDialog.show()
        }

        binding.miniPlayerLayout.setOnClickListener {
            PlayerState.isOn = true
            binding.playerSheet.playerLayout.fadeVisibility(View.VISIBLE)
            binding.bottomCard.fadeVisibility(View.GONE)
        }

        binding.playerSheet.backBtn.setOnClickListener {
            PlayerState.isOn = false
            binding.playerSheet.playerLayout.fadeVisibility(View.GONE)
            binding.bottomCard.fadeVisibility(View.VISIBLE)
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

        binding.playerSheet.nextBtn.setOnClickListener{
            next()
        }
        binding.playerSheet.previousBtn.setOnClickListener{
            previous()
        }
        binding.playerSheet.playPauseBtn.setOnClickListener{
            if (musicPlayerService!!.isPlaying()) {
                pause()
            } else {
                play()
            }
        }
        binding.playerSheet.songSb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                musicPlayerService!!.seekTo(seekBar.progress)
                updateProgress()
            }
        })
    }

    private fun createDialogForAddPlaylist(onlinePlaylistViewModel: OnlinePlaylistViewModel) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.menu_playlist_dialog, null)

        builder.setMessage("Create")
            .setTitle("")
            .setView(view)
            .setPositiveButton("Create") { _, _ ->

                val title =
                    view.findViewById<EditText>(R.id.title_et_menu_playlist_dialog).text.toString()

                if (title.isEmpty()) {
                    Toast.makeText(this, "Name can not be empty", Toast.LENGTH_SHORT).show()
                } else {
                    val playlist = OnlinePlaylist("", title, emptyList())
                    FirebaseAuth.getInstance().currentUser?.let {
                        onlinePlaylistViewModel.addPlaylistForUser(playlist, it)
                    }
                    onlinePlaylistViewModel.addPlaylist.observe(this) {
                        when (it) {
                            is UiState.Loading -> {

                            }
                            is UiState.Failure -> {

                            }
                            is UiState.Success -> {
                                Toast.makeText(this, it.data, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                // User cancelled the dialog
            }
        // Create the AlertDialog object and return it
        builder.create().show()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun previous() {
        GlobalScope.launch {
            songPosition -= 1
            val maxLength: Int = songList!!.size
            if (songPosition < 0) {
                songPosition = maxLength - 1
            }
            if (playState == PlayState.SHUFFLE) {
                createRandomTrackPosition()
            } else {
                if (playState == PlayState.LOOP) {
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
            runOnUiThread {
                binding.playerSheet.playPauseBtn.setImageResource(R.drawable.ic_baseline_pause_24)
                binding.miniPlayPauseBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
                setTime()
                loadUI()
                setCompleteListener()
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun next() {
        GlobalScope.launch {
            songPosition += 1
            val maxLength: Int = songList!!.size
            if (songPosition > maxLength - 1) {
                songPosition = 0
            }
            if (playState == PlayState.SHUFFLE) {
                createRandomTrackPosition()
            } else {
                if (playState == PlayState.LOOP) {
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
            runOnUiThread {
                binding.playerSheet.playPauseBtn.setImageResource(R.drawable.ic_baseline_pause_24)
                binding.miniPlayPauseBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
                setTime()
                loadUI()
                setCompleteListener()
            }
        }
    }

    private fun createRandomTrackPosition() {
        val limit: Int = songList!!.size
        val random = Random()
        val randomNumber = random.nextInt(limit)
        songPosition = randomNumber
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun play(){
        GlobalScope.launch {
            musicPlayerService!!.start()
            runOnUiThread {
                binding.playerSheet.playPauseBtn.setImageResource(R.drawable.ic_baseline_pause_24)
                binding.miniPlayPauseBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
                setTime()
                updateProgress()
                setCompleteListener()
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun pause(){
        GlobalScope.launch {
            musicPlayerService!!.pause()
            runOnUiThread {
                binding.playerSheet.playPauseBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                binding.miniPlayPauseBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
                setTime()
                updateProgress()
                setCompleteListener()
            }
        }
    }

    private fun initState() {
        val intent = Intent(this, OnlineMusicPlayerService::class.java)
        intent.putExtra("songService", songList!![songPosition])
        startService(intent)
        bindService(intent, this, BIND_AUTO_CREATE)
    }

    @SuppressLint("SimpleDateFormat")
    private fun setTime() {
        val sdf = SimpleDateFormat("mm:ss")
        binding.playerSheet.endTxt.text = sdf.format(musicPlayerService!!.getDuration())
        binding.playerSheet.songSb.max = musicPlayerService!!.getDuration()
        binding.miniPb.max = musicPlayerService!!.getDuration()
    }

    private fun loadUI(){

        binding.playerSheet.titleTxt.text = songList!![songPosition].name
        binding.playerSheet.songSb.max = musicPlayerService!!.getDuration()
        binding.miniSongTitle.text = songList!![songPosition].name
        binding.miniPb.max = musicPlayerService!!.getDuration()

        if (songList!![songPosition].imgFilePath!!.isNotEmpty()){
            Glide.with(this@OnlineMainActivity)
                .load(songList!![songPosition].imgFilePath!!)
                .into(binding.playerSheet.songImg)
        }

        onlineArtistViewModel.getAllArtistFromSongID(songList!![songPosition].id!!)
        onlineArtistViewModel.artistFromSongID.observe(this){
            when(it){
                is UiState.Loading -> {

                }
                is UiState.Failure -> {

                }
                is UiState.Success -> {
                    var text = ""
                    for (x in it.data){
                        text += x.name.plus(", ")
                    }
                    text = text.dropLast(2)
                    currentArtists = text
                    PlayerState.artistText = text
                    musicPlayerService!!.recreateNotification(text)
                    binding.miniSongArtist.text = text
                    binding.playerSheet.artistTxt.text = text
                }
            }
        }

        onlineViewViewModel.updateView(songList!![songPosition].id!!)
        onlineViewViewModel.updateView.observe(this){
            when(it) {
                is UiState.Loading -> {

                }
                is UiState.Failure -> {

                }
                is UiState.Success -> {
                    Log.i("TAG502", "view updated for ${songList!![songPosition].name}")
                }
            }
        }

        updateProgress()
    }

    private fun updateProgress() {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            @SuppressLint("SimpleDateFormat")
            override fun run(){
                try{
                    val currentPosition = musicPlayerService!!.getCurrentDuration()
                    val sdf = SimpleDateFormat("mm:ss")
                    binding.playerSheet.startTxt.text = sdf.format(currentPosition)
                    binding.playerSheet.songSb.progress = currentPosition
                    binding.miniPb.progress = currentPosition
                    handler.postDelayed(this, 500)
                }
                catch (error: IllegalStateException){
                    handler.removeCallbacksAndMessages(null)
                }
            }
        }, 500)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (musicPlayerService != null){
            musicPlayerService == null
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

    fun stopService(){
        if (musicPlayerService != null){
            musicPlayerService == null
//            musicPlayerService!!.stop()
//            musicPlayerService!!.pause()
        }
        stopService(Intent(this, OnlineMusicPlayerService::class.java))
        if (isServiceConnected){
            unbindService(this)
            isServiceConnected = false
        }
        binding.miniPlayerLayout.visibility = View.GONE
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

    @OptIn(DelicateCoroutinesApi::class)
    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        GlobalScope.launch {
            val myBinder = p1 as OnlineMusicPlayerService.MyBinder
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
                OnlineMusicPlayerService.ACTION_PREVIOUS ->
                    GlobalScope.launch {
                        previous()
                    }
                OnlineMusicPlayerService.ACTION_PAUSE -> {
                    if (musicPlayerService!!.isPlaying()) {
                        GlobalScope.launch {
                            pause()
                        }
                    } else {
                        GlobalScope.launch {
                            play()
                        }
                    }
                }
                OnlineMusicPlayerService.ACTION_NEXT ->
                    GlobalScope.launch {
                        next()
                    }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun preparePlayer(){
        GlobalScope.launch {
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed(object : Runnable {
                override fun run() {
                    if (getConnectionType(this@OnlineMainActivity) == ConnectionType.NOT_CONNECT){
                        AlertDialog
                            .Builder(this@OnlineMainActivity)
                            .setMessage("Switch to offline mode?")
                            .setTitle("No internet connection")
                            .setPositiveButton("Yes") { _, _ ->
                                startActivity(Intent(this@OnlineMainActivity, MainActivity::class.java))
                            }
                            .setNegativeButton("Retry") { _, _ ->
                                handler.postDelayed(this, 100)
                            }
                            .create()
                            .show()
                    }
                    else {
                        runOnUiThread {
                            binding.miniPlayPauseBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
                            binding.playerSheet.playPauseBtn.setImageResource(R.drawable.ic_baseline_pause_24)
                        }
                        if (!isServiceConnected){
                            initState()
                            runOnUiThread{
                                binding.miniPlayerLayout.visibility = View.VISIBLE
                            }
                        }
                        else{
                            musicPlayerService!!.stop()
                            musicPlayerService!!.release()
                            musicPlayerService!!.createMediaPlayer(songList!![songPosition])
                            musicPlayerService!!.start()
                            runOnUiThread {
                                setTime()
                                loadUI()
                                setCompleteListener()
                            }
                        }
                        registerReceiver(broadcastReceiver, IntentFilter("TRACKS_TRACKS"))
                    }
                }
            }, 500)
        }

    }

    override fun onMenuClick(action: String, playlist: OnlinePlaylist) {
        if (action == "Rename"){
            createDialogForRenamePlaylist(playlist, onlinePlaylistViewModel)
        }
        if (action == "Delete"){
            createDialogForDeletePlaylist(playlist, onlinePlaylistViewModel)
        }
    }

    private fun createDialogForDeletePlaylist(playlist: OnlinePlaylist, onlinePlaylistViewModel: OnlinePlaylistViewModel) {
        val builder = AlertDialog.Builder(this)

        builder.setMessage("Delete ${playlist.name} playlist?")
            .setTitle("")
            .setPositiveButton("Delete") { _, _ ->

                FirebaseAuth.getInstance().currentUser?.let {
                    onlinePlaylistViewModel.deletePlaylistForUser(playlist, it)
                }
                onlinePlaylistViewModel.deletePlaylist.observe(this) {
                    when (it) {
                        is UiState.Loading -> {

                        }
                        is UiState.Failure -> {

                        }
                        is UiState.Success -> {
                            Toast.makeText(this, it.data, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                // User cancelled the dialog
            }
        // Create the AlertDialog object and return it
        builder.create().show()
    }

    private fun createDialogForRenamePlaylist(playlist: OnlinePlaylist, onlinePlaylistViewModel: OnlinePlaylistViewModel) {
        val builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val view = inflater.inflate(R.layout.menu_playlist_dialog, null)

        view.findViewById<EditText>(R.id.title_et_menu_playlist_dialog).setText(playlist.name)

        builder.setMessage("Rename")
            .setTitle("")
            .setView(view)
            .setPositiveButton("Rename") { _, _ ->

                val title = view.findViewById<EditText>(R.id.title_et_menu_playlist_dialog).text.toString()

                if (title.isEmpty()) {
                    Toast.makeText(this, "Name can not be empty", Toast.LENGTH_SHORT).show()
                } else {
                    playlist.name = title
                    FirebaseAuth.getInstance().currentUser?.let {
                        onlinePlaylistViewModel.updatePlaylistForUser(playlist, it)
                    }
                    onlinePlaylistViewModel.updatePlaylist.observe(this) {
                        when (it) {
                            is UiState.Loading -> {

                            }
                            is UiState.Failure -> {

                            }
                            is UiState.Success -> {
                                Toast.makeText(this, it.data, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                // User cancelled the dialog
            }
        // Create the AlertDialog object and return it
        builder.create().show()
    }

    override fun onItemPlaylistClick(playlist: OnlinePlaylist) {
        val currentSong = songList!![songPosition]
        FirebaseAuth.getInstance().currentUser?.let {
            onlineSongViewModel.addSongToPlaylist(currentSong, playlist, it)
            toast("Song ${currentSong.name} added to ${playlist.name} playlist")
        }
    }

    override fun callBackFromClickSongInDetail(songList: List<OnlineSong>, position: Int) {
        this.songList = songList
        this.songPosition = position
        preparePlayer()
    }

    override fun callBackFromSongFragment(songs: List<OnlineSong>, position: Int) {
        this.songList = songs
        this.songPosition = position
        preparePlayer()
    }

    override fun callBackFromMenuClickComment(action: String, comment: OnlineComment) {
        if (action == "Edit"){
            if (comment.userId!! == Firebase.auth.currentUser!!.uid){

                val builder = AlertDialog.Builder(this)
                val inflater = layoutInflater
                val view = inflater.inflate(R.layout.menu_playlist_dialog, null)

                view.findViewById<EditText>(R.id.title_et_menu_playlist_dialog).setText(comment.message)

                builder.setMessage("Edit this comment...")
                    .setTitle("")
                    .setView(view)
                    .setPositiveButton("Save") { _, _ ->

                        val titleTemp = view.findViewById<EditText>(R.id.title_et_menu_playlist_dialog).text.toString()

                        if (titleTemp.isEmpty()){
                            toast("Please type something...")
                        }
                        else {
                            comment.message = titleTemp
                            onlineCommentViewModel.updateComment(comment)
                            onlineCommentViewModel.updateComment.observe(this){
                                when(it){
                                    is UiState.Loading -> {

                                    }
                                    is UiState.Failure -> {

                                    }
                                    is UiState.Success -> {
                                        toast("Comment updated!!!")
                                    }
                                }
                            }
                        }
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                        // User cancelled the dialog
                    }
                // Create the AlertDialog object and return it
                builder.create().show()
            }
            else {
                toast("You can't edit this comment!")
            }
        }
        if (action == "Delete"){
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Delete this comment?")
                .setTitle("")
                .setPositiveButton("Delete") { _, _ ->
                    if (comment.userId!! == Firebase.auth.currentUser!!.uid) {
                        onlineCommentViewModel.deleteComment(comment)
                        onlineCommentViewModel.deleteComment.observe(this) {
                            when (it) {
                                is UiState.Loading -> {

                                }
                                is UiState.Failure -> {

                                }
                                is UiState.Success -> {
                                    toast(it.data)
                                }
                            }
                        }
                    } else {
                        toast("You can't delete this comment")
                    }

                }
                .setNegativeButton("Cancel") { _, _ ->
                    // User cancelled the dialog
                }
            // Create the AlertDialog object and return it
            builder.create().show()
        }
    }

}