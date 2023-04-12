package com.example.music.online.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.music.R
import com.example.music.databinding.FragmentSearchBinding
import com.example.music.online.data.models.*
import com.example.music.online.ui.adapters.*
import com.example.music.online.viewModels.*
import com.example.music.utils.*
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class SearchFragment(private val clickSongFromDetail: ClickSongFromDetail) : Fragment(),
    OnlinePlaylistInHomeAdapter.ClickAPlaylist,
    OnlineArtistAdapter.ClickAnArtist,
    OnlineGenreAdapter.ClickAGenre,
    OnlineAlbumAdapter.ClickAnAlbum,
    OnlineCountryAdapter.ClickACountry,
    DetailCollectionFragment.ClickASongInDetail,
    OnlineSongAdapter.ItemSongClickListener,
    OnlineDialogPlaylistAdapter.ItemClickListener{

    private var _binding: FragmentSearchBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val onlinePlaylistViewModel: OnlinePlaylistViewModel by viewModels()
    private val onlineArtistViewModel: OnlineArtistViewModel by viewModels()
    private val onlineGenreViewModel: OnlineGenreViewModel by viewModels()
    private val onlineAlbumViewModel: OnlineAlbumViewModel by viewModels()
    private val onlineSongViewModel: OnlineSongViewModel by viewModels()
    private val onlineCountryViewModel: OnlineCountryViewModel by viewModels()
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

    private val onlineSongAdapter: OnlineSongAdapter by lazy {
        OnlineSongAdapter(requireContext(), this, viewLifecycleOwner, onlineArtistViewModel)
    }

    private val onlineDialogPlaylistAdapter: OnlineDialogPlaylistAdapter by lazy {
        OnlineDialogPlaylistAdapter(requireContext(), this)
    }

    private lateinit var currentSong: OnlineSong
    private var initialList: List<OnlineSong>? = null

    private var albums: List<OnlineAlbum> = emptyList()
    private var playlists: List<OnlinePlaylist> = emptyList()
    private var artists: List<OnlineArtist> = emptyList()
    private var genres: List<OnlineGenre> = emptyList()
    private var songs: List<OnlineSong> = emptyList()
    private var countries: List<OnlineCountry> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //get all songs
        with(binding.songRecyclerView) {
            adapter = onlineSongAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        onlineSongViewModel.getAllSongs()
        onlineSongViewModel.song.observe(viewLifecycleOwner){
            when(it){
                is UiState.Loading -> {

                }
                is UiState.Failure -> {

                }
                is UiState.Success -> {
                    onlineSongAdapter.setData(it.data)
                    songs = it.data
                }
            }
        }

        //load data for playlist
        with(binding.playlistRv){
            adapter = onlinePlaylistInHomeAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        }
        onlinePlaylistViewModel.getAllPlaylists()
        onlinePlaylistViewModel.playlist2.observe(viewLifecycleOwner){
            when(it){
                is UiState.Loading -> {

                }
                is UiState.Failure -> {

                }
                is UiState.Success -> {
                    playlists = it.data
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
                    artists = it.data
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
                    genres = it.data
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
                    albums = it.data
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
                    countries = it.data
                }
            }
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(text: String): Boolean {
                val trimText = text.trim()
                filterPlaylist(trimText)
                filterArtist(trimText)
                filterGenre(trimText)
                filterAlbum(trimText)
                filterSong(trimText)
                filterCountry(trimText)
                return false
            }

            override fun onQueryTextChange(text: String): Boolean {
                val trimText = text.trim()
                filterPlaylist(trimText)
                filterArtist(trimText)
                filterGenre(trimText)
                filterAlbum(trimText)
                filterSong(trimText)
                filterCountry(trimText)
                return false
            }
        })
    }

    private fun filterCountry(text: String) {
        val filter: MutableList<OnlineCountry> = mutableListOf()
        for (item in countries) {
            if (item.name!!.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                filter.add(item)
            }
        }
        if (filter.isEmpty()) {
            binding.countryLayout.visibility = View.GONE
        }
        if (text.isEmpty()){
            binding.countryLayout.visibility = View.GONE
        }
        else {
            onlineCountryAdapter.setData(filter)
            binding.countryLayout.visibility = View.VISIBLE
        }
    }

    private fun filterSong(text: String) {
        // creating a new array list to filter our data.
        val filter = ArrayList<OnlineSong>()
        // running a for loop to compare elements.
        for (item in songs) {
            // checking if the entered string matched with any item of our recycler view.
            if (item.name!!.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                // if the item is matched we are
                // adding it to our filtered list.
                filter.add(item)
            }
        }
        if (filter.isEmpty()) {
            onlineSongAdapter.setData(songs)
        }
        if (text.isEmpty()){
            onlineSongAdapter.setData(songs)
        }
        else {
            onlineSongAdapter.setData(filter)
        }
    }

    private fun filterAlbum(text: String) {
        val filter: ArrayList<OnlineAlbum> = ArrayList<OnlineAlbum>()

        for (item in albums) {
            if (item.name!!.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                filter.add(item)
            }
        }
        if (filter.isEmpty()) {
            binding.albumLayout.visibility = View.GONE
        }
        if (text.isEmpty()){
            binding.albumLayout.visibility = View.GONE
        }
        else {
            onlineAlbumAdapter.setData(filter)
            binding.albumLayout.visibility = View.VISIBLE
        }
    }

    private fun filterGenre(text: String) {
        val filter: ArrayList<OnlineGenre> = ArrayList<OnlineGenre>()

        for (item in genres) {
            if (item.name!!.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                filter.add(item)
            }
        }
        if (filter.isEmpty()) {
            binding.genreLayout.visibility = View.GONE
        }
        if (text.isEmpty()){
            binding.genreLayout.visibility = View.GONE
        }
        else {
            onlineGenreAdapter.setData(filter)
            binding.genreLayout.visibility = View.VISIBLE
        }
    }

    private fun filterArtist(text: String) {
        val filter: ArrayList<OnlineArtist> = ArrayList<OnlineArtist>()

        for (item in artists) {
            if (item.name!!.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                filter.add(item)
            }
        }
        if (filter.isEmpty()) {
            binding.artistLayout.visibility = View.GONE
        }
        if (text.isEmpty()){
            binding.artistLayout.visibility = View.GONE
        }
        else {
            onlineArtistAdapter.setData(filter)
            binding.artistLayout.visibility = View.VISIBLE
        }
    }

    private fun filterPlaylist(text: String) {
        val filter: ArrayList<OnlinePlaylist> = ArrayList<OnlinePlaylist>()

        for (item in playlists) {
            if (item.name!!.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                filter.add(item)
            }
        }
        if (filter.isEmpty()) {
            binding.playlistLayout.visibility = View.GONE
        }
        if (text.isEmpty()){
            binding.playlistLayout.visibility = View.GONE
        }
        else {
            onlinePlaylistInHomeAdapter.setData(filter)
            binding.playlistLayout.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun sendDataToDetailFragment(name: String, songs: List<String>, imgFilePath: String){
        val fragmentTransaction: FragmentTransaction = parentFragmentManager.beginTransaction()
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

}