package com.example.music.ui.fragments.online

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.music.R
import com.example.music.UiState
import com.example.music.data.models.online.OnlinePlaylist
import com.example.music.databinding.FragmentHomeBinding
import com.example.music.ui.adapters.*
import com.example.music.utils.createBottomSheetDialog
import com.example.music.utils.toast
import com.example.music.viewModels.online.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@AndroidEntryPoint
class HomeFragment: Fragment(), OnlinePlaylistInHomeAdapter.ClickAPlaylist {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding

    private val onlinePlaylistViewModel: OnlinePlaylistViewModel by viewModels()
    private val onlineArtistViewModel: OnlineArtistViewModel by viewModels()
    private val onlineGenreViewModel: OnlineGenreViewModel by viewModels()
    private val onlineAlbumViewModel: OnlineAlbumViewModel by viewModels()
    private val firebaseViewModel: FirebaseViewModel by viewModels()

    private val onlinePlaylistInHomeAdapter: OnlinePlaylistInHomeAdapter by lazy {
        OnlinePlaylistInHomeAdapter(requireContext(), this)
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

    private val onlineListSongAdapter: OnlineListSongAdapter by lazy {
        OnlineListSongAdapter(requireContext())
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

        binding!!.changeFragmentBtn.setOnClickListener {

//            val transaction = requireActivity().supportFragmentManager.beginTransaction()
//            val transaction = this.childFragmentManager.beginTransaction()
//            transaction.add(R.id.viewPagerMainOnline, BlankFragment())
//            transaction.disallowAddToBackStack()
//            transaction.commit()

            val bottomSheetDialog = createBottomSheetDialog()
            val listSongRV = bottomSheetDialog.findViewById<RecyclerView>(R.id.list_song_rv)

            with(listSongRV!!){
                adapter = onlineListSongAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }

            //set state for dialog
            bottomSheetDialog.show()

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun showBSD(listSong: List<String>){

        val bottomSheetDialog = createBottomSheetDialog()
        val listSongRV = bottomSheetDialog.findViewById<RecyclerView>(R.id.list_song_rv)

        with(listSongRV!!){
            adapter = onlineListSongAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        firebaseViewModel.getAllSongFromListSongID(listSong)
        when(firebaseViewModel.allSongFromID){
            is UiState.Loading -> {

            }
            is UiState.Failure -> {

            }
            is UiState.Success -> {

            }
        }

        //set state for dialog
        bottomSheetDialog.show()
    }

    override fun callBackFromPlaylistClick(playlist: OnlinePlaylist) {
        showBSD(playlist.songs!!)
    }

}