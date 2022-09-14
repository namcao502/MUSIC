package com.example.music.ui.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.music.R
import com.example.music.UiState
import com.example.music.databinding.FragmentOnlinePlaylistBinding
import com.example.music.models.OnlinePlaylist
import com.example.music.models.OnlineSong
import com.example.music.models.Song
import com.example.music.models.SongPlaylistCrossRef
import com.example.music.ui.adapters.OnlinePlaylistAdapter
import com.example.music.ui.adapters.OnlineSongInPlaylistAdapter
import com.example.music.ui.adapters.SongInPlaylistAdapter
import com.example.music.viewModels.FirebaseViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnlinePlaylistFragment(private val songInPlaylistClick: OnlineSongInPlaylistAdapter.ItemSongInPlaylistClickListener)
    : Fragment(),
    OnlinePlaylistAdapter.ItemPlaylistClickListener,
    OnlineSongInPlaylistAdapter.ItemSongInPlaylistClickListener{

    private val firebaseViewModel: FirebaseViewModel by viewModels()

    private val onlinePlaylistAdapter: OnlinePlaylistAdapter by lazy {
        OnlinePlaylistAdapter(requireContext(), this, viewLifecycleOwner, firebaseViewModel)
    }

    private var _binding: FragmentOnlinePlaylistBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding =  FragmentOnlinePlaylistBinding.inflate(layoutInflater, container, false)

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
                    R.id.add_new_playlist_menu -> {
                        // clearCompletedTasks()
                        createDialogForAddPlaylist()
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
            adapter = onlinePlaylistAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        FirebaseAuth.getInstance().currentUser?.let {
            firebaseViewModel.getAllPlaylistOfUser(it)
        }
        firebaseViewModel.playlist.observe(viewLifecycleOwner){
            when(it){
                is UiState.Loading -> {

                }
                is UiState.Failure -> {

                }
                is UiState.Success -> {
                    onlinePlaylistAdapter.setData(it.data)
                }
            }
        }

//        FirebaseAuth.getInstance().currentUser?.let {
//            firebaseViewModel.getAllPlaylistOfUser(it)
//        }
//        firebaseViewModel.playlist.observe(viewLifecycleOwner, Observer {
//            onlinePlaylistAdapter.setData(it)
//        })

//        binding.addBtn.setOnClickListener {
//            createDialogForAddPlaylist()
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
                        val playlist = OnlinePlaylist("", title, emptyList())
                        FirebaseAuth.getInstance().currentUser?.let {
                            firebaseViewModel.addPlaylistForUser(playlist, it)
                        }
                    }
                })
            .setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, id ->
                    // User cancelled the dialog
                })
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
            .setPositiveButton("Rename",
                DialogInterface.OnClickListener { dialog, id ->

                    val title = view.findViewById<EditText>(R.id.title_et_menu_playlist_dialog).text.toString()

                    if (title.isEmpty()){
                        Toast.makeText(requireContext(), "Name can not be empty", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        playlist.name = title
                        FirebaseAuth.getInstance().currentUser?.let {
                            firebaseViewModel.updatePlaylistForUser(playlist, it)
                        }
                    }
                })
            .setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, id ->
                    // User cancelled the dialog
                })
        // Create the AlertDialog object and return it
        builder.create().show()
    }

    private fun createDialogForDeletePlaylist(playlist: OnlinePlaylist){

        val builder = AlertDialog.Builder(requireContext())

        builder.setMessage("Delete ${playlist.name} playlist?")
            .setTitle("")
            .setPositiveButton("Delete",
                DialogInterface.OnClickListener { dialog, id ->
                    FirebaseAuth.getInstance().currentUser?.let {
                        firebaseViewModel.deletePlaylistForUser(playlist, it)
                    }
                })
            .setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, id ->
                    // User cancelled the dialog
                })
        // Create the AlertDialog object and return it
        builder.create().show()
    }

    override fun callBackFromMenuPlaylistClick(action: String, playlist: OnlinePlaylist) {
        if (action == "Rename"){
            createDialogForRenamePlaylist(playlist)
        }
        if (action == "Delete"){
            createDialogForDeletePlaylist(playlist)
        }
    }

    override fun callBackFromSongInPlaylist(songList: List<OnlineSong>, position: Int) {
        songInPlaylistClick.callBackFromSongInPlaylist(songList, position)
    }

    override fun callBackFromMenuSongInPlaylist(action: String, songList: List<OnlineSong>, position: Int, playlist: OnlinePlaylist) {
        if (action == "Play"){
            songInPlaylistClick.callBackFromSongInPlaylist(songList, position)
        }
        if (action == "Delete from playlist"){
            FirebaseAuth.getInstance().currentUser?.let {
                firebaseViewModel.deleteSongInPlaylist(songList[position], playlist, it)
            }
        }
    }
}