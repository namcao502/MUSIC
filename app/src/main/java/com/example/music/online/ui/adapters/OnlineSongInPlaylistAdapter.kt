package com.example.music.online.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.music.R
import com.example.music.databinding.SongRowItemBinding
import com.example.music.online.data.models.OnlinePlaylist
import com.example.music.online.data.models.OnlineSong

class OnlineSongInPlaylistAdapter(
    private val context: Context,
    private val itemClickListener: ItemSongInPlaylistClickListener
)
    : RecyclerView.Adapter<OnlineSongInPlaylistAdapter.ViewHolder>() {

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
//                Toast.makeText(itemView.context, "Clicked at $position", Toast.LENGTH_SHORT).show()
                itemClickListener.callBackFromSongInPlaylist(songList, position)
            }

            binding.menuBtn.setOnClickListener {

                val wrapper: Context = ContextThemeWrapper(context, R.style.PopupMenu)
                PopupMenu(wrapper, binding.menuBtn).apply {
                    menuInflater.inflate(R.menu.row_song_in_playlist_menu, this.menu)
                    setOnMenuItemClickListener { menuItem ->
                        itemClickListener.callBackFromMenuSongInPlaylist(menuItem.title.toString(), songList, position, OnlinePlaylist())
                        true
                    }
                    // Showing the popup menu
                    show()
                }
            }

            with(songList[position]){
                binding.titleTxt.text = this.name
                binding.lengthTxt.visibility = View.GONE
                binding.authorTxt.visibility = View.GONE
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(listSong: List<OnlineSong>){
        this.songList = listSong
        notifyDataSetChanged()
    }

    interface ItemSongInPlaylistClickListener{
        fun callBackFromSongInPlaylist(songList: List<OnlineSong>, position: Int)
        fun callBackFromMenuSongInPlaylist(action: String, songList: List<OnlineSong>, position: Int, playlist: OnlinePlaylist)
    }
}