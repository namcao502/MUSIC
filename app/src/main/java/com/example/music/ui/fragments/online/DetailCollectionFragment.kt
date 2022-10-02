package com.example.music.ui.fragments.online

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.music.R
import com.example.music.UiState
import com.example.music.data.models.online.OnlinePlaylist
import com.example.music.data.models.online.OnlineSong
import com.example.music.databinding.FragmentDetailCollectionBinding
import com.example.music.ui.adapters.DetailCollectionAdapter
import com.example.music.ui.adapters.OnlineDialogPlaylistAdapter
import com.example.music.utils.FireStoreCollection
import com.example.music.utils.toast
import com.example.music.viewModels.online.FirebaseViewModel
import com.example.music.viewModels.online.OnlineArtistViewModel
import com.example.music.viewModels.online.OnlinePlaylistViewModel
import com.example.music.viewModels.online.OnlineSongViewModel
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
    private val clickASongInDetail: ClickASongInDetail)
    : Fragment(), DetailCollectionAdapter.ClickASong, OnlineDialogPlaylistAdapter.ItemClickListener {

    private var _binding: FragmentDetailCollectionBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val firebaseViewModel: FirebaseViewModel by viewModels()

    private val artistViewModel: OnlineArtistViewModel by viewModels()

    private val playlistViewModel: OnlinePlaylistViewModel by viewModels()

    private val songViewModel: OnlineSongViewModel by viewModels()

    private val detailCollectionAdapter: DetailCollectionAdapter by lazy {
        DetailCollectionAdapter(requireContext(), this, viewLifecycleOwner, artistViewModel)
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

        binding.addImg.setOnClickListener {
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

        binding.backImg.setOnClickListener {
            //back press
            requireActivity().onBackPressed()
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
            createDialogForAddToPlaylist()
            currentSong = songList[position]
        }
        if (action == "Delete"){
//            val playlist = requireActivity().intent.getSerializableExtra(FireStoreCollection.PLAYLIST) as OnlinePlaylist

            if (arguments?.getSerializable(FireStoreCollection.PLAYLIST) != null){
                val playlist = arguments?.getSerializable(FireStoreCollection.PLAYLIST) as OnlinePlaylist
                deleteSongInPlaylistForUser(songList[position], playlist, Firebase.auth.currentUser!!)
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
    }

    private fun createDialogForAddToPlaylist() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.fragment_playlist)

        //set size for dialog
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
        dialog.window!!.attributes = lp

        val recyclerView = dialog.findViewById<RecyclerView>(R.id.playlist_recyclerView)
        recyclerView.adapter = onlineDialogPlaylistAdapter
        recyclerView.layoutManager = LinearLayoutManager(dialog.context)

//        FirebaseAuth.getInstance().currentUser?.let {
//            firebaseViewModel.getAllPlaylistOfUser(it)
//        }
//        firebaseViewModel.playlist.observe(viewLifecycleOwner, Observer {
//            onlineSongInPlaylistAdapter.setData(it)
//        })

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
            createDialogForAddPlaylist()
        }
        dialog.show()
    }

    private fun createDialogForAddPlaylist(){

        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.menu_playlist_dialog, null)

        builder.setMessage("Create")
            .setTitle("")
            .setView(view)
            .setPositiveButton("Create") { _, _ ->

                val title =
                    view.findViewById<EditText>(R.id.title_et_menu_playlist_dialog).text.toString()

                if (title.isEmpty()) {
                    Toast.makeText(requireContext(), "Name can not be empty", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    val playlist = OnlinePlaylist("", title, emptyList())
                    FirebaseAuth.getInstance().currentUser?.let {
                        playlistViewModel.addPlaylistForUser(playlist, it)
                    }
                    playlistViewModel.addPlaylist.observe(viewLifecycleOwner) {
                        when (it) {
                            is UiState.Loading -> {

                            }
                            is UiState.Failure -> {

                            }
                            is UiState.Success -> {
                                Toast.makeText(requireContext(), it.data, Toast.LENGTH_SHORT).show()
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

    private fun createDialogForRenamePlaylist(playlist: OnlinePlaylist){

        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.menu_playlist_dialog, null)

        view.findViewById<EditText>(R.id.title_et_menu_playlist_dialog).setText(playlist.name)

        builder.setMessage("Rename")
            .setTitle("")
            .setView(view)
            .setPositiveButton("Rename") { _, _ ->

                val title =
                    view.findViewById<EditText>(R.id.title_et_menu_playlist_dialog).text.toString()

                if (title.isEmpty()) {
                    Toast.makeText(requireContext(), "Name can not be empty", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    playlist.name = title
                    FirebaseAuth.getInstance().currentUser?.let {
                        playlistViewModel.updatePlaylistForUser(playlist, it)
                    }
                    playlistViewModel.updatePlaylist.observe(viewLifecycleOwner) {
                        when (it) {
                            is UiState.Loading -> {

                            }
                            is UiState.Failure -> {

                            }
                            is UiState.Success -> {
                                Toast.makeText(requireContext(), it.data, Toast.LENGTH_SHORT)
                                    .show()
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

    private fun createDialogForDeletePlaylist(playlist: OnlinePlaylist){

        val builder = AlertDialog.Builder(requireContext())

        builder.setMessage("Delete ${playlist.name} playlist?")
            .setTitle("")
            .setPositiveButton("Delete") { _, _ ->

                FirebaseAuth.getInstance().currentUser?.let {
                    playlistViewModel.deletePlaylistForUser(playlist, it)
                }
                playlistViewModel.deletePlaylist.observe(viewLifecycleOwner) {
                    when (it) {
                        is UiState.Loading -> {

                        }
                        is UiState.Failure -> {

                        }
                        is UiState.Success -> {
                            Toast.makeText(requireContext(), it.data, Toast.LENGTH_SHORT).show()
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

    override fun onMenuClick(action: String, playlist: OnlinePlaylist) {
        if (action == "Rename"){
            createDialogForRenamePlaylist(playlist)
        }
        if (action == "Delete"){
            createDialogForDeletePlaylist(playlist)
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
                    Toast.makeText(
                        requireContext(),
                        "Song ${songSelected.name} added to ${playlist.name} playlist",
                        Toast.LENGTH_SHORT
                    ).show()
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