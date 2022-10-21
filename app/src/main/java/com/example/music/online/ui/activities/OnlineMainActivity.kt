package com.example.music.online.ui.activities

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.*
import android.media.AudioManager
import android.net.Uri
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
import com.example.music.online.data.models.OnlineComment
import com.example.music.online.data.models.OnlinePlaylist
import com.example.music.online.data.models.OnlineSong
import com.example.music.online.services.OnlineMusicPlayerService
import com.example.music.online.ui.adapters.OnlineDialogPlaylistAdapter
import com.example.music.online.ui.fragments.*
import com.example.music.online.viewModels.*
import com.example.music.utils.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class OnlineMainActivity: AppCompatActivity(),
    ServiceConnection,
    OnlineSongFragment.SongFromAdapterClick,
    OnlineDialogPlaylistAdapter.ItemClickListener,
    HomeFragment.ClickSongFromDetail,
    SearchFragment.ClickSongFromDetail,
    OnlinePlaylistFragment.ClickSongFromDetail{

    private lateinit var binding: ActivityOnlineMainBinding

    private var songFragment = OnlineSongFragment(this)
    private var playlistFragment = OnlinePlaylistFragment(this)
    private var homeFragment = HomeFragment(this)
    private var searchFragment = SearchFragment(this)
    private var userFragment = UserFragment()
    private var activeFragment: Fragment = homeFragment

    var songList: List<OnlineSong>? = null
    private var songPosition = -1

    var audioManager: AudioManager? = null

    private var playState = "Go"

    var musicPlayerService: OnlineMusicPlayerService? = null

    private var isServiceConnected = false

    private var iBinder: OnlineMusicPlayerService.MyBinder? = null

    private val onlineSongViewModel: OnlineSongViewModel by viewModels()
    private val onlinePlaylistViewModel: OnlinePlaylistViewModel by viewModels()
    private val onlineArtistViewModel: OnlineArtistViewModel by viewModels()
    private val onlineCommentViewModel: OnlineCommentViewModel by viewModels()
    private val firebaseViewModel: FirebaseViewModel by viewModels()

    private val onlineDialogPlaylistAdapter: OnlineDialogPlaylistAdapter by lazy {
        OnlineDialogPlaylistAdapter(this, this) }

    private var currentArtists: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnlineMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.online_home_menu -> {
                    if (DetailFragmentState.isOn){
                        onBackPressed()
                        DetailFragmentState.isOn = false
                    }
                    supportFragmentManager.beginTransaction()
                        .hide(activeFragment)
                        .show(homeFragment).commit()
                    activeFragment = homeFragment
                    return@setOnItemSelectedListener true
                }
                R.id.online_search_menu -> {
                    if (DetailFragmentState.isOn){
                        onBackPressed()
                        DetailFragmentState.isOn = false
                    }
                    supportFragmentManager.beginTransaction()
                        .hide(activeFragment)
                        .show(searchFragment).commit()
                    activeFragment = searchFragment
                    return@setOnItemSelectedListener true
                }
                R.id.online_song_menu -> {
                    if (DetailFragmentState.isOn){
                        onBackPressed()
                        DetailFragmentState.isOn = false
                    }
                    supportFragmentManager.beginTransaction()
                        .hide(activeFragment)
                        .show(songFragment).commit()
                    activeFragment = songFragment
                    return@setOnItemSelectedListener true
                }
                R.id.online_collection_menu -> {
                    if (DetailFragmentState.isOn){
                        onBackPressed()
                        DetailFragmentState.isOn = false
                    }
                    supportFragmentManager.beginTransaction()
                        .hide(activeFragment)
                        .show(playlistFragment).commit()
                    activeFragment = playlistFragment
                    return@setOnItemSelectedListener true
                }
                R.id.online_user_menu -> {
                    if (DetailFragmentState.isOn){
                        onBackPressed()
                        DetailFragmentState.isOn = false
                    }
                    supportFragmentManager.beginTransaction()
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

//        BottomSheetBehavior.from(binding.bottomSheet.playerLayout).apply {
//
//            this.state = BottomSheetBehavior.STATE_COLLAPSED
//
//            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback(){
//                override fun onStateChanged(bottomSheet: View, newState: Int) {
//
//                }
//
//                override fun onSlide(bottomSheet: View, slideOffset: Float) {
//                    //begin animation
//                    binding.bottomSheet.playerLayout.visibility = View.GONE
//                    //end animation
//                }
//
//            })
//        }

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

            commentsBtn!!.setOnClickListener {
                //create bottom sheet dialog
                val bottomSheetDialog = createBottomSheetDialog(R.layout.comment_dialog)

                val commentLv = bottomSheetDialog.findViewById<ListView>(R.id.comment_lv)
                val postBtn = bottomSheetDialog.findViewById<Button>(R.id.post_cmt_btn)
                val message = bottomSheetDialog.findViewById<EditText>(R.id.message_et_cmt_dialog)

                var comments: List<OnlineComment> = emptyList()

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
//                        for (x in comments){
//                            x.message = Firebase.auth.currentUser!!.email.toString() + ": " + x.message
//                        }
                            commentLv!!.adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, comments)
                        }
                    }
                }

                commentLv!!.setOnItemClickListener { _, _, i, _ ->
                    //update
                    if (comments[i].userId!! == Firebase.auth.currentUser!!.uid){
                        val builder = AlertDialog.Builder(this)
                        val inflater = layoutInflater
                        val view = inflater.inflate(R.layout.menu_playlist_dialog, null)

                        view.findViewById<EditText>(R.id.title_et_menu_playlist_dialog).setText(comments[i].message)

                        builder.setMessage("Edit this comment...")
                            .setTitle("")
                            .setView(view)
                            .setPositiveButton("Save") { _, _ ->

                                val title = view.findViewById<EditText>(R.id.title_et_menu_playlist_dialog).text.toString()

                                if (title.isEmpty()){
                                    toast("Please type something...")
                                }
                                else {
                                    comments[i].message = title
                                    onlineCommentViewModel.updateComment(comments[i])
                                    onlineCommentViewModel.updateComment.observe(this){
                                        when(it){
                                            is UiState.Loading -> {

                                            }
                                            is UiState.Failure -> {

                                            }
                                            is UiState.Success -> {
                                                toast("Updated!!!")
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

                commentLv.setOnItemLongClickListener { _, _, i, l ->
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage("Delete this comment?")
                        .setTitle("")
                        .setPositiveButton("Delete") { _, _ ->
                            if (comments[i].userId!! == Firebase.auth.currentUser!!.uid) {
                                onlineCommentViewModel.deleteComment(comments[i])
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

                    return@setOnItemLongClickListener false
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
                                postBtn.requestFocus()
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
                Firebase.storage
                    .getReferenceFromUrl(songList!![songPosition].filePath!!)
                    .downloadUrl
                    .addOnSuccessListener { uri: Uri ->
                        val url = uri.toString()
                        downloadFile(this, songList!![songPosition].name!!.plus(" - $currentArtists"), "mp3", Environment.DIRECTORY_DOWNLOADS, url)
                        toast("Downloading...")
                    }.addOnFailureListener { e: Exception ->
                        toast(e.toString())
                    }
            }

            bottomDialog.show()
        }

        binding.miniPlayerLayout.setOnClickListener {
            binding.miniPlayerLayout.visibility = View.VISIBLE
            binding.playerSheet.playerLayout.visibility = View.VISIBLE
            binding.bottomCard.visibility = View.GONE
        }

        binding.playerSheet.backBtn.setOnClickListener {
            binding.playerSheet.playerLayout.visibility = View.GONE
            binding.bottomCard.visibility = View.VISIBLE
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

        binding.playerSheet.playStateBtn.setOnClickListener{
            if (playState == "Loop") {
                playState = "Shuffle"
                binding.playerSheet.playStateBtn.setImageResource(R.drawable.ic_baseline_shuffle_24)
            } else {
                if (playState == "Shuffle") {
                    playState = "Go"
                    binding.playerSheet.playStateBtn.setImageResource(R.drawable.ic_baseline_arrow_forward_24)
                } else {
                    if (playState == "Go") {
                        playState = "Loop"
                        binding.playerSheet.playStateBtn.setImageResource(R.drawable.ic_baseline_repeat_24)
                    }
                }
            }
            toast("Switched to $playState")
        }

        binding.playerSheet.nextBtn.setOnClickListener{ next() }
        binding.playerSheet.previousBtn.setOnClickListener{ previous() }
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
//        try {
//            audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
//            binding.bottomSheet.volumeSb.max = audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
////            audioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2, 0)
//            val handler = Handler(Looper.getMainLooper())
//            handler.postDelayed(object : Runnable{
//                override fun run() {
//                    binding.bottomSheet.volumeSb.progress = audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
//                    handler.postDelayed(this, 500)
//                }
//
//            }, 500)
//
//
//            binding.bottomSheet.volumeSb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//                override fun onStopTrackingTouch(arg0: SeekBar) {}
//                override fun onStartTrackingTouch(arg0: SeekBar) {}
//                override fun onProgressChanged(arg0: SeekBar, progress: Int, arg2: Boolean) {
//                    audioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
//                }
//            })
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        setCompleteListener()
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
        binding.playerSheet.playPauseBtn.setImageResource(R.drawable.ic_baseline_pause_24)
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
        binding.playerSheet.playPauseBtn.setImageResource(R.drawable.ic_baseline_pause_24)
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
        binding.playerSheet.playPauseBtn.setImageResource(R.drawable.ic_baseline_pause_24)
        binding.miniPlayPauseBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
        setTime()
        updateProgress()
    }

    private fun pause(){
        musicPlayerService!!.pause()
        binding.playerSheet.playPauseBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24)
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

        onlineArtistViewModel.getAllArtistFromSong(songList!![songPosition], 0)
        onlineArtistViewModel.artistInSong[0].observe(this){
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
                    binding.miniSongArtist.text = text
                    binding.playerSheet.artistTxt.text = text
                }
            }
        }

        songList!![songPosition].views = songList!![songPosition].views?.toInt()?.plus(1).toString()
        onlineSongViewModel.updateViewForSong(songList!![songPosition])
        onlineSongViewModel.updateView.observe(this){
            when(it){
                is UiState.Loading -> {

                }
                is UiState.Failure -> {

                }
                is UiState.Success -> {
                    Log.i("TAG502", "loadUI: ${it.data}")
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

    private fun preparePlayer(){
        binding.miniPlayPauseBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
        binding.playerSheet.playPauseBtn.setImageResource(R.drawable.ic_baseline_pause_24)
        if (!isServiceConnected){
            initState()
            binding.miniPlayerLayout.visibility = View.VISIBLE
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
            Toast.makeText(this, "Song ${currentSong.name} added to ${playlist.name} playlist", Toast.LENGTH_SHORT).show()
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

}