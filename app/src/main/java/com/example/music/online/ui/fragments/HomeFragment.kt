package com.example.music.online.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.music.R
import com.example.music.databinding.FragmentHomeBinding
import com.example.music.online.data.models.*
import com.example.music.online.ui.activities.OnlineMainActivity
import com.example.music.online.ui.adapters.*
import com.example.music.online.viewModels.*
import com.example.music.utils.*
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class HomeFragment(private val clickSongFromDetail: ClickSongFromDetail): Fragment(),
    OnlinePlaylistInHomeAdapter.ClickAPlaylist,
    OnlineArtistAdapter.ClickAnArtist,
    OnlineGenreAdapter.ClickAGenre,
    OnlineAlbumAdapter.ClickAnAlbum,
    OnlineCountryAdapter.ClickACountry,
    DetailCollectionFragment.ClickASongInDetail,
    OnlineSongAdapter.ItemSongClickListener,
    OnlineDialogPlaylistAdapter.ItemClickListener{

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val onlinePlaylistViewModel: OnlinePlaylistViewModel by viewModels()
    private val onlineArtistViewModel: OnlineArtistViewModel by viewModels()
    private val onlineGenreViewModel: OnlineGenreViewModel by viewModels()
    private val onlineAlbumViewModel: OnlineAlbumViewModel by viewModels()
    private val onlineCountryViewModel: OnlineCountryViewModel by viewModels()
    private val onlineAccountViewModel: OnlineAccountViewModel by viewModels()
    private val onlineViewViewModel: OnlineViewViewModel by viewModels()
    private val onlineSongViewModel: OnlineSongViewModel by viewModels()
    private val firebaseViewModel: FirebaseViewModel by viewModels()

    private val onlinePlaylistInHomeAdapter: OnlinePlaylistInHomeAdapter by lazy {
        OnlinePlaylistInHomeAdapter(requireContext(), this)
    }

    private val onlineArtistAdapter: OnlineArtistAdapter by lazy {
        OnlineArtistAdapter(requireContext(), this)
    }

    private val onlineGenreAdapter: OnlineGenreAdapter by lazy {
        OnlineGenreAdapter(requireContext(), this)
    }

    private val onlineAlbumAdapter: OnlineAlbumAdapter by lazy {
        OnlineAlbumAdapter(requireContext(), this)
    }

    private val onlineCountryAdapter: OnlineCountryAdapter by lazy {
        OnlineCountryAdapter(requireContext(), this)
    }

    private val onlineSongAdapter: OnlineSongAdapter by lazy {
        OnlineSongAdapter(requireContext(), this, this, onlineArtistViewModel)
    }

    private val onlineDialogPlaylistAdapter: OnlineDialogPlaylistAdapter by lazy {
        OnlineDialogPlaylistAdapter(requireContext(), this)
    }

    var songs: List<OnlineSong> = emptyList()

    private var sharedPreference: SharedPreferences? = null

    private lateinit var currentSong: OnlineSong

    var handler: Handler = Handler(Looper.getMainLooper())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //load data for recent
        with(binding.recentRv){
            adapter = onlineSongAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        }
        sharedPreference = requireActivity().getSharedPreferences(Recent.SHARE_REF, Context.MODE_PRIVATE)

        handler.postDelayed(object : Runnable{
            override fun run() {
                //Set the values
                if (Recent.IDs.isNotEmpty()){
                    val gson = Gson()
                    val jsonTextIn = gson.toJson(Recent.IDs)
                    val editor = sharedPreference!!.edit()
                    editor.putString("listID", jsonTextIn)
                    editor.apply()
                }

                //Retrieve the values
                val jsonTextOut = sharedPreference!!.getString("listID", "")
                if (jsonTextOut != ""){
                    val gson = Gson()
                    val idList = gson.fromJson(jsonTextOut, Array<String>::class.java).toList()
                    if (getView() != null){
                        firebaseViewModel.getSongFromListSongIDForRecent(idList)
                        firebaseViewModel.songFromIDRecent.observe(viewLifecycleOwner){
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
                    if (_binding != null){
                        binding.recentLayout.visibility = View.VISIBLE
                    }
                }
                else {
                    if (_binding != null){
                        binding.recentLayout.visibility = View.GONE
                    }
                }

                handler.postDelayed(this, 5000)
            }
        }, 1000)

        createChart()

        //load all music
        onlineSongViewModel.getAllSongs()
        onlineSongViewModel.song.observe(viewLifecycleOwner){
            when(it){
                is UiState.Loading -> {

                }
                is UiState.Failure -> {

                }
                is UiState.Success -> {
                    songs = it.data
                }
            }
        }

        showGreeting()

        binding.playGameTxt.setOnClickListener {
            if ((activity as OnlineMainActivity).musicPlayerService != null){
                if ((activity as OnlineMainActivity).musicPlayerService!!.isPlaying()){
                    (activity as OnlineMainActivity).pause()
                }
            }
            createGame()
        }

        //load data for playlist
        with(binding.playlistRv){
            adapter = onlinePlaylistInHomeAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        }

        onlinePlaylistViewModel.getAllPlaylists()
        onlinePlaylistViewModel.playlist2.observe(viewLifecycleOwner){ playlist ->
            when(playlist){
                is UiState.Loading -> {

                }
                is UiState.Failure -> {

                }
                is UiState.Success -> {

                    for (x in playlist.data){
                        if (x.name == FireStoreCollection.TRENDING){
                            //get top 10 song's ids by views
                            onlineViewViewModel.getAllModelIDByName(FireStoreCollection.SONG)
                            onlineViewViewModel.getAllModelIDByName.observe(viewLifecycleOwner){ listID ->
                                when(listID) {
                                    is UiState.Loading -> {

                                    }
                                    is UiState.Failure -> {

                                    }
                                    is UiState.Success -> {
                                        x.songs = listID.data
                                        Log.i("TAG502", "onViewCreated: ${listID.data}")
                                        onlinePlaylistInHomeAdapter.setData(playlist.data)
                                    }
                                }
                            }
                            break
                        }
                    }
                    onlinePlaylistInHomeAdapter.setData(playlist.data)

                }
            }
        }

        //load data for artist
        with(binding.artistRv){
            adapter = onlineArtistAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        }
        onlineArtistViewModel.getAllArtists()
        onlineArtistViewModel.artist.observe(viewLifecycleOwner){
            when(it){
                is UiState.Loading -> {

                }
                is UiState.Failure -> {

                }
                is UiState.Success -> {
                    onlineArtistAdapter.setData(it.data)
                }
            }
        }

        //load data for genre
        with(binding.genreRv){
            adapter = onlineGenreAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        }
        onlineGenreViewModel.getAllGenres()
        onlineGenreViewModel.genre.observe(viewLifecycleOwner){
            when(it){
                is UiState.Loading -> {

                }
                is UiState.Failure -> {

                }
                is UiState.Success -> {
                    onlineGenreAdapter.setData(it.data)
                }
            }
        }

        //load data for album
        with(binding.albumRv){
            adapter = onlineAlbumAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        }
        onlineAlbumViewModel.getAllAlbums()
        onlineAlbumViewModel.album.observe(viewLifecycleOwner){
            when(it){
                is UiState.Loading -> {

                }
                is UiState.Failure -> {

                }
                is UiState.Success -> {
                    onlineAlbumAdapter.setData(it.data)
                }
            }
        }

        //load data for country
        with(binding.countryRv){
            adapter = onlineCountryAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        }
        onlineCountryViewModel.getAllCountries()
        onlineCountryViewModel.country.observe(viewLifecycleOwner){
            when(it){
                is UiState.Loading -> {

                }
                is UiState.Failure -> {

                }
                is UiState.Success -> {
                    onlineCountryAdapter.setData(it.data)
                }
            }
        }

        val imageList = arrayListOf(
            SlideModel(R.drawable.slide_1, ""),
            SlideModel(R.drawable.slide_2, ""),
            SlideModel(R.drawable.slide_3, ""),
            SlideModel(R.drawable.slide_4, ""),
            SlideModel(R.drawable.slide_5, ""),
            SlideModel(R.drawable.alan_walker_slide, ""),
            SlideModel(R.drawable.poster_08, "")
        )
        binding.sliderImg.setImageList(imageList, ScaleTypes.FIT)

        binding.playListSeeAll.setOnClickListener {
            if (binding.playListSeeAll.text.toString() == getString(R.string.collapse)){
                binding.playListSeeAll.text = getString(R.string.see_more)
                binding.playlistRv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            }
            else {
                binding.playListSeeAll.text = getString(R.string.collapse)
                binding.playlistRv.layoutManager = GridLayoutManager(requireContext(), 2, RecyclerView.VERTICAL, false)
            }
        }

        binding.artistSeeAll.setOnClickListener {
            if (binding.artistSeeAll.text.toString() == getString(R.string.collapse)){
                binding.artistSeeAll.text = getString(R.string.see_more)
                binding.artistRv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            }
            else {
                binding.artistSeeAll.text = getString(R.string.collapse)
                binding.artistRv.layoutManager = GridLayoutManager(requireContext(), 2, RecyclerView.VERTICAL, false)
            }
        }

        binding.albumSeeAll.setOnClickListener {
            if (binding.albumSeeAll.text.toString() == getString(R.string.collapse)){
                binding.albumSeeAll.text = getString(R.string.see_more)
                binding.albumRv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            }
            else {
                binding.albumSeeAll.text = getString(R.string.collapse)
                binding.albumRv.layoutManager = GridLayoutManager(requireContext(), 2, RecyclerView.VERTICAL, false)
            }
        }

        binding.genreSeeAll.setOnClickListener {
            if (binding.genreSeeAll.text.toString() == getString(R.string.collapse)){
                binding.genreSeeAll.text = getString(R.string.see_more)
                binding.genreRv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            }
            else {
                binding.genreSeeAll.text = getString(R.string.collapse)
                binding.genreRv.layoutManager = GridLayoutManager(requireContext(), 2, RecyclerView.VERTICAL, false)
            }
        }

        binding.countrySeeAll.setOnClickListener {
            if (binding.countrySeeAll.text.toString() == getString(R.string.collapse)){
                binding.countrySeeAll.text = getString(R.string.see_more)
                binding.countryRv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            }
            else {
                binding.countrySeeAll.text = getString(R.string.collapse)
                binding.countryRv.layoutManager = GridLayoutManager(requireContext(), 2, RecyclerView.VERTICAL, false)
            }
        }

    }

    private fun createChart() {

        with(binding.chartView){
            setNoDataTextColor(Color.WHITE)
            setNoDataText("Come here to join us...")
            description.isEnabled = false
            legend.isEnabled = false
            centerText = "trending song views".uppercase(Locale.ROOT)
            setCenterTextColor(Color.WHITE)
            transparentCircleRadius = 10F
            setHoleColor(Color.TRANSPARENT)
        }

        val dataValue = listOf(
            PieEntry(77F, "Hellcat"),
            PieEntry(69F, "Arrow"),
            PieEntry(59F, "Link"),
            PieEntry(38F, "Energy"),
            PieEntry(28F, "Nova")
        )

        val dataSet = PieDataSet(dataValue, "")
        dataSet.colors = listOf(
            Color.parseColor("#F2A65A"),
            Color.GRAY,
            Color.parseColor("#B12D77"),
            Color.parseColor("#20BF55"),
            Color.parseColor("#01BAEF"))
        dataSet.valueTextSize = 20F
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueFormatter = DefaultValueFormatter(0)

        val pieData = PieData()
        pieData.addDataSet(dataSet)

        binding.chartView.data = pieData

        getDataForChart()

    }

    private fun getDataForChart(){
        val viewsForChart: ArrayList<Int> = ArrayList()
        FirebaseFirestore.getInstance()
            .collection(FireStoreCollection.VIEW)
            .whereEqualTo("modelName", "Song").limit(10).orderBy("quantity", Query.Direction.DESCENDING)
            .addSnapshotListener { value, _ ->
                val views: ArrayList<String> = ArrayList()
                if (value != null){
                    for (document in value){
                        val view = document.toObject(OnlineView::class.java)
                        viewsForChart.add(view.quantity!!)
                        views.add(view.modelId!!)
                    }
                    firebaseViewModel.getSongFromListSongID(views)
                    firebaseViewModel.songFromID.observe(viewLifecycleOwner){
                        when (it) {
                            is UiState.Loading -> {

                            }
                            is UiState.Failure -> {

                            }
                            is UiState.Success -> {
                                val dataValue: ArrayList<PieEntry> = ArrayList()
                                for (i in 0..4){
                                    dataValue.add(PieEntry(viewsForChart[i].toFloat(), it.data[i].name))
                                }
                                val dataSet = PieDataSet(dataValue, "")
                                dataSet.colors = listOf(
                                    Color.parseColor("#F2A65A"),
                                    Color.GRAY,
                                    Color.parseColor("#B12D77"),
                                    Color.parseColor("#20BF55"),
                                    Color.parseColor("#01BAEF"))
                                dataSet.valueTextSize = 20F
                                dataSet.valueTextColor = Color.WHITE
                                dataSet.valueFormatter = DefaultValueFormatter(0)

                                val pieData = PieData()
                                pieData.addDataSet(dataSet)

                                if (_binding != null){
                                    binding.chartView.data = pieData
                                }
                            }
                        }
                    }
                }
            }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun createGame(){

        //create dialog
        val dialog = createDialog(R.layout.game_dialog)

//        val timeTxt = dialog.findViewById<TextView>(R.id.time_txt)
        val replayTxt = dialog.findViewById<TextView>(R.id.replayTxt)
        val answers: List<TextView> = listOf(
            dialog.findViewById(R.id.ans_A),
            dialog.findViewById(R.id.ans_B),
            dialog.findViewById(R.id.ans_C),
            dialog.findViewById(R.id.ans_D)
        )

        val shuffle: ArrayList<OnlineSong> = ArrayList()
        var tempInt = -1

        while (true){
            val randomInt = Random(System.currentTimeMillis()).nextInt(songs.size)
            if (tempInt != randomInt){
                shuffle.add(songs[randomInt])
                tempInt = randomInt
            }
            if (shuffle.size == 4){
                break
            }
        }

        val randomInt = Random(System.currentTimeMillis()).nextInt(4)

        val correctAns = shuffle[randomInt]

        val mediaPlayer = MediaPlayer()

        GlobalScope.launch {
            with(mediaPlayer) {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(correctAns.filePath)
                prepare()
                start()
            }
        }

        for (i in 0..3){
            answers[i].text = shuffle[i].name.toString()
            answers[i].setOnClickListener {
                checkAnswer(answers[i], mediaPlayer, correctAns, answers)
                replayTxt.visibility = View.VISIBLE
            }
        }

        dialog.setOnCancelListener {
            stopMusic(mediaPlayer)
        }

        replayTxt.setOnClickListener {
            stopMusic(mediaPlayer)
            dialog.cancel()
            createGame()
        }

        dialog.show()
    }

    private fun checkAnswer(view: TextView, mediaPlayer: MediaPlayer, correctAns: OnlineSong, answers: List<TextView>){
        if (view.text.toString() == correctAns.name){
            toast("You win!")
            view.setBackgroundResource(R.drawable.rounded_item_correct)
        }
        else {
            toast("You lose!")
            view.setBackgroundResource(R.drawable.rounded_item_in_correct)
            for (i in 0..3){
                if (answers[i].text.toString() == correctAns.name){
                    answers[i].setBackgroundResource(R.drawable.rounded_item_correct)
                }
            }
        }
        stopMusic(mediaPlayer)
    }

    private fun stopMusic(mediaPlayer: MediaPlayer){
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable{
            override fun run() {
                if (mediaPlayer.isPlaying){
                    mediaPlayer.stop()
                }
                handler.postDelayed(this, 1000)
            }
        }, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    @SuppressLint("SimpleDateFormat")
    private fun showGreeting(){
        val sdf = SimpleDateFormat("HH:mm:ss")

        var name = ""

        val currentUserID = Firebase.auth.currentUser?.uid

        if (currentUserID != null){
            onlineAccountViewModel.getAccountByID(currentUserID)
            onlineAccountViewModel.accountByID.observe(viewLifecycleOwner){
                when (it) {
                    is UiState.Loading -> {

                    }
                    is UiState.Failure -> {

                    }
                    is UiState.Success -> {
                        name = it.data.name.toString()
                    }
                }
            }
        }

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable{
            override fun run() {
                val currentTime = sdf.format(Date()).dropLast(6).toInt()
                var greeting = "Welcome, guest"
                if (currentTime < 24){
                    greeting = WelcomeText.EVENING
                }
                if (currentTime < 18){
                    greeting = WelcomeText.AFTERNOON
                }
                if (currentTime < 12){
                    greeting = WelcomeText.MORNING
                }
                if (_binding != null){
                    binding.welcomeTv.text = greeting.plus(", $name")
                }
                handler.postDelayed(this, 1000)
            }
        }, 1000)

    }

    private fun sendDataToDetailFragment(name: String, songs: List<String>, imgFilePath: String){
        val fragmentTransaction: FragmentTransaction = parentFragmentManager.beginTransaction()
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, 0, 0, 0)
        fragmentTransaction.add(R.id.fragment_container, DetailCollectionFragment(name, songs, imgFilePath, this))
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    override fun callBackFromPlaylistClick(playlist: OnlinePlaylist) {
        sendDataToDetailFragment(playlist.name!!, playlist.songs!!, playlist.imgFilePath!!)
        updateViewForModel(playlist.id!!, onlineViewViewModel)

    }

    override fun callBackFromArtistClick(artist: OnlineArtist) {
        sendDataToDetailFragment(artist.name!!, artist.songs!!, artist.imgFilePath!!)
        updateViewForModel(artist.id!!, onlineViewViewModel)
    }

    override fun callBackFromGenreClick(genre: OnlineGenre) {
        sendDataToDetailFragment(genre.name!!, genre.songs!!, genre.imgFilePath!!)
        updateViewForModel(genre.id!!, onlineViewViewModel)
    }

    override fun callBackFromAlbumClick(album: OnlineAlbum) {
        sendDataToDetailFragment(album.name!!, album.songs!!, album.imgFilePath!!)
        updateViewForModel(album.id!!, onlineViewViewModel)
    }

    override fun callBackFromClickASongInDetail(songList: List<OnlineSong>, position: Int) {
        clickSongFromDetail.callBackFromClickSongInDetail(songList, position)
    }

    interface ClickSongFromDetail{
        fun callBackFromClickSongInDetail(songList: List<OnlineSong>, position: Int)
    }

    override fun callBackFromCountryClick(country: OnlineCountry) {
        sendDataToDetailFragment(country.name!!, country.songs!!, country.imgFilePath!!)
        updateViewForModel(country.id!!, onlineViewViewModel)
    }

    override fun callBackFromMenuSongClick(
        action: String,
        songList: List<OnlineSong>,
        position: Int
    ) {
        if (action == "Play"){
            clickSongFromDetail.callBackFromClickSongInDetail(songList, position)
        }
        if (action == "Add to playlist"){
            createDialogForAddToPlaylist(onlinePlaylistViewModel, onlineDialogPlaylistAdapter)
            currentSong = songList[position]
        }
        if (action == "Delete"){
//            createDialogForDeleteSong(songList[position])
            toast("Just for fun, you can't delete :>")
        }
    }

    override fun callBackFromSongClick(songList: List<OnlineSong>, position: Int) {
        clickSongFromDetail.callBackFromClickSongInDetail(songList, position)
    }

    override fun onMenuClick(action: String, playlist: OnlinePlaylist) {
        if (action == "Rename"){
            createDialogForRenamePlaylist(playlist, onlinePlaylistViewModel)
        }
        if (action == "Delete"){
            createDialogForDeletePlaylist(playlist, onlinePlaylistViewModel)
        }
    }

    override fun onItemPlaylistClick(playlist: OnlinePlaylist) {
        //add song to selected playlist
        val songSelected = currentSong

        FirebaseAuth.getInstance().currentUser?.let {
            onlineSongViewModel.addSongToPlaylist(songSelected, playlist, it)
        }

        onlineSongViewModel.addSongInPlaylist.observe(viewLifecycleOwner) {
            when (it) {
                is UiState.Loading -> {

                }
                is UiState.Failure -> {

                }
                is UiState.Success -> {
                    toast("Song ${songSelected.name} added to ${playlist.name} playlist")
                }
            }
        }
    }

}