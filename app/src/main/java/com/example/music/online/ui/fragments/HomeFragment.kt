package com.example.music.online.ui.fragments

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import java.sql.Types.NULL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class HomeFragment(private val clickSongFromDetail: ClickSongFromDetail): Fragment(),
    OnlinePlaylistInHomeAdapter.ClickAPlaylist,
    OnlineArtistAdapter.ClickAnArtist,
    OnlineGenreAdapter.ClickAGenre,
    OnlineAlbumAdapter.ClickAnAlbum,
    OnlineCountryAdapter.ClickACountry,
    DetailCollectionFragment.ClickASongInDetail {

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

    var songs: List<OnlineSong> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            SlideModel(R.drawable.poster_06, ""),
            SlideModel(R.drawable.poster_07, ""),
            SlideModel(R.drawable.alan_walker_slide, ""),
            SlideModel(R.drawable.poster_08, "")
        )
        binding.sliderImg.setImageList(imageList, ScaleTypes.FIT)
    }

    private fun createGame(){

        //create dialog
        val dialog = createDialog(R.layout.game_dialog)

        val timeTxt = dialog.findViewById<TextView>(R.id.time_txt)
        val againLayout = dialog.findViewById<LinearLayout>(R.id.again)
        val answers: List<TextView> = listOf(
            dialog.findViewById(R.id.ans_A),
            dialog.findViewById(R.id.ans_B),
            dialog.findViewById(R.id.ans_C),
            dialog.findViewById(R.id.ans_D)
        )
        val replayBtn = dialog.findViewById<ImageButton>(R.id.replayBtn)

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
                checkAnswer(answers[i], mediaPlayer, correctAns)
                againLayout.visibility = View.VISIBLE
            }
        }

        dialog.setOnCancelListener {
            if (mediaPlayer.isPlaying)
                mediaPlayer.stop()
        }

        replayBtn.setOnClickListener {
            if (mediaPlayer.isPlaying)
                mediaPlayer.stop()
            dialog.cancel()
            createGame()
        }

        dialog.show()
    }

    private fun checkAnswer(view: TextView, mediaPlayer: MediaPlayer, correctAns: OnlineSong){
        if (view.text.toString() == correctAns.name){
            toast("You're winner!")
            view.setBackgroundResource(R.drawable.rounded_item_correct)
        }
        else {
            toast("You're loser!")
            view.setBackgroundResource(R.drawable.rounded_item_in_correct)
        }
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

}