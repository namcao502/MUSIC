package com.example.music.online.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


@AndroidEntryPoint
class SearchFragment(private val clickSongFromDetail: ClickSongFromDetail) : Fragment(),
    OnlinePlaylistInHomeAdapter.ClickAPlaylist,
    OnlineArtistAdapter.ClickAnArtist,
    OnlineGenreAdapter.ClickAGenre,
    OnlineAlbumAdapter.ClickAnAlbum,
    OnlineSongInSearchAdapter.ClickASong,
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

        binding.searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                TODO("Not yet implemented")
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterPlaylist(newText)
                filterArtist(newText)
                filterGenre(newText)
                filterAlbum(newText)
                filterSong(newText)
                return false
            }

        })
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
        if (filter.isEmpty() || text.isEmpty()) {
            // if no item is added in filtered list we are
            // displaying a toast message as no data found.
            binding.songRv.visibility = View.GONE
        } else {
            // at last we are passing that filtered
            // list to our adapter class.
            onlineSongInSearchAdapter.setData(filter)
            binding.songRv.visibility = View.VISIBLE
        }
    }

    private fun filterAlbum(text: String) {
        // creating a new array list to filter our data.
        val filter: ArrayList<OnlineAlbum> = ArrayList<OnlineAlbum>()

        // running a for loop to compare elements.
        for (item in onlineAlbumAdapter.album) {
            // checking if the entered string matched with any item of our recycler view.
            if (item.name!!.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                // if the item is matched we are
                // adding it to our filtered list.
                filter.add(item)
            }
        }
        if (filter.isEmpty() || text.isEmpty()) {
            // if no item is added in filtered list we are
            // displaying a toast message as no data found.
            binding.albumRv.visibility = View.GONE
        } else {
            // at last we are passing that filtered
            // list to our adapter class.
            onlineAlbumAdapter.setData(filter)
            binding.albumRv.visibility = View.VISIBLE
        }
    }

    private fun filterGenre(text: String) {
        // creating a new array list to filter our data.
        val filter: ArrayList<OnlineGenre> = ArrayList<OnlineGenre>()

        // running a for loop to compare elements.
        for (item in onlineGenreAdapter.genre) {
            // checking if the entered string matched with any item of our recycler view.
            if (item.name!!.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                // if the item is matched we are
                // adding it to our filtered list.
                filter.add(item)
            }
        }
        if (filter.isEmpty() || text.isEmpty()) {
            // if no item is added in filtered list we are
            // displaying a toast message as no data found.
            binding.genreRv.visibility = View.GONE
        } else {
            // at last we are passing that filtered
            // list to our adapter class.
            onlineGenreAdapter.setData(filter)
            binding.genreRv.visibility = View.VISIBLE
        }
    }

    private fun filterArtist(text: String) {
        // creating a new array list to filter our data.
        val filter: ArrayList<OnlineArtist> = ArrayList<OnlineArtist>()

        // running a for loop to compare elements.
        for (item in onlineArtistAdapter.artist) {
            // checking if the entered string matched with any item of our recycler view.
            if (item.name!!.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                // if the item is matched we are
                // adding it to our filtered list.
                filter.add(item)
            }
        }
        if (filter.isEmpty() || text.isEmpty()) {
            // if no item is added in filtered list we are
            // displaying a toast message as no data found.
            binding.artistRv.visibility = View.GONE
        } else {
            // at last we are passing that filtered
            // list to our adapter class.
            onlineArtistAdapter.setData(filter)
            binding.artistRv.visibility = View.VISIBLE
        }
    }

    private fun filterPlaylist(text: String) {
        // creating a new array list to filter our data.
        val filter: ArrayList<OnlinePlaylist> = ArrayList<OnlinePlaylist>()

        // running a for loop to compare elements.
        for (item in onlinePlaylistInHomeAdapter.playlist) {
            // checking if the entered string matched with any item of our recycler view.
            if (item.name!!.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                // if the item is matched we are
                // adding it to our filtered list.
                filter.add(item)
            }
        }
        if (filter.isEmpty() || text.isEmpty()) {
            // if no item is added in filtered list we are
            // displaying a toast message as no data found.
            binding.playlistRv.visibility = View.GONE
        } else {
            // at last we are passing that filtered
            // list to our adapter class.
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

}