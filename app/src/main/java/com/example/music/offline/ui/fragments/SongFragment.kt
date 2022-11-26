package com.example.music.offline.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.music.R
import com.example.music.databinding.FragmentSongBinding
import com.example.music.offline.data.models.Playlist
import com.example.music.offline.data.models.Song
import com.example.music.offline.data.models.SongPlaylistCrossRef
import com.example.music.offline.ui.adapters.DialogPlaylistAdapter
import com.example.music.offline.ui.adapters.SongAdapter
import com.example.music.offline.viewModels.PlaylistViewModel
import com.example.music.offline.viewModels.ScanSongInStorage
import com.example.music.offline.viewModels.SongInPlaylistViewModel
import com.example.music.offline.viewModels.SongViewModel
import com.example.music.utils.createDialog
import com.example.music.utils.toast
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

        requestRead()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.songRecyclerView.apply {
            adapter = songAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        songViewModel.readAllSongs().observe(viewLifecycleOwner) {
            songAdapter.setData(it)
        }

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

        val dialog = createDialog(R.layout.fragment_playlist)

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
            .setPositiveButton("Delete") { _, _ ->
                songViewModel.deleteSong(song)
            }
            .setNegativeButton("Cancel") { _, _ ->
                // User cancelled the dialog
            }
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
            .setPositiveButton("Create") { _, _ ->

                val title =
                    view.findViewById<EditText>(R.id.title_et_menu_playlist_dialog).text.toString()

                if (title.isEmpty()) {
                    Toast.makeText(requireContext(), "Name can not be empty", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    val playlist = Playlist(0, title)
                    playlistViewModel.addPlaylist(playlist)
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                // User cancelled the dialog
            }
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
            .setPositiveButton("Rename") { _, _ ->

                val title =
                    view.findViewById<EditText>(R.id.title_et_menu_playlist_dialog).text.toString()

                if (title.isEmpty()) {
                    toast("Name can't be empty")
                } else {
                    val updatedPlaylist = Playlist(playlist.playlist_id, title)
                    playlistViewModel.updatePlaylist(updatedPlaylist)
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                // User cancelled the dialog
            }
        // Create the AlertDialog object and return it
        builder.create().show()
    }

    private fun createDialogForDeletePlaylist(playlist: Playlist){

        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Delete ${playlist.name} playlist?")
            .setTitle("")
            .setPositiveButton("Delete") { _, _ ->
                playlistViewModel.deletePlaylist(playlist)
            }
            .setNegativeButton("Cancel") { _, _ ->
                // User cancelled the dialog
            }
        // Create the AlertDialog object and return it

        builder.create().show()
    }

    private fun requestRead() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            startLocationPermissionRequest()
//            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), permission)
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

//    @Deprecated("Deprecated in Java")
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        if (requestCode == permission) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                readFile()
//            } else {
//                // Permission Denied
//                Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
//            }
//            return
//        }
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // PERMISSION GRANTED
            readFile()
        } else {
            // PERMISSION NOT GRANTED
            toast("Permission Denied")
        }
    }

    private fun startLocationPermissionRequest() {
        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    interface SongFromAdapterClick{
        fun callBackFromSongFragment(songs: List<Song>, position: Int)
    }

}