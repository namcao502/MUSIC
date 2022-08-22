package com.example.music.ui.fragments

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.music.databinding.PlaylistRowItemBinding
import com.example.music.models.Playlist

class PlaylistAdapter: RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

    var playlist = emptyList<Playlist>()

    inner class ViewHolder(val binding: PlaylistRowItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PlaylistRowItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return playlist.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

//        val currentItem = playlist[position]
//
//        holder.itemView.title_txt.text = currentItem.name
//        holder.itemView.count_song_txt.text = ""
//        holder.itemView.count_length_txt.text = ""
//
//        holder.itemView.setOnClickListener {
//
//        }

        with(holder){
            itemView.setOnClickListener {
                Toast.makeText(itemView.context, "Clicked", Toast.LENGTH_SHORT).show()
            }
            with(playlist[position]){
                binding.titleTxt.text = this.name
                binding.countSongTxt.text = ""
                binding.countLengthTxt.text = ""
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(playlist: List<Playlist>){
        this.playlist = playlist
        notifyDataSetChanged()
    }

}