package com.example.music.ui.fragments

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
import com.example.music.R
import com.example.music.UiState
import com.example.music.databinding.FragmentOnlinePlaylistBinding
import com.example.music.data.models.online.OnlinePlaylist
import com.example.music.data.models.online.OnlineSong
import com.example.music.ui.adapters.OnlinePlaylistAdapter
import com.example.music.ui.adapters.OnlineSongInPlaylistAdapter
import com.example.music.viewModels.online.FirebaseViewModel
import com.example.music.viewModels.online.OnlinePlaylistViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnlinePlaylistFragment(private val songInPlaylistClick: OnlineSongInPlaylistAdapter.ItemSongInPlaylistClickListener)
    : Fragment(),
    OnlinePlaylistAdapter.ItemPlaylistClickListener,
    OnlineSongInPlaylistAdapter.ItemSongInPlaylistClickListener{

    private val onlinePlaylistViewModel: OnlinePlaylistViewModel by viewModels()

    private val onlinePlaylistAdapter: OnlinePlaylistAdapter by lazy {
        OnlinePlaylistAdapter(requireContext(), this, viewLifecycleOwner, onlinePlaylistViewModel)
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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.playlistRecyclerView.apply {
            adapter = onlinePlaylistAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

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
                    onlinePlaylistAdapter.setData(it.data)
                }
            }
        }

        binding.addBtn.setOnClickListener {
            createDialogForAddPlaylist()
        }
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
                            onlinePlaylistViewModel.addPlaylistForUser(playlist, it)
                        }
                        onlinePlaylistViewModel.addPlaylist.observe(viewLifecycleOwner, Observer {
                            when (it) {
                                is UiState.Loading -> {

                                }
                                is UiState.Failure -> {

                                }
                                is UiState.Success -> {
                                    Toast.makeText(requireContext(), it.data, Toast.LENGTH_SHORT).show()
                                }
                            }
                        })
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
                            onlinePlaylistViewModel.updatePlaylistForUser(playlist, it)
                        }
                        onlinePlaylistViewModel.updatePlaylist.observe(viewLifecycleOwner, Observer {
                            when (it) {
                                is UiState.Loading -> {

                                }
                                is UiState.Failure -> {

                                }
                                is UiState.Success -> {
                                    Toast.makeText(requireContext(), it.data, Toast.LENGTH_SHORT).show()
                                }
                            }
                        })
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
                        onlinePlaylistViewModel.deletePlaylistForUser(playlist, it)
                    }
                    onlinePlaylistViewModel.deletePlaylist.observe(viewLifecycleOwner, Observer {
                        when (it) {
                            is UiState.Loading -> {

                            }
                            is UiState.Failure -> {

                            }
                            is UiState.Success -> {
                                Toast.makeText(requireContext(), it.data, Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
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
                onlinePlaylistViewModel.deleteSongInPlaylist(songList[position], playlist, it)
            }
            onlinePlaylistViewModel.deleteSongInPlaylist.observe(viewLifecycleOwner, Observer {
                when (it) {
                    is UiState.Loading -> {

                    }
                    is UiState.Failure -> {

                    }
                    is UiState.Success -> {
                        Toast.makeText(requireContext(), it.data, Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }
}