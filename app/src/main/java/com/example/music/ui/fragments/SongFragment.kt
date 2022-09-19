package com.example.music.ui.fragments

import android.Manifest
import android.app.Dialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.music.R
import com.example.music.databinding.FragmentSongBinding
import com.example.music.models.Playlist
import com.example.music.models.Song
import com.example.music.models.SongPlaylistCrossRef
import com.example.music.ui.adapters.DialogPlaylistAdapter
import com.example.music.ui.adapters.SongAdapter
import com.example.music.viewModels.PlaylistViewModel
import com.example.music.viewModels.ScanSongInStorage
import com.example.music.viewModels.SongInPlaylistViewModel
import com.example.music.viewModels.SongViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SongFragment(private val songFromAdapterClick: SongFromAdapterClick)
    : Fragment(), SongAdapter.ItemSongClickListener, DialogPlaylistAdapter.ItemClickListener {

    private val songViewModel: SongViewModel by viewModels()
    private val playlistViewModel: PlaylistViewModel by viewModels()
    private val songInPlaylistViewModel: SongInPlaylistViewModel by viewModels()
    private val songAdapter: SongAdapter by lazy { SongAdapter(requireContext(), this) }
    private val dialogPlaylistAdapter: DialogPlaylistAdapter by lazy { DialogPlaylistAdapter(requireContext(), this, viewLifecycleOwner, songInPlaylistViewModel) }

    private var _binding: FragmentSongBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val permission = 502

    private lateinit var currentSong: Song

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSongBinding.inflate(layoutInflater, container, false)

        val menuHost: MenuHost = requireActivity()
        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.main_song_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.scan_menu -> {
                        // clearCompletedTasks()
                        requestRead()
                        Toast.makeText(requireContext(), "Scan completed", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

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

    //popup menu when click on a menu song
    override fun callBackFromMenuSongClick(action: String, songList: List<Song>, position: Int) {
        if (action == "Play"){
            songFromAdapterClick.callBackFromSongFragment(songList, position)
        }
        if (action == "Add to playlist"){
            createDialogForAddToPlaylist()
            currentSong = songList[position]
        }
        if (action == "Delete"){
            createDialogForDeleteSong(songList[position])
        }
    }

    override fun callBackFromSongClick(songList: List<Song>, position: Int) {
        songFromAdapterClick.callBackFromSongFragment(songList, position)
    }

    override fun onItemPlaylistClick(playlist: Playlist) {
        //add song to selected playlist
        val songSelected = currentSong

        val songPlaylistCrossRef = SongPlaylistCrossRef(songSelected.song_id, playlist.playlist_id)
        if (songPlaylistCrossRef != null) {
            songInPlaylistViewModel.addSongPlaylistCrossRef(songPlaylistCrossRef)
        }

        Toast.makeText(requireContext(), "Song ${songSelected.name} added to ${playlist.name} playlist", Toast.LENGTH_SHORT).show()
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
            createDialogForAddPlaylist()
            dialog.cancel()
        }
        dialog.show()

        if (dialog.isShowing){
            addBtn.visibility = View.VISIBLE
        }
        else {
            addBtn.visibility = View.GONE
        }
    }

    private fun createDialogForDeleteSong(song: Song){

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

    //pop up menu when click on playlist
    override fun onMenuClick(action: String, playlist: Playlist) {
        if (action == "Rename"){
            createDialogForRenamePlaylist(playlist)
        }
        if (action == "Delete"){
            createDialogForDeletePlaylist(playlist)
        }
    }

    private fun createDialogForAddPlaylist(){

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

    private fun createDialogForRenamePlaylist(playlist: Playlist){

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
                        val updatedPlaylist = Playlist(playlist.playlist_id, title)
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

    private fun createDialogForDeletePlaylist(playlist: Playlist){

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

    fun requestRead() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                permission)
        } else {
            readFile()
        }
    }

    private fun readFile(){
        val context = requireContext()
        val listSong = ScanSongInStorage(context).getAllSongs()
        songViewModel.deleteAllSongs()
        for (song in listSong){
            songViewModel.addSong(song)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == permission) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readFile()
            } else {
                // Permission Denied
                Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    interface SongFromAdapterClick{
        fun callBackFromSongFragment(songs: List<Song>, position: Int)
    }

}