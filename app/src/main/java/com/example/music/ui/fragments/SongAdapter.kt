package com.example.music.ui.fragments

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.music.databinding.SongRowItemBinding
import com.example.music.models.Song
import java.util.concurrent.TimeUnit

class SongAdapter: RecyclerView.Adapter<SongAdapter.ViewHolder>() {

    var songList = emptyList<Song>()

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

//        val currentItem = songList[position]
//
//        holder.itemView.title_txt.text = currentItem.name
//        holder.itemView.author_txt.text = ""
//        holder.itemView.length_txt.text = currentItem.duration.toString()
//
//        holder.itemView.setOnClickListener {
//
//        }

        with(holder){
            itemView.setOnClickListener {
                Toast.makeText(itemView.context, "Clicked at $position", Toast.LENGTH_SHORT).show()
            }
            with(songList[position]){
                binding.titleTxt.text = this.name
                val minutes = this.duration / 1000 / 60
                val seconds = this.duration / 1000 % 60
                binding.lengthTxt.text = "$minutes:$seconds"
                binding.authorTxt.text = this.artists
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(listSong: List<Song>){
        this.songList = listSong
        notifyDataSetChanged()
    }

}