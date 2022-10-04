package com.example.music.offline.ui.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.music.R
import com.example.music.databinding.FragmentPlaylistBinding
import com.example.music.offline.data.models.Playlist
import com.example.music.offline.data.models.Song
import com.example.music.offline.data.models.SongPlaylistCrossRef
import com.example.music.offline.ui.adapters.PlaylistAdapter
import com.example.music.offline.ui.adapters.SongInPlaylistAdapter
import com.example.music.offline.viewModels.PlaylistViewModel
import com.example.music.offline.viewModels.SongInPlaylistViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlaylistFragment(private val songInPlaylistClick: SongInPlaylistAdapter.ItemSongInPlaylistClickListener) :
    Fragment(),
    PlaylistAdapter.ItemPlaylistClickListener,
    SongInPlaylistAdapter.ItemSongInPlaylistClickListener{

    private val playlistViewModel: PlaylistViewModel by viewModels()
    private val songInPlaylistViewModel: SongInPlaylistViewModel by viewModels()

    private val playlistAdapter: PlaylistAdapter by lazy {
        PlaylistAdapter(requireContext(),
            this, viewLifecycleOwner, songInPlaylistViewModel)
    }

    private var _binding: FragmentPlaylistBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding =  FragmentPlaylistBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.playlistRecyclerView.apply {
            adapter = playlistAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        playlistViewModel.readAllPlaylists().observe(viewLifecycleOwner, Observer {
            playlistAdapter.setData(it)
        })

//        binding.addBtn.setOnClickListener {
//            createDialogForAddPlaylist()
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun callBackFromMenuPlaylistClick(action: String, playlist: Playlist) {
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

    override fun callBackFromSongInPlaylist(songList: List<Song>, position: Int) {
        songInPlaylistClick.callBackFromSongInPlaylist(songList, position)
    }

    override fun callBackFromMenuSongInPlaylist(action: String, songList: List<Song>, position: Int, playlist: Playlist) {
        if (action == "Play"){
            songInPlaylistClick.callBackFromSongInPlaylist(songList, position)
        }
        if (action == "Delete from playlist"){
            val songInPlaylistCrossRef = SongPlaylistCrossRef(songList[position].song_id, playlist.playlist_id)
            songInPlaylistCrossRef.let {
                songInPlaylistViewModel.deleteSongPlaylistCrossRef(it)
            }
        }
    }

}