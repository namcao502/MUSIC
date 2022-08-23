package com.example.music.ui.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.EditText
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.music.R
import com.example.music.databinding.FragmentPlaylistBinding
import com.example.music.models.Playlist
import com.example.music.viewModels.PlaylistViewModel
import com.example.music.viewModels.SongInPlaylistViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlaylistFragment : Fragment(), PlaylistAdapter.ItemPlaylistClickListener{

    private val playlistViewModel: PlaylistViewModel by viewModels()
    private val songInPlaylistViewModel: SongInPlaylistViewModel by viewModels()
    private val playlistAdapter: PlaylistAdapter by lazy { PlaylistAdapter(requireContext(), this) }
    private val songInPlaylistAdapter: SongInPlaylistAdapter by lazy { SongInPlaylistAdapter(requireContext()) }

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

        val menuHost: MenuHost = requireActivity()
        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.main_playlist_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.sample_menu -> {
                        // clearCompletedTasks()
                        Toast.makeText(requireContext(), "From playlist", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

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

        binding.addBtn.setOnClickListener {
            createDialogForAddPlaylist()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(action: String, playlist: Playlist) {
        if (action == "Rename"){
            createDialogForRenamePlaylist(playlist)
        }
        if (action == "Delete"){
            createDialogForDeletePlaylist(playlist)
        }
    }

    override fun onPlaylistToSongClick(playlist: Playlist) {
        //change adapter and load
        binding.playlistRecyclerView.apply {
            adapter = songInPlaylistAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.addBtn.visibility = View.GONE

        songInPlaylistViewModel.getSongsOfPlaylist(playlist.playlist_id).observe(viewLifecycleOwner, Observer {
            songInPlaylistAdapter.setData(it.listSong)
        })

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            binding.addBtn.visibility = View.VISIBLE
            // Handle the back button event
            binding.playlistRecyclerView.apply {
                adapter = playlistAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }

            playlistViewModel.readAllPlaylists().observe(viewLifecycleOwner, Observer {
                playlistAdapter.setData(it)
            })

            binding.addBtn.setOnClickListener {
                createDialogForAddPlaylist()
            }
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

}