package com.example.music.online.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.music.databinding.FragmentOnlineSongBinding
import com.example.music.online.data.models.OnlinePlaylist
import com.example.music.online.data.models.OnlineSong
import com.example.music.online.ui.adapters.OnlineDialogPlaylistAdapter
import com.example.music.online.ui.adapters.OnlineSongAdapter
import com.example.music.online.viewModels.OnlineArtistViewModel
import com.example.music.online.viewModels.OnlinePlaylistViewModel
import com.example.music.online.viewModels.OnlineSongViewModel
import com.example.music.utils.*
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class OnlineSongFragment(private val songFromAdapterClick: SongFromAdapterClick)
    : Fragment(),
    OnlineSongAdapter.ItemSongClickListener,
    OnlineDialogPlaylistAdapter.ItemClickListener{

    private var _binding: FragmentOnlineSongBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val onlineSongViewModel: OnlineSongViewModel by viewModels()
    private val onlinePlaylistViewModel: OnlinePlaylistViewModel by viewModels()
    private val onlineArtistViewModel: OnlineArtistViewModel by viewModels()

    private val onlineSongAdapter: OnlineSongAdapter by lazy {
        OnlineSongAdapter(requireContext(), this, viewLifecycleOwner, onlineArtistViewModel)
    }

    private val onlineDialogPlaylistAdapter: OnlineDialogPlaylistAdapter by lazy {
        OnlineDialogPlaylistAdapter(requireContext(), this)
    }

    private lateinit var currentSong: OnlineSong

    private var initialList: List<OnlineSong>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentOnlineSongBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String): Boolean {
                val trimText = query.trim()
                filterSong(trimText)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                val trimText = newText.trim()
                filterSong(trimText)
                return false
            }

        })

        binding.songRecyclerView.apply {
            adapter = onlineSongAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        //get all songs
        onlineSongViewModel.getAllSongs()
        onlineSongViewModel.song.observe(viewLifecycleOwner){
            when(it){
                is UiState.Loading -> {

                }
                is UiState.Failure -> {

                }
                is UiState.Success -> {
                    onlineSongAdapter.setData(it.data)
                    initialList = it.data
                }
            }
        }
    }

    private fun filterSong(text: String) {
        val filter: ArrayList<OnlineSong> = ArrayList()
        for (item in onlineSongAdapter.songList) {
            if (item.name!!.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                filter.add(item)
            }
        }
        if (filter.isEmpty()) {
            toast("Not found")
        }
        if (text.isEmpty()){
            onlineSongAdapter.setData(initialList!!)
        }
        else {
            onlineSongAdapter.setData(filter)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun callBackFromMenuSongClick(action: String, songList: List<OnlineSong>, position: Int) {
        if (action == "Play"){
            songFromAdapterClick.callBackFromSongFragment(songList, position)
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
        songFromAdapterClick.callBackFromSongFragment(songList, position)
    }

    interface SongFromAdapterClick{
        fun callBackFromSongFragment(songs: List<OnlineSong>, position: Int)
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