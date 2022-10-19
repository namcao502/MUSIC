package com.example.music.online.ui.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.music.R
import com.example.music.online.data.models.OnlinePlaylist
import com.example.music.online.data.models.OnlineSong
import com.example.music.databinding.FragmentOnlinePlaylistBinding
import com.example.music.online.ui.adapters.OnlinePlaylistAdapter
import com.example.music.online.viewModels.OnlinePlaylistViewModel
import com.example.music.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnlinePlaylistFragment(private val clickSongFromDetail: ClickSongFromDetail):
    Fragment(),
    OnlinePlaylistAdapter.ClickAPlaylist,
    DetailCollectionFragment.ClickASongInDetail {

    private val onlinePlaylistViewModel: OnlinePlaylistViewModel by viewModels()

    private val onlinePlaylistAdapter: OnlinePlaylistAdapter by lazy {
        OnlinePlaylistAdapter(requireContext(), this)
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

        Firebase.auth.currentUser?.let {
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
            createDialogForAddPlaylist(onlinePlaylistViewModel)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun callBackFromMenuPlaylistClick(action: String, playlist: OnlinePlaylist) {
        if (action == "Rename"){
            createDialogForRenamePlaylist(playlist, onlinePlaylistViewModel)
        }
        if (action == "Delete"){
            createDialogForDeletePlaylist(playlist, onlinePlaylistViewModel)
        }
    }

    override fun callBackFromPlaylistClick(playlist: OnlinePlaylist) {
        val detailCollectionFragment = DetailCollectionFragment(playlist.name!!, playlist.songs!!, playlist.imgFilePath!!, this)
        val args = Bundle()
        args.putSerializable(FireStoreCollection.PLAYLIST, playlist)
        detailCollectionFragment.arguments = args
        val fragmentTransaction: FragmentTransaction = parentFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.fragment_container, detailCollectionFragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    override fun callBackFromClickASongInDetail(songList: List<OnlineSong>, position: Int) {
        clickSongFromDetail.callBackFromClickSongInDetail(songList, position)
    }

    interface ClickSongFromDetail{
        fun callBackFromClickSongInDetail(songList: List<OnlineSong>, position: Int)
    }
}