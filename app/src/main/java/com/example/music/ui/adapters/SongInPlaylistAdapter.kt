package com.example.music.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.music.R
import com.example.music.databinding.SongRowItemBinding
import com.example.music.data.models.offline.Playlist
import com.example.music.data.models.offline.Song

class SongInPlaylistAdapter(
    private val context: Context,
    private val itemClickListener: ItemSongInPlaylistClickListener)
    : RecyclerView.Adapter<SongInPlaylistAdapter.ViewHolder>() {

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

        with(holder){

            itemView.setOnClickListener {
//                Toast.makeText(itemView.context, "Clicked at $position", Toast.LENGTH_SHORT).show()
                itemClickListener.callBackFromSongInPlaylist(songList, position)
            }

            binding.menuBtn.setOnClickListener {

                PopupMenu(context, binding.menuBtn).apply {
                    menuInflater.inflate(R.menu.row_song_in_playlist_menu, this.menu)
                    setOnMenuItemClickListener { menuItem ->
                        itemClickListener.callBackFromMenuSongInPlaylist(menuItem.title.toString(), songList, position, Playlist(0, ""))
                        true
                    }
                    // Showing the popup menu
                    show()
                }
            }

            with(songList[position]){
                binding.titleTxt.text = this.name
                val minutes = this.duration / 1000 / 60
                val seconds = this.duration / 1000 % 60
                if (seconds < 10){
                    binding.lengthTxt.text = "$minutes:0$seconds"
                }
                else {
                    binding.lengthTxt.text = "$minutes:$seconds"
                }

                binding.authorTxt.text = this.artists
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(listSong: List<Song>){
        this.songList = listSong
        notifyDataSetChanged()
    }

    interface ItemSongInPlaylistClickListener{
        fun callBackFromSongInPlaylist(songList: List<Song>, position: Int)
        fun callBackFromMenuSongInPlaylist(action: String, songList: List<Song>, position: Int, playlist: Playlist)
    }
}