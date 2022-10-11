package com.example.music.online.ui.fragments

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
import com.example.music.R
import com.example.music.databinding.FragmentOnlineSongBinding
import com.example.music.online.data.models.OnlinePlaylist
import com.example.music.online.data.models.OnlineSong
import com.example.music.online.ui.adapters.OnlineDialogPlaylistAdapter
import com.example.music.online.ui.adapters.OnlineSongAdapter
import com.example.music.online.viewModels.OnlineArtistViewModel
import com.example.music.online.viewModels.OnlinePlaylistViewModel
import com.example.music.online.viewModels.OnlineSongViewModel
import com.example.music.utils.UiState
import com.example.music.utils.createDialog
import com.example.music.utils.toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
        OnlineSongAdapter(requireContext(), viewLifecycleOwner, onlineArtistViewModel,this)
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

        binding.searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String): Boolean {
                TODO("Not yet implemented")
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterSong(newText)
                return false
            }

        })

        binding.songRecyclerView.apply {
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
                    initialList = it.data
                }
            }
        }
    }

    private fun filterSong(text: String) {
        //creating a new array list to filter our data.
        val filter: ArrayList<OnlineSong> = ArrayList<OnlineSong>()

        // running a for loop to compare elements.
        for (item in onlineSongAdapter.songList) {
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
            onlineSongAdapter.setData(initialList!!)
        } else {
            // at last we are passing that filtered
            // list to our adapter class.
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
            createDialogForAddToPlaylist()
            currentSong = songList[position]
        }
        if (action == "Delete"){
//            createDialogForDeleteSong(songList[position])
            toast("Just for fun, you can't delete :>")
        }
    }

    private fun createDialogForAddToPlaylist() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.fragment_online_playlist)

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

        FirebaseAuth.getInstance().currentUser?.let {
            onlinePlaylistViewModel.getAllPlaylistOfUser(it)
        }
        onlinePlaylistViewModel.playlist.observe(viewLifecycleOwner){
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

    override fun callBackFromSongClick(songList: List<OnlineSong>, position: Int) {
        songFromAdapterClick.callBackFromSongFragment(songList, position)
    }

    interface SongFromAdapterClick{
        fun callBackFromSongFragment(songs: List<OnlineSong>, position: Int)
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
            onlineSongViewModel.addSongToPlaylist(songSelected, playlist, it)
        }

        onlineSongViewModel.addSongInPlaylist.observe(viewLifecycleOwner) {
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
                        onlinePlaylistViewModel.updatePlaylistForUser(playlist, it)
                    }
                    onlinePlaylistViewModel.updatePlaylist.observe(viewLifecycleOwner) {
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
                    onlinePlaylistViewModel.deletePlaylistForUser(playlist, it)
                }
                onlinePlaylistViewModel.deletePlaylist.observe(viewLifecycleOwner) {
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
                        onlinePlaylistViewModel.addPlaylistForUser(playlist, it)
                    }
                    onlinePlaylistViewModel.addPlaylist.observe(viewLifecycleOwner) {
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

}