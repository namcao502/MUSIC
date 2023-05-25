package com.example.music.online.ui.activities

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.*
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.provider.MediaStore
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
import com.example.music.online.data.models.OnlineDiary
import com.example.music.online.data.models.OnlinePlaylist
import com.example.music.online.data.models.OnlineSong
import com.example.music.online.services.OnlineMusicPlayerService
import com.example.music.online.ui.adapters.CommentDialogAdapter
import com.example.music.online.ui.adapters.OnlineDialogPlaylistAdapter
import com.example.music.online.ui.adapters.OnlineSongAdapter
import com.example.music.online.ui.fragments.*
import com.example.music.online.viewModels.*
import com.example.music.utils.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class OnlineMainActivity: AppCompatActivity(),
    ServiceConnection,
    OnlineDialogPlaylistAdapter.ItemClickListener,
    HomeFragment.ClickSongFromDetail,
    SearchFragment.ClickSongFromDetail,
    OnlinePlaylistFragment.ClickSongFromDetail,
    CommentDialogAdapter.ClickAComment,
    OnlineSongAdapter.ItemSongClickListener{

    private lateinit var binding: ActivityOnlineMainBinding

    private var diaryFragment = OnlineDiaryFragment()
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
    private val onlineDiaryViewModel: OnlineDiaryViewModel by viewModels()

    private val onlineDialogPlaylistAdapter: OnlineDialogPlaylistAdapter by lazy {
        OnlineDialogPlaylistAdapter(this, this) }

    private val onlineSongAdapter: OnlineSongAdapter by lazy {
        OnlineSongAdapter(this, this, this, onlineArtistViewModel)
    }

    private var doubleBackToExitPressedOnce = false

    private lateinit var connectivityObserver: ConnectivityObserver

    var handler: Handler = Handler(Looper.getMainLooper())

    var handler2: Handler = Handler(Looper.getMainLooper())

    private lateinit var currentSong: OnlineSong

    //Declare timer
    var cTimer: CountDownTimer? = null

    override fun onBackPressed() {

        if (PlayerState.isOn){
            PlayerState.isOn = false
            binding.playerSheet.playerLayout.fadeVisibility(View.GONE)
            binding.bottomCard.fadeVisibility(View.VISIBLE)
            setStatusColor(false)
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

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnlineMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        with(supportFragmentManager.beginTransaction()){
            add(R.id.fragment_container, userFragment).hide(userFragment)
            add(R.id.fragment_container, playlistFragment).hide(playlistFragment)
            add(R.id.fragment_container, diaryFragment).hide(diaryFragment)
            add(R.id.fragment_container, searchFragment).hide(searchFragment)
            add(R.id.fragment_container, homeFragment)
            commit()
        }

        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }

        setStatusColor(false)

        GlobalScope.launch {
            connectivityObserver = NetworkConnectivityObserver(this@OnlineMainActivity)
            connectivityObserver.observe().collect { value ->
                when (value){
                    ConnectivityObserver.Status.Available -> {
                        runOnUiThread {
                            with(binding.internetTxt){
                                setBackgroundColor(Color.GREEN)
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
                                setBackgroundColor(Color.RED)
                                text = ConnectionType.NO_INTERNET
                                visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }
        }

        listener()

//        checkNetwork(binding.internetTxt)

    }

    private fun setStatusColor(playerIsOn: Boolean){
        if (playerIsOn){
            window.navigationBarColor = resources.getColor(R.color.nav_player, this.theme)
            window.statusBarColor = resources.getColor(R.color.status_player, this.theme)
        }
        else {
            window.navigationBarColor = resources.getColor(R.color.nav_bottom, this.theme)
            window.statusBarColor = resources.getColor(R.color.status_top, this.theme)
        }
    }

    @SuppressLint("ClickableViewAccessibility", "SimpleDateFormat", "SetTextI18n")
    private fun listener() {

        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.online_home_menu -> {
                    if (DetailFragmentState.isOn){
                        supportFragmentManager.popBackStack()
                        DetailFragmentState.isOn = false
                    }
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, 0, 0, 0)
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
                        .setCustomAnimations(android.R.anim.fade_in, 0, 0, 0)
                        .hide(activeFragment)
                        .show(searchFragment).commit()
                    activeFragment = searchFragment
                    return@setOnItemSelectedListener true
                }
                R.id.online_diary_menu -> {
                    if (DetailFragmentState.isOn){
                        supportFragmentManager.popBackStack()
                        DetailFragmentState.isOn = false
                    }
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, 0, 0, 0)
                        .hide(activeFragment)
                        .show(diaryFragment).commit()
                    activeFragment = diaryFragment
                    return@setOnItemSelectedListener true
                }
                R.id.online_collection_menu -> {
                    if (DetailFragmentState.isOn){
                        supportFragmentManager.popBackStack()
                        DetailFragmentState.isOn = false
                    }
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, 0, 0, 0)
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
                        .setCustomAnimations(android.R.anim.fade_in, 0, 0, 0)
                        .hide(activeFragment)
                        .show(userFragment).commit()
                    activeFragment = userFragment
                    return@setOnItemSelectedListener true
                }
            }
            return@setOnItemSelectedListener false
        }

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
            val diaryBtn = bottomDialog.findViewById<LinearLayout>(R.id.diary_layout)
            val downloadBtn = bottomDialog.findViewById<LinearLayout>(R.id.download_layout)
            val playStateBtn = bottomDialog.findViewById<LinearLayout>(R.id.play_state_layout)
            val playStateTxt = bottomDialog.findViewById<TextView>(R.id.play_state_txt)
            val timer = bottomDialog.findViewById<LinearLayout>(R.id.timer_layout)
            val ringtone = bottomDialog.findViewById<LinearLayout>(R.id.ringtone_layout)

            ringtone!!.setOnClickListener {
                val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)

                builder.setMessage("Do you want to set this track as your ringtone?")
                    .setTitle("Confirm set ringtone")
                    .setPositiveButton("Agree") { _, _ ->

                        val fileName = binding.miniSongTitle.text.toString().plus(" - ") +
                                binding.miniSongArtist.text.toString().plus(".mp3")

                        val file = File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_MUSIC).toString() + "/$fileName"
                        )

                        if (file.exists()){

                            //We now create a new content values object to store all the information
                            //about the ringtone.
                            val values = ContentValues()
                            values.put(MediaStore.MediaColumns.DATA, file.absolutePath)
                            values.put(MediaStore.MediaColumns.TITLE, file.name)
                            values.put(MediaStore.MediaColumns.SIZE, file.length())
                            values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3")
                            values.put(MediaStore.Audio.AudioColumns.IS_RINGTONE, true)
                            values.put(MediaStore.Audio.AudioColumns.IS_NOTIFICATION, false)
                            values.put(MediaStore.Audio.AudioColumns.IS_ALARM, false)
                            values.put(MediaStore.Audio.AudioColumns.IS_MUSIC, false)

                            //Work with the content resolver now
                            //First get the file we may have added previously and delete it,
                            //otherwise we will fill up the ringtone manager with a bunch of copies over time.

                            val uri = MediaStore.Audio.Media.getContentUriForPath(file.absolutePath)
                            if (uri != null) {
//                                this.contentResolver.delete(
//                                    uri,
//                                    null,
//                                    null,
//                                )
                                //Ok now insert it
                                val newUri: Uri? = this.contentResolver.insert(uri, values)

                                //Ok now set the ringtone from the content manager's uri, NOT the file's uri
                                RingtoneManager.setActualDefaultRingtoneUri(
                                    this,
                                    RingtoneManager.TYPE_RINGTONE,
                                    newUri
                                )

                                toast("This track has been set as your ringtone!")
                            }
                        }
                        else {
                            toast("Please download this song first!")
                        }

                    }
                    .setNegativeButton("No") { _, _ ->
                        // User cancelled the dialog
                    }
                // Create the AlertDialog object and return it
                builder.create().show()
            }

            timer!!.setOnClickListener {

                val dialog = createDialog(R.layout.timer_dialog)
                val timePicker = dialog.findViewById<TimePicker>(R.id.timePicker)
                val setTimeBtn = dialog.findViewById<Button>(R.id.setTime_btn)
                val cancelBtn = dialog.findViewById<Button>(R.id.cancel_btn)

                timePicker.setIs24HourView(true)
                timePicker.hour = 0
                timePicker.minute = 0

                cancelBtn.setOnClickListener {
                    dialog.cancel()
                }

                setTimeBtn.setOnClickListener {
                    val hour = timePicker.hour
                    val minute = timePicker.minute
                    if(cTimer != null){
                        cTimer!!.cancel()
                    }
                    val time = hour * 60 * 60 * 1000 + minute * 60 * 1000
                    cTimer = object : CountDownTimer(time.toLong(), 1000) {
                        override fun onTick(millisUntilFinished: Long) {}
                        override fun onFinish() {
                            handler.removeMessages(0)
                            handler2.removeMessages(0)
                            stopService()
                            if (PlayerState.isOn){
                                PlayerState.isOn = false
                                binding.playerSheet.playerLayout.fadeVisibility(View.GONE)
                                binding.bottomCard.fadeVisibility(View.VISIBLE)
                                setStatusColor(false)
                                return
                            }
                            bottomDialog.dismiss()
                        }
                    }
                    cTimer!!.start()

                    val hourText = if (hour > 1){
                        "hour"
                    } else {
                        "hours"
                    }

                    val minuteText = if (hour > 1){
                        "minute"
                    } else {
                        "minutes"
                    }

                    toast("We will be silent about $hour $hourText and $minute $minuteText!")
                    dialog.cancel()
                }
                dialog.show()
            }

            timer.setOnLongClickListener {
                val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)

                builder.setMessage("Do you want to cancel this timer?")
                    .setTitle("Confirm cancel")
                    .setPositiveButton("Cancel") { _, _ ->
                        if(cTimer != null){
                            cTimer!!.cancel()
                        }
                        toast("Timer has been canceled!")
                    }
                    .setNegativeButton("No") { _, _ ->
                        // User cancelled the dialog
                    }
                // Create the AlertDialog object and return it
                builder.create().show()
                true
            }

            diaryBtn!!.setOnClickListener {
                val dialog = createDialog(R.layout.diary_dialog)

                val subjectTxt = dialog.findViewById<TextView>(R.id.subject_txt)!!
                val contentTxt = dialog.findViewById<TextView>(R.id.content_txt)!!
                val saveBtn = dialog.findViewById<ImageButton>(R.id.save_btn)!!
                val deleteBtn = dialog.findViewById<ImageButton>(R.id.delete_btn)!!
                val songArtistTxt = dialog.findViewById<TextView>(R.id.songArtist_txt)!!
                val dateTimeTxt = dialog.findViewById<TextView>(R.id.datetime_txt)!!

                deleteBtn.visibility = View.GONE

                songArtistTxt.text =
                    binding.miniSongTitle.text.toString()
                    .plus(" - ")
                    .plus(binding.miniSongArtist.text.toString())

                val currentDate = SimpleDateFormat("MM/dd/yyyy HH:mm").format(Date())

                dateTimeTxt.text = currentDate.toString()

                saveBtn.setOnClickListener {

                    if (subjectTxt.text.toString().isEmpty()){
                        toast("Please type a subject...")
                        return@setOnClickListener
                    }

                    if (contentTxt.text.toString().isEmpty()){
                        toast("Please type some content...")
                        return@setOnClickListener
                    }

                    val diary = OnlineDiary(
                        "",
                        subjectTxt.text.toString(),
                        contentTxt.text.toString(),
                        currentDate.toString(),
                        songArtistTxt.text.toString(),
                        songList!![songPosition].id
                    )

                    Firebase.auth.currentUser?.let {
                        onlineDiaryViewModel.addDiary(diary, it)
                    }

                    onlineDiaryViewModel.addDiary.observe(this){
                        when(it){
                            is UiState.Loading -> {

                            }
                            is UiState.Failure -> {

                            }
                            is UiState.Success -> {
                                toast(it.data)
                                dialog.cancel()
                            }
                        }
                    }

                }
                dialog.show()
            }

            commentsBtn!!.setOnClickListener {
                //create bottom sheet dialog
                val dialog = createDialog(R.layout.comment_dialog)

                val commentRv = dialog.findViewById<RecyclerView>(R.id.comment_rv)
                val postBtn = dialog.findViewById<Button>(R.id.post_cmt_btn)
                val message = dialog.findViewById<EditText>(R.id.message_et_cmt_dialog)

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
                dialog.show()
            }

            addToPlaylistBtn!!.setOnClickListener {
                val addDialog = createDialog(R.layout.playlist_dialog)

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
                val manager: DownloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                val uri = Uri.parse(songList!![songPosition].filePath)
                val request = DownloadManager.Request(uri)
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC,
                    binding.miniSongTitle.text.toString().plus(" - ") +
                    binding.miniSongArtist.text.toString() + ".mp3")
                manager.enqueue(request)
                toast("Downloading...")
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
                playStateTxt!!.text = "Mode: ".plus(playState)
                toast("Switched to $playState")
            }

            bottomDialog.show()
        }

        binding.playerSheet.recommend.setOnClickListener {
            val dialog = createDialog(R.layout.recommend_song_dialog)
            val recyclerView = dialog.findViewById<RecyclerView>(R.id.songRv)
            recyclerView.adapter = onlineSongAdapter
            recyclerView.layoutManager = LinearLayoutManager(dialog.context, RecyclerView.VERTICAL, false)

            onlineArtistViewModel.getAllArtistFromSongID(songList!![songPosition].id!!)
            onlineArtistViewModel.artistFromSongID.observe(this){ artists ->
                when(artists){
                    is UiState.Loading -> {

                    }
                    is UiState.Failure -> {

                    }
                    is UiState.Success -> {
                        if (artists.data.isNotEmpty()){
                            val songs = artists.data[0].songs!!
                            firebaseViewModel.getSongFromListSongID(songs)
                            firebaseViewModel.songFromID.observe(this){
                                when(it){
                                    is UiState.Loading -> {

                                    }
                                    is UiState.Failure -> {

                                    }
                                    is UiState.Success -> {
                                        onlineSongAdapter.setData(it.data)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            dialog.show()
        }

        binding.miniPlayerLayout.setOnTouchListener(object : OnSwipeTouchListener(binding.miniPlayerLayout.context) {
            override fun onSwipeLeft() {
                super.onSwipeLeft()
                handler.removeMessages(0)
                handler2.removeMessages(0)
                stopService()
                binding.miniPlayerLayout.fadeVisibility(View.GONE)
            }
            override fun onSwipeRight() {
                super.onSwipeRight()
                handler.removeMessages(0)
                handler2.removeMessages(0)
                stopService()
                binding.miniPlayerLayout.fadeVisibility(View.GONE)
            }
            override fun onSwipeUp() {
                super.onSwipeUp()
                PlayerState.isOn = true
                binding.playerSheet.playerLayout.fadeVisibility(View.VISIBLE)
                binding.bottomCard.fadeVisibility(View.GONE)
                setStatusColor(true)
            }
            override fun onSwipeDown() {
                super.onSwipeDown()
                handler.removeMessages(0)
                handler2.removeMessages(0)
                stopService()
                binding.miniPlayerLayout.fadeVisibility(View.GONE)
            }
            override fun onClick(){
                super.onClick()
                PlayerState.isOn = true
                binding.playerSheet.playerLayout.fadeVisibility(View.VISIBLE)
                binding.bottomCard.fadeVisibility(View.GONE)
                setStatusColor(true)
            }
        })

        binding.playerSheet.backBtn.setOnClickListener {
            PlayerState.isOn = false
            binding.playerSheet.playerLayout.fadeVisibility(View.GONE)
            binding.bottomCard.fadeVisibility(View.VISIBLE)
            setStatusColor(false)
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
        val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
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
    fun pause(){
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

        Glide.with(this).load(R.drawable.recommend).into(binding.playerSheet.recommend)

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
                    if (it.data.isNotEmpty()){
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

                        //save song id for recent
                        Recent.IDs = it.data[0].songs!!
                    }
                }
            }
        }

        //update view
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
        handler2 = Handler(Looper.getMainLooper())
        handler2.postDelayed(object : Runnable {
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

    fun stopService(){
        if (musicPlayerService != null){
            musicPlayerService = null
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

    @OptIn(DelicateCoroutinesApi::class)
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
            handler = Handler(Looper.getMainLooper())
            handler.postDelayed(object : Runnable {
                override fun run() {
                    if (getConnectionType(this@OnlineMainActivity) == ConnectionType.NOT_CONNECT){
                        AlertDialog
                            .Builder(this@OnlineMainActivity, R.style.AlertDialogTheme)
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
        val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)

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
        val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
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

    override fun callBackFromMenuClickComment(action: String, comment: OnlineComment) {
        if (action == "Edit"){
            if (comment.userId!! == Firebase.auth.currentUser!!.uid){

                val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
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
            val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
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

    override fun callBackFromMenuSongClick(
        action: String,
        songList: List<OnlineSong>,
        position: Int
    ) {
        if (action == "Play"){
            this.songList = songList
            this.songPosition = position
            preparePlayer()
        }
        if (action == "Add to playlist"){
            val dialog = createDialog(R.layout.playlist_dialog)

            val recyclerView = dialog.findViewById<RecyclerView>(R.id.playlist_recyclerView)
            recyclerView.adapter = onlineDialogPlaylistAdapter
            recyclerView.layoutManager = LinearLayoutManager(dialog.context)

            FirebaseAuth.getInstance().currentUser?.let {
                onlinePlaylistViewModel.getAllPlaylistOfUser(it)
            }
            onlinePlaylistViewModel.playlist.observe(this){
                when(it){
                    is UiState.Loading -> {

                    }
                    is UiState.Failure -> {

                    }
                    is UiState.Success -> {
                        onlineDialogPlaylistAdapter.setData(it.data)
                    }
                }
            }

            val addBtn = dialog.findViewById<FloatingActionButton>(R.id.add_btn)

            addBtn.setOnClickListener {
                createDialogForAddPlaylist(onlinePlaylistViewModel)
            }
            dialog.show()
            currentSong = songList[position]
        }
        if (action == "Delete"){
//            createDialogForDeleteSong(songList[position])
            toast("Just for fun, you can't delete :>")
        }
    }

    override fun callBackFromSongClick(songList: List<OnlineSong>, position: Int) {
        this.songList = songList
        this.songPosition = position
        preparePlayer()
    }

}