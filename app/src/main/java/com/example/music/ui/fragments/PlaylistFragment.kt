package com.example.music.ui.fragments

import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.music.R
import com.example.music.databinding.FragmentPlaylistBinding
import com.example.music.models.Playlist
import com.example.music.viewModels.PlaylistViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlaylistFragment : Fragment(), PlaylistAdapter.ItemClickListener {

    private val playlistViewModel: PlaylistViewModel by viewModels()
    private val playlistAdapter: PlaylistAdapter by lazy { PlaylistAdapter(requireContext(), this) }

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

        binding.addBtn.setOnClickListener {
            createDialogFor("Create", 0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(action: String, playlist: Playlist) {
        if (action == "Rename"){
            createDialogFor(action, playlist.id)
        }
        if (action == "Delete"){
            playlistViewModel.deletePlaylist(playlist)
        }
    }

    private fun createDialogFor(action: String, playlistId: Int){
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.add_new_playlist_dialog, null)

        builder.setMessage(action)
            .setTitle("")
            .setView(view)
            .setPositiveButton(action,
                DialogInterface.OnClickListener { dialog, id ->

                    val title = view.findViewById<EditText>(R.id.title_et).text.toString()

                    if (title.isEmpty()){
                        Toast.makeText(requireContext(), "Name can not be empty", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        val playlist = Playlist(playlistId, title)
                        if (action == "Rename"){
                            playlistViewModel.updatePlaylist(playlist)
                        }
                        else {
                            playlistViewModel.addPlaylist(playlist)
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
}