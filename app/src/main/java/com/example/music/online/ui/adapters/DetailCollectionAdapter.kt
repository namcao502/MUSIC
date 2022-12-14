package com.example.music.online.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.music.R
import com.example.music.utils.UiState
import com.example.music.databinding.SongRowItemBinding
import com.example.music.online.data.models.OnlineArtist
import com.example.music.online.data.models.OnlineSong
import com.example.music.online.viewModels.OnlineArtistViewModel

class DetailCollectionAdapter(
    private val context: Context,
    private val clickASong: ClickASong,
    private val artistViewModel: OnlineArtistViewModel,
    private val lifecycleOwner: LifecycleOwner
): RecyclerView.Adapter<DetailCollectionAdapter.ViewHolder>() {

    var songList = emptyList<OnlineSong>()
    private var artistList: ArrayList<String> = ArrayList()

    inner class ViewHolder(val binding: SongRowItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SongRowItemBinding.inflate(
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
                clickASong.callBackFromDetailClick(songList, position)
            }

            binding.menuBtn.setOnClickListener {
                PopupMenu(context, binding.menuBtn).apply {
                    menuInflater.inflate(R.menu.row_song_menu, this.menu)
                    setOnMenuItemClickListener { menuItem ->
                        clickASong.callBackFromMenuDetailClick(menuItem.title.toString(), songList, position)
                        true
                    }
                    show()
                }
            }

            with(songList[position]){

                binding.titleTxt.text = name
                binding.lengthTxt.visibility = View.GONE
                Glide.with(context).load(imgFilePath).into(binding.imageView)

//                var text = "Unknown"
//                if (artistList.isEmpty()){
//                    binding.authorTxt.text = text
//                }
//                else {
//                    for (x in artistList[position]){
//                        text = ""
//                        text += x.name.plus(", ")
//                    }
//                    binding.authorTxt.text = text.dropLast(2)
//                }

                artistViewModel.getAllArtistFromSong2(this, position)
                artistViewModel.artistInSong2[position].observe(lifecycleOwner){
                    when(it){
                        is UiState.Loading -> {

                        }
                        is UiState.Failure -> {

                        }
                        is UiState.Success -> {
                            var text = ""
                            for (x in it.data){
                                text += x.name.plus(", ")
                            }
                            binding.authorTxt.text = text.dropLast(2)
                        }
                    }
                }
            }
        }
    }

    interface ClickASong{
        fun callBackFromDetailClick(songList: List<OnlineSong>, position: Int)
        fun callBackFromMenuDetailClick(action: String, songList: List<OnlineSong>, position: Int)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(songList: List<OnlineSong>){
        this.songList = songList
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setDataForArtist(artistList: ArrayList<String>){
        this.artistList = artistList
        notifyDataSetChanged()
    }

}