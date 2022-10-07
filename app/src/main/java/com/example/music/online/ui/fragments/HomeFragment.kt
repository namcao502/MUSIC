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
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.music.R
import com.example.music.utils.UiState
import com.example.music.databinding.FragmentHomeBinding
import com.example.music.online.data.models.*
import com.example.music.online.ui.adapters.OnlineAlbumAdapter
import com.example.music.online.ui.adapters.OnlineArtistAdapter
import com.example.music.online.ui.adapters.OnlineGenreAdapter
import com.example.music.online.ui.adapters.OnlinePlaylistInHomeAdapter
import com.example.music.online.viewModels.OnlineAlbumViewModel
import com.example.music.online.viewModels.OnlineArtistViewModel
import com.example.music.online.viewModels.OnlineGenreViewModel
import com.example.music.online.viewModels.OnlinePlaylistViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment(private val clickSongFromDetail: ClickSongFromDetail): Fragment(),
    OnlinePlaylistInHomeAdapter.ClickAPlaylist,
    OnlineArtistAdapter.ClickAnArtist,
    OnlineGenreAdapter.ClickAGenre,
    OnlineAlbumAdapter.ClickAnAlbum,
    DetailCollectionFragment.ClickASongInDetail {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val onlinePlaylistViewModel: OnlinePlaylistViewModel by viewModels()
    private val onlineArtistViewModel: OnlineArtistViewModel by viewModels()
    private val onlineGenreViewModel: OnlineGenreViewModel by viewModels()
    private val onlineAlbumViewModel: OnlineAlbumViewModel by viewModels()

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        val imageList = arrayListOf(
            SlideModel(R.drawable.poster_06, ""),
            SlideModel(R.drawable.ncs_slide, ""),
            SlideModel(R.drawable.poster_07, ""),
            SlideModel(R.drawable.alan_walker_slide, ""),
            SlideModel(R.drawable.poster_08, "")
        )
        binding.sliderImg.setImageList(imageList, ScaleTypes.FIT)

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

}