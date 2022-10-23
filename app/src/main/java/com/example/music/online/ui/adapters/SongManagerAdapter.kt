package com.example.music.online.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.music.R
import com.example.music.databinding.OnlinePlaylistRowItemBinding
import com.example.music.databinding.SongManagerRowItemBinding
import com.example.music.utils.UiState
import com.example.music.databinding.SongRowItemBinding
import com.example.music.online.data.models.OnlineSong
import com.example.music.online.viewModels.FirebaseViewModel
import com.example.music.online.viewModels.OnlineArtistViewModel

class SongManagerAdapter(
    private val clickASong: ClickASong,
    val lifecycleOwner: LifecycleOwner,
    val firebaseViewModel: FirebaseViewModel
): RecyclerView.Adapter<SongManagerAdapter.ViewHolder>() {

    var songList = emptyList<OnlineSong>()

    inner class ViewHolder(val binding: SongManagerRowItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SongManagerRowItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return songList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        with(holder){

            itemView.setOnClickListener {
                clickASong.callBackFromClickASong(songList[position])
            }

            with(songList[position]){

                binding.titleTxt.text = name

//                firebaseViewModel.getSongFromSongID(id!!, position)
//                firebaseViewModel.songFromID2[position].observe(lifecycleOwner){
//                    when(it) {
//                        is UiState.Loading -> {
//
//                        }
//                        is UiState.Failure -> {
//
//                        }
//                        is UiState.Success -> {
//                            binding.titleTxt.text = it.data.name
//                        }
//                    }
//                }
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(songList: List<OnlineSong>){
        this.songList = songList
        notifyDataSetChanged()
    }

    interface ClickASong{
        fun callBackFromClickASong(song: OnlineSong)
    }

}