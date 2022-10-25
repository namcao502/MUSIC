package com.example.music.online.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.music.databinding.SongManagerRowItemBinding
import com.example.music.online.data.models.OnlineSong

class SongManagerAdapter(
    private val clickASong: ClickASong
): RecyclerView.Adapter<SongManagerAdapter.ViewHolder>() {

//    ,
//    val lifecycleOwner: LifecycleOwner,
//    val firebaseViewModel: FirebaseViewModel

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

                binding.titleTxt.text = name.plus(", views = $views")

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