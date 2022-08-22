package com.example.music.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.music.R
import com.example.music.databinding.PlaylistRowItemBinding
import com.example.music.models.Playlist

class PlaylistAdapter(private val context: Context, private val itemClickListener: ItemClickListener): RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

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

            binding.imageButton.setOnClickListener {

                PopupMenu(context, binding.imageButton).apply {
                    menuInflater.inflate(R.menu.add_playlist_menu, this.menu)
                    setOnMenuItemClickListener { menuItem ->
                        itemClickListener.onClick(menuItem.title.toString(), playlist[position])
                        true
                    }
                    // Showing the popup menu
                    show()
                }
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

    interface ItemClickListener {
        fun onClick(action: String, playlist: Playlist)
    }

}