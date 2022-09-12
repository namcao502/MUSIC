package com.example.music.ui.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.music.R
import com.example.music.databinding.FragmentOnlineSongBinding
import com.example.music.databinding.FragmentSongBinding
import com.example.music.models.OnlineSong
import com.example.music.models.Song
import com.example.music.ui.adapters.DialogPlaylistAdapter
import com.example.music.ui.adapters.OnlineSongAdapter
import com.example.music.ui.adapters.SongAdapter
import com.example.music.viewModels.FirebaseViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnlineSongFragment(private val songFromAdapterClick: SongFromAdapterClick)
    : Fragment(), OnlineSongAdapter.ItemSongClickListener {

    private var _binding: FragmentOnlineSongBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val onlineSongAdapter: OnlineSongAdapter by lazy {
        OnlineSongAdapter(requireContext(), this)
    }

    private val firebaseViewModel: FirebaseViewModel by viewModels()

    private lateinit var currentSong: OnlineSong

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentOnlineSongBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.songRecyclerView.apply {
            adapter = onlineSongAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        firebaseViewModel.getAllSongs()
        firebaseViewModel.song.observe(viewLifecycleOwner, Observer {

        })
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
//            createDialogForAddToPlaylist()
            currentSong = songList[position]
        }
        if (action == "Delete"){
//            createDialogForDeleteSong(songList[position])
        }
    }

//    private fun createDialogForAddToPlaylist() {
//        val dialog = Dialog(requireContext())
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog.setCancelable(true)
//        dialog.setContentView(R.layout.fragment_playlist)
//
//        //set size for dialog
//        val lp = WindowManager.LayoutParams()
//        lp.copyFrom(dialog.window!!.attributes)
//        lp.width = WindowManager.LayoutParams.MATCH_PARENT
//        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
//        lp.gravity = Gravity.CENTER
//        dialog.window!!.attributes = lp
//
//        val recyclerView = dialog.findViewById<RecyclerView>(R.id.playlist_recyclerView)
//        recyclerView.adapter = dialogPlaylistAdapter
//        recyclerView.layoutManager = LinearLayoutManager(dialog.context)
//
//        playlistViewModel.readAllPlaylists().observe(viewLifecycleOwner, Observer {
//            dialogPlaylistAdapter.setData(it)
//        })
//
//        val addBtn = dialog.findViewById<FloatingActionButton>(R.id.add_btn)
//
//        addBtn.setOnClickListener {
//            createDialogForAddPlaylist()
//            dialog.cancel()
//        }
//        dialog.show()
//
//        if (dialog.isShowing){
//            addBtn.visibility = View.VISIBLE
//        }
//        else {
//            addBtn.visibility = View.GONE
//        }
//    }

//    private fun createDialogForDeleteSong(song: Song){
//
//        val builder = AlertDialog.Builder(requireContext())
//
//        builder.setMessage("Delete ${song.name}?")
//            .setTitle("")
//            .setPositiveButton("Delete",
//                DialogInterface.OnClickListener { dialog, id ->
//                    songViewModel.deleteSong(song)
//                })
//            .setNegativeButton("Cancel",
//                DialogInterface.OnClickListener { dialog, id ->
//                    // User cancelled the dialog
//                })
//        // Create the AlertDialog object and return it
//        builder.create().show()
//    }

    override fun callBackFromSongClick(songList: List<OnlineSong>, position: Int) {
        songFromAdapterClick.callBackFromSongFragment(songList, position)
    }

    interface SongFromAdapterClick{
        fun callBackFromSongFragment(songs: List<OnlineSong>, position: Int)
    }

}