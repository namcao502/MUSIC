package com.example.music.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.music.R
import com.example.music.databinding.SongRowItemBinding
import com.example.music.data.models.online.OnlineSong

class OnlineListSongAdapter(
    private val context: Context)
    : RecyclerView.Adapter<OnlineListSongAdapter.ViewHolder>() {

    var songList = emptyList<OnlineSong>()

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
                //click on a song
//                itemClickListener.callBackFromSongClick(songList, position)
            }

            binding.menuBtn.setOnClickListener {
                PopupMenu(context, binding.menuBtn).apply {
                    menuInflater.inflate(R.menu.row_song_menu, this.menu)
                    setOnMenuItemClickListener { menuItem ->
//                        itemClickListener.callBackFromMenuSongClick(menuItem.title.toString(), songList, position)
                        true
                    }
                    // Showing the popup menu
                    show()
                }
            }

            with(songList[position]){
                binding.titleTxt.text = this.name
                binding.authorTxt.visibility = View.GONE
                binding.lengthTxt.visibility = View.GONE
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(songList: List<OnlineSong>){
        this.songList = songList
        notifyDataSetChanged()
    }

    interface ItemSongClickListener {
        fun callBackFromMenuListSongClick(action: String, songList: List<OnlineSong>, position: Int)
        fun callBackFromListSongClick(songList: List<OnlineSong>, position: Int)
    }

}