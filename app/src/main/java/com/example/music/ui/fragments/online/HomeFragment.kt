package com.example.music.ui.fragments.online

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.music.R
import com.example.music.UiState
import com.example.music.databinding.FragmentHomeBinding
import com.example.music.databinding.FragmentWelcomeBinding
import com.example.music.ui.adapters.OnlineAlbumAdapter
import com.example.music.ui.adapters.OnlineArtistAdapter
import com.example.music.ui.adapters.OnlineGenreAdapter
import com.example.music.ui.adapters.OnlinePlaylistInHomeAdapter
import com.example.music.viewModels.online.OnlineAlbumViewModel
import com.example.music.viewModels.online.OnlineArtistViewModel
import com.example.music.viewModels.online.OnlineGenreViewModel
import com.example.music.viewModels.online.OnlinePlaylistViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding

    private val onlinePlaylistViewModel: OnlinePlaylistViewModel by viewModels()
    private val onlineArtistViewModel: OnlineArtistViewModel by viewModels()
    private val onlineGenreViewModel: OnlineGenreViewModel by viewModels()
    private val onlineAlbumViewModel: OnlineAlbumViewModel by viewModels()

    private val onlinePlaylistInHomeAdapter: OnlinePlaylistInHomeAdapter by lazy {
        OnlinePlaylistInHomeAdapter(requireContext())
    }

    private val onlineArtistAdapter: OnlineArtistAdapter by lazy {
        OnlineArtistAdapter(requireContext())
    }

    private val onlineGenreAdapter: OnlineGenreAdapter by lazy {
        OnlineGenreAdapter(requireContext())
    }

    private val onlineAlbumAdapter: OnlineAlbumAdapter by lazy {
        OnlineAlbumAdapter(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //load data for playlist
        with(binding!!.playlistRv){
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
        with(binding!!.artistRv){
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
        with(binding!!.genreRv){
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
        with(binding!!.albumRv){
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
            SlideModel(R.drawable.ncs_slide, ""),
            SlideModel(R.drawable.alan_walker_slide, ""),
            SlideModel(R.drawable.bolero_slide, "")
        )
        binding!!.sliderImg.setImageList(imageList, ScaleTypes.FIT)

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}