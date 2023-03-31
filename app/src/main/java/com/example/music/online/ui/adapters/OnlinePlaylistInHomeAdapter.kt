package com.example.music.online.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.music.databinding.ColumnItemBinding
import com.example.music.online.data.models.OnlinePlaylist

class OnlinePlaylistInHomeAdapter(val context: Context,
                                  private val clickAPlaylist: ClickAPlaylist)
    : RecyclerView.Adapter<OnlinePlaylistInHomeAdapter.ViewHolder>() {

    var playlist = emptyList<OnlinePlaylist>()

    inner class ViewHolder(val binding: ColumnItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ColumnItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return playlist.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        with(holder){
            itemView.setOnClickListener {
                clickAPlaylist.callBackFromPlaylistClick(playlist[position])
            }
            with(playlist[position]){
                binding.itemName.text = this.name
                val imgUrl = this.imgFilePath
                Glide.with(context).load(imgUrl).into(binding.itemImg)
            }
        }

    }

    interface ClickAPlaylist{
        fun callBackFromPlaylistClick(playlist: OnlinePlaylist)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(playlist: List<OnlinePlaylist>){
        this.playlist = playlist
        notifyDataSetChanged()
    }

}