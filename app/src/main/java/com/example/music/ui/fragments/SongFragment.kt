package com.example.music.ui.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.music.R
import com.example.music.databinding.FragmentSongBinding
import com.example.music.models.Playlist
import com.example.music.models.Song
import com.example.music.viewModels.PlaylistViewModel
import com.example.music.viewModels.SongViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SongFragment : Fragment(), SongAdapter.ItemSongClickListener, DialogPlaylistAdapter.ItemClickListener {

    private val songViewModel: SongViewModel by viewModels()
    private val playlistViewModel: PlaylistViewModel by viewModels()
    private val songAdapter: SongAdapter by lazy { SongAdapter(requireContext(), this) }
    private val dialogPlaylistAdapter: DialogPlaylistAdapter by lazy { DialogPlaylistAdapter(requireContext(), this) }

    private var _binding: FragmentSongBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var currentSong: Song

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSongBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.songRecyclerView.apply {
            adapter = songAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        songViewModel.readAllSongs().observe(viewLifecycleOwner, Observer {
            songAdapter.setData(it)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(action: String, song: Song) {
        if (action == "Play"){
        }
        if (action == "Add to playlist"){
            createDialogForAddToPlaylist()
            currentSong = song
        }
        if (action == "Delete"){
            createDialogForDelete(song)
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
        recyclerView.adapter = dialogPlaylistAdapter
        recyclerView.layoutManager = LinearLayoutManager(dialog.context)

        playlistViewModel.readAllPlaylists().observe(viewLifecycleOwner, Observer {
            dialogPlaylistAdapter.setData(it)
        })

        val addBtn = dialog.findViewById<FloatingActionButton>(R.id.add_btn)

        addBtn.setOnClickListener {
            createDialogForAdd()
        }

        dialog.show()
    }

    private fun createDialogForDelete(song: Song){

        val builder = AlertDialog.Builder(requireContext())

        builder.setMessage("Delete ${song.name}?")
            .setTitle("")
            .setPositiveButton("Delete",
                DialogInterface.OnClickListener { dialog, id ->
                    songViewModel.deleteSong(song)
                })
            .setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, id ->
                    // User cancelled the dialog
                })
        // Create the AlertDialog object and return it
        builder.create().show()
    }

    override fun onMenuClick(action: String, playlist: Playlist) {
        if (action == "Rename"){
            createDialogForRename(playlist)
        }
        if (action == "Delete"){
            createDialogForDelete(playlist)
        }
    }

    override fun onItemPlaylistClick(playlist: Playlist) {
        //add song to selected playlist
        val song = currentSong
        song.playlistID = playlist.id
        songViewModel.updateSong(song)
        Toast.makeText(requireContext(), "Song ${song.name} added to ${playlist.name} playlist", Toast.LENGTH_SHORT).show()
    }

    private fun createDialogForAdd(){

        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.menu_playlist_dialog, null)

        builder.setMessage("Create")
            .setTitle("")
            .setView(view)
            .setPositiveButton("Create",
                DialogInterface.OnClickListener { dialog, id ->

                    val title = view.findViewById<EditText>(R.id.title_et_menu_playlist_dialog).text.toString()

                    if (title.isEmpty()){
                        Toast.makeText(requireContext(), "Name can not be empty", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        val playlist = Playlist(0, title)
                        playlistViewModel.addPlaylist(playlist)
                    }
                })
            .setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, id ->
                    // User cancelled the dialog
                })
        // Create the AlertDialog object and return it
        builder.create().show()
    }

    private fun createDialogForRename(playlist: Playlist){

        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.menu_playlist_dialog, null)

        view.findViewById<EditText>(R.id.title_et_menu_playlist_dialog).setText(playlist.name)

        builder.setMessage("Rename")
            .setTitle("")
            .setView(view)
            .setPositiveButton("Rename",
                DialogInterface.OnClickListener { dialog, id ->

                    val title = view.findViewById<EditText>(R.id.title_et_menu_playlist_dialog).text.toString()

                    if (title.isEmpty()){
                        Toast.makeText(requireContext(), "Name can not be empty", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        val updatedPlaylist = Playlist(playlist.id, title)
                        playlistViewModel.updatePlaylist(updatedPlaylist)
                    }
                })
            .setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, id ->
                    // User cancelled the dialog
                })
        // Create the AlertDialog object and return it
        builder.create().show()
    }

    private fun createDialogForDelete(playlist: Playlist){

        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Delete ${playlist.name} playlist?")
            .setTitle("")
            .setPositiveButton("Delete",
                DialogInterface.OnClickListener { dialog, id ->
                    playlistViewModel.deletePlaylist(playlist)
                })
            .setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, id ->
                    // User cancelled the dialog
                })
        // Create the AlertDialog object and return it

        builder.create().show()
    }


}