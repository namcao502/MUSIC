package com.example.music.online.ui.fragments

import android.os.Bundle
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
import com.example.music.utils.UiState
import com.example.music.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


@AndroidEntryPoint
class SearchFragment(private val clickSongFromDetail: ClickSongFromDetail) : Fragment(),
    OnlinePlaylistInHomeAdapter.ClickAPlaylist,
    OnlineArtistAdapter.ClickAnArtist,
    OnlineGenreAdapter.ClickAGenre,
    OnlineAlbumAdapter.ClickAnAlbum,
    OnlineSongInSearchAdapter.ClickASong,
    OnlineCountryAdapter.ClickACountry,
    DetailCollectionFragment.ClickASongInDetail {

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

    private val onlineSongInSearchAdapter: OnlineSongInSearchAdapter by lazy {
        OnlineSongInSearchAdapter(requireContext(),this)
    }

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

        //load data for song
        with(binding.songRv){
            adapter = onlineSongInSearchAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        }
        onlineSongViewModel.getAllSongForSearch()
        onlineSongViewModel.songForSearch.observe(viewLifecycleOwner){
            when(it){
                is UiState.Loading -> {

                }
                is UiState.Failure -> {

                }
                is UiState.Success -> {
                    onlineSongInSearchAdapter.setData(it.data)
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
                    onlinePlaylistInHomeAdapter.setData(it.data)
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

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(text: String): Boolean {
                filterPlaylist(text)
                filterArtist(text)
                filterGenre(text)
                filterAlbum(text)
                filterSong(text)
                filterCountry(text)
                return false
            }

            override fun onQueryTextChange(text: String): Boolean {
                filterPlaylist(text)
                filterArtist(text)
                filterGenre(text)
                filterAlbum(text)
                filterSong(text)
                filterCountry(text)
                return false
            }

        })
    }

    private fun filterCountry(text: String) {
        val filter: ArrayList<OnlineCountry> = ArrayList<OnlineCountry>()

        for (item in onlineCountryAdapter.countries) {
            if (item.name!!.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                filter.add(item)
            }
        }
        if (filter.isEmpty()) {
            toast("Not found")
            binding.countryRv.visibility = View.GONE
        }
        if (text.isEmpty()){
            binding.countryRv.visibility = View.GONE
        }
        else {
            onlineCountryAdapter.setData(filter)
            binding.countryRv.visibility = View.VISIBLE
        }
    }

    private fun filterSong(text: String) {
        // creating a new array list to filter our data.
        val filter: ArrayList<OnlineSong> = ArrayList<OnlineSong>()
        // running a for loop to compare elements.
        for (item in onlineSongInSearchAdapter.songList) {
            // checking if the entered string matched with any item of our recycler view.
            if (item.name!!.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                // if the item is matched we are
                // adding it to our filtered list.
                filter.add(item)
            }
        }
        if (filter.isEmpty()) {
            // if no item is added in filtered list we are
            // displaying a toast message as no data found.
            toast("Not found")
            binding.songRv.visibility = View.GONE
        }
        if (text.isEmpty()){
            binding.songRv.visibility = View.GONE
        }
        else {
            // at last we are passing that filtered
            // list to our adapter class.
            onlineSongInSearchAdapter.setData(filter)
            binding.songRv.visibility = View.VISIBLE
        }
    }

    private fun filterAlbum(text: String) {
        val filter: ArrayList<OnlineAlbum> = ArrayList<OnlineAlbum>()

        for (item in onlineAlbumAdapter.album) {
            if (item.name!!.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                filter.add(item)
            }
        }
        if (filter.isEmpty()) {
            toast("Not found")
            binding.albumRv.visibility = View.GONE
        }
        if (text.isEmpty()){
            binding.albumRv.visibility = View.GONE
        }
        else {
            onlineAlbumAdapter.setData(filter)
            binding.albumRv.visibility = View.VISIBLE
        }
    }

    private fun filterGenre(text: String) {
        val filter: ArrayList<OnlineGenre> = ArrayList<OnlineGenre>()

        for (item in onlineGenreAdapter.genre) {
            if (item.name!!.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                filter.add(item)
            }
        }
        if (filter.isEmpty()) {
            toast("Not found")
            binding.genreRv.visibility = View.GONE
        }
        if (text.isEmpty()){
            binding.genreRv.visibility = View.GONE
        }
        else {
            onlineGenreAdapter.setData(filter)
            binding.genreRv.visibility = View.VISIBLE
        }
    }

    private fun filterArtist(text: String) {
        val filter: ArrayList<OnlineArtist> = ArrayList<OnlineArtist>()

        for (item in onlineArtistAdapter.artist) {
            if (item.name!!.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                filter.add(item)
            }
        }
        if (filter.isEmpty()) {
            toast("Not found")
            binding.artistRv.visibility = View.GONE
        }
        if (text.isEmpty()){
            binding.artistRv.visibility = View.GONE
        }
        else {
            onlineArtistAdapter.setData(filter)
            binding.artistRv.visibility = View.VISIBLE
        }
    }

    private fun filterPlaylist(text: String) {
        val filter: ArrayList<OnlinePlaylist> = ArrayList<OnlinePlaylist>()

        for (item in onlinePlaylistInHomeAdapter.playlist) {
            if (item.name!!.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                filter.add(item)
            }
        }
        if (filter.isEmpty()) {
            toast("Not found")
            binding.playlistRv.visibility = View.GONE
        }
        if (text.isEmpty()){
            binding.playlistRv.visibility = View.GONE
        }
        else {
            onlinePlaylistInHomeAdapter.setData(filter)
            binding.playlistRv.visibility = View.VISIBLE
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
    }

    override fun callBackFromArtistClick(artist: OnlineArtist) {
        sendDataToDetailFragment(artist.name!!, artist.songs!!, artist.imgFilePath!!)
    }

    override fun callBackFromGenreClick(genre: OnlineGenre) {
        sendDataToDetailFragment(genre.name!!, genre.songs!!, genre.imgFilePath!!)
    }

    override fun callBackFromAlbumClick(album: OnlineAlbum) {
        sendDataToDetailFragment(album.name!!, album.songs!!, album.imgFilePath!!)
    }

    override fun callBackFromClickASongInDetail(songList: List<OnlineSong>, position: Int) {
        clickSongFromDetail.callBackFromClickSongInDetail(songList, position)
    }

    interface ClickSongFromDetail{
        fun callBackFromClickSongInDetail(songList: List<OnlineSong>, position: Int)
    }

    override fun callBackFromSongClick(songList: List<OnlineSong>, position: Int) {
        clickSongFromDetail.callBackFromClickSongInDetail(songList, position)
    }

    override fun callBackFromCountryClick(country: OnlineCountry) {
        sendDataToDetailFragment(country.name!!, country.songs!!, country.imgFilePath!!)
    }

}