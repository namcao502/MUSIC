package com.example.music.online.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.music.R
import com.example.music.databinding.FragmentDetailCollectionBinding
import com.example.music.online.data.models.OnlineArtist
import com.example.music.online.data.models.OnlinePlaylist
import com.example.music.online.data.models.OnlineSong
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

        DetailFragmentState.isOn = true

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
            //back press
            requireActivity().onBackPressed()
            DetailFragmentState.isOn = false
        }

        binding.nameTv.text = name

        if (imgFilePath.isNotEmpty()){
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

                    //with each song, get its artists
//                    val artistList: ArrayList<String> = ArrayList()
//                    for (i in 0 until it.data.size){
//                        artistViewModel.getAllArtistFromSong2(it.data[i], i)
//                        artistViewModel.artistInSong2[i].observe(viewLifecycleOwner){ artists ->
//                            when(artists){
//                                is UiState.Loading -> {
//
//                                }
//                                is UiState.Failure -> {
//
//                                }
//                                is UiState.Success -> {
//                                    Log.i("TAG502", "onViewCreated - $i: ${it.data}")
//                                    var text = ""
//                                    for (x in artists.data){
//                                        text += x.name.plus(", ")
//                                    }
//                                    artistList.add(text.dropLast(2))
//                                    Log.i("TAG502", "onViewCreated - $i: $artistList")
////                                    Log.i("TAG502", "before - $i: $artistList")
//                                    detailCollectionAdapter.setDataForArtist(artistList)
////                                    Log.i("TAG502", "after - $i: $artistList")
//                                }
//                            }
//                        }
//                    }
                    detailCollectionAdapter.setData(it.data)
//                    Log.i("TAG502", "before set: $artistList")
//                    detailCollectionAdapter.setDataForArtist(artistList)
//                    Log.i("TAG502", "after set: $artistList")
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
            createDialogForAddToPlaylist()
            currentSong = songList[position]
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
        val dialog = createDialog(R.layout.fragment_online_playlist)

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
        playlistViewModel.deleteSongInPlaylist(song, playlist, user)
        playlistViewModel.deleteSongInPlaylist.observe(viewLifecycleOwner){
            when (it) {
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

}