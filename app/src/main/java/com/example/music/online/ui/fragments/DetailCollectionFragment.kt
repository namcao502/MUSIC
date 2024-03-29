package com.example.music.online.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.music.R
import com.example.music.databinding.FragmentDetailCollectionBinding
import com.example.music.online.data.models.OnlinePlaylist
import com.example.music.online.data.models.OnlineSong
import com.example.music.online.ui.activities.OnlineMainActivity
import com.example.music.online.ui.adapters.DetailCollectionAdapter
import com.example.music.online.ui.adapters.OnlineDialogPlaylistAdapter
import com.example.music.online.viewModels.FirebaseViewModel
import com.example.music.online.viewModels.OnlineArtistViewModel
import com.example.music.online.viewModels.OnlinePlaylistViewModel
import com.example.music.online.viewModels.OnlineSongViewModel
import com.example.music.utils.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailCollectionFragment(
    val name: String,
    val songs: List<String>,
    val imgFilePath: String,
    private val clickASongInDetail: ClickASongInDetail
): Fragment(), DetailCollectionAdapter.ClickASong, OnlineDialogPlaylistAdapter.ItemClickListener {

    private var _binding: FragmentDetailCollectionBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val firebaseViewModel: FirebaseViewModel by viewModels()

    private val artistViewModel: OnlineArtistViewModel by viewModels()

    private val playlistViewModel: OnlinePlaylistViewModel by viewModels()

    private val songViewModel: OnlineSongViewModel by viewModels()

    private val detailCollectionAdapter: DetailCollectionAdapter by lazy {
        DetailCollectionAdapter(requireContext(), this, artistViewModel, viewLifecycleOwner)
    }

    private val onlineDialogPlaylistAdapter: OnlineDialogPlaylistAdapter by lazy {
        OnlineDialogPlaylistAdapter(requireContext(), this)
    }

    private lateinit var currentSong: OnlineSong

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDetailCollectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as OnlineMainActivity).window.navigationBarColor = Color.parseColor("#5F0A87")
        (activity as OnlineMainActivity).window.statusBarColor = Color.parseColor("#A4508B")

        DetailFragmentState.isOn = true
        DetailFragmentState.instance = this

        binding.addImg.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage("Do you want to add this playlist to your collection?")
                .setTitle("")
                .setPositiveButton("Yes") { _, _ ->
                    val currentUser = Firebase.auth.currentUser!!
                    val playlist = OnlinePlaylist("", name, songs, imgFilePath)
                    playlistViewModel.addPlaylistForUser(playlist, currentUser)
                    playlistViewModel.addPlaylist.observe(viewLifecycleOwner){
                        when(it){
                            is UiState.Loading -> {

                            }
                            is UiState.Failure -> {

                            }
                            is UiState.Success -> {
                                toast(it.data)
                            }
                        }
                    }
                }
                .setNegativeButton("No") { _, _ ->

                }
            // Create the AlertDialog object and return it
            builder.create().show()

        }

        binding.backImg.setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_out, android.R.anim.fade_out, 0, 0)
                .remove(this)
                .commit()
//            parentFragmentManager.popBackStack()
            (activity as OnlineMainActivity).setStatusColor(false)
            DetailFragmentState.isOn = false
        }

        binding.nameTv.text = name

        if (imgFilePath != ""){
            Glide.with(requireContext()).load(imgFilePath).into(binding.imgImg)
        }
        else {
            binding.imgImg.setImageResource(R.drawable.ic_baseline_album_24)
        }

        with(binding.listSongRv){
            adapter = detailCollectionAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        firebaseViewModel.getSongFromListSongID(songs)
        firebaseViewModel.songFromID.observe(viewLifecycleOwner){
            when(it){
                is UiState.Loading -> {

                }
                is UiState.Failure -> {

                }
                is UiState.Success -> {
                    detailCollectionAdapter.setData(it.data)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    interface ClickASongInDetail{
        fun callBackFromClickASongInDetail(songList: List<OnlineSong>, position: Int)
    }

    override fun callBackFromDetailClick(songList: List<OnlineSong>, position: Int) {
        clickASongInDetail.callBackFromClickASongInDetail(songList, position)
    }

    override fun callBackFromMenuDetailClick(action: String, songList: List<OnlineSong>, position: Int) {
        if (action == "Play"){
            clickASongInDetail.callBackFromClickASongInDetail(songList, position)
        }
        if (action == "Add to playlist"){
            currentSong = songList[position]
            createDialogForAddToPlaylist()
        }
        if (action == "Delete"){
            if (arguments?.getSerializable(FireStoreCollection.PLAYLIST) != null){
                val playlist = arguments?.getSerializable(FireStoreCollection.PLAYLIST) as OnlinePlaylist
                deleteSongInPlaylistForUser(songList[position], playlist, Firebase.auth.currentUser!!)
            }
            else {
                toast("Ehe, you can't delete this")
            }
        }
    }

    private fun createDialogForAddToPlaylist() {
        val dialog = createDialog(R.layout.playlist_dialog)

        val recyclerView = dialog.findViewById<RecyclerView>(R.id.playlist_recyclerView)
        recyclerView.adapter = onlineDialogPlaylistAdapter
        recyclerView.layoutManager = LinearLayoutManager(dialog.context)

        FirebaseAuth.getInstance().currentUser?.let {
            playlistViewModel.getAllPlaylistOfUser(it)
        }

        playlistViewModel.playlist.observe(viewLifecycleOwner){
            when(it){
                is UiState.Loading -> {

                }
                is UiState.Failure -> {

                }
                is UiState.Success -> {
                    onlineDialogPlaylistAdapter.setData(it.data)
                }
            }
        }

        val addBtn = dialog.findViewById<FloatingActionButton>(R.id.add_btn)

        addBtn.setOnClickListener {
            createDialogForAddPlaylist(playlistViewModel)
        }

        dialog.show()
    }

    override fun onMenuClick(action: String, playlist: OnlinePlaylist) {
        if (action == "Rename"){
            createDialogForRenamePlaylist(playlist, playlistViewModel)
        }
        if (action == "Delete"){
            createDialogForDeletePlaylist(playlist, playlistViewModel)
        }
    }

    override fun onItemPlaylistClick(playlist: OnlinePlaylist) {
        //add song to selected playlist
        val songSelected = currentSong

        FirebaseAuth.getInstance().currentUser?.let {
            songViewModel.addSongToPlaylist(songSelected, playlist, it)
        }

        songViewModel.addSongInPlaylist.observe(viewLifecycleOwner) {
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

    private fun deleteSongInPlaylistForUser(song: OnlineSong, playlist: OnlinePlaylist, user: FirebaseUser){

        val builder = AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
        builder.setMessage("Delete ${song.name} in ${playlist.name}?")
            .setTitle("Confirm delete")
            .setPositiveButton("Delete") { _, _ ->

                playlistViewModel.deleteSongInPlaylist(song, playlist, user)
                playlistViewModel.deleteSongInPlaylist.observe(viewLifecycleOwner){ result ->
                    when (result) {
                        is UiState.Loading -> {

                        }
                        is UiState.Failure -> {

                        }
                        is UiState.Success -> {
                            toast(result.data)
                            firebaseViewModel.getSongFromListSongID(songs)
                            firebaseViewModel.songFromID.observe(viewLifecycleOwner){
                                when(it){
                                    is UiState.Loading -> {

                                    }
                                    is UiState.Failure -> {

                                    }
                                    is UiState.Success -> {
                                        detailCollectionAdapter.setData(it.data)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                // User cancelled the dialog
            }
        // Create the AlertDialog object and return it
        builder.create().show()
    }
}