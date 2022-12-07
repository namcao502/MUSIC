package com.example.music.online.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.music.online.ui.adapters.*
import com.example.music.online.viewModels.*
import com.example.music.utils.FireStoreCollection
import com.example.music.utils.UiState
import com.example.music.utils.WelcomeText
import com.example.music.utils.updateViewForModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Runnable
import java.text.SimpleDateFormat
import java.util.*

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showGreeting()

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
            SlideModel(R.drawable.ncs_slide, ""),
            SlideModel(R.drawable.poster_07, ""),
            SlideModel(R.drawable.alan_walker_slide, ""),
            SlideModel(R.drawable.poster_08, "")
        )
        binding.sliderImg.setImageList(imageList, ScaleTypes.FIT)
    }

    private fun updatePlaylist(playlist: OnlinePlaylist){
        onlinePlaylistViewModel.updatePlaylist(playlist)
        onlinePlaylistViewModel.updatePlaylist2.observe(viewLifecycleOwner){
            when (it) {
                is UiState.Loading -> {

                }
                is UiState.Failure -> {

                }
                is UiState.Success -> {
                    Log.i("TAG502", "updatePlaylist: ${it.data}")
                }
            }
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

//        val name = Firebase.auth.currentUser!!.email!!.split("@")[0]

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
        fragmentTransaction.setCustomAnimations(R.anim.fade, 0, 0, 0)
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