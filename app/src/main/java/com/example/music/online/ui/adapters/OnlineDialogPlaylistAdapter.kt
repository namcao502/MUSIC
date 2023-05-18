package com.example.music.online.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.music.R
import com.example.music.databinding.OnlinePlaylistDialogRowItemBinding
import com.example.music.databinding.OnlinePlaylistRowItemBinding
import com.example.music.databinding.PlaylistRowItemBinding
import com.example.music.online.data.models.OnlinePlaylist
class OnlineDialogPlaylistAdapter(
    private val context: Context,
    private val itemClickListener: ItemClickListener
    ): RecyclerView.Adapter<OnlineDialogPlaylistAdapter.ViewHolder>() {

    var playlist = emptyList<OnlinePlaylist>()
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

        with(holder){
            itemView.setOnClickListener {
                itemClickListener.onItemPlaylistClick(playlist[position])
            }
            binding.menuBtn.setOnClickListener {
                val wrapper: Context = ContextThemeWrapper(context, R.style.PopupMenu)
                PopupMenu(wrapper, binding.menuBtn).apply {
                    menuInflater.inflate(R.menu.row_playlist_menu, this.menu)
                    setOnMenuItemClickListener { menuItem ->
                        itemClickListener.onMenuClick(menuItem.title.toString(), playlist[position])
                        true
                    }
                    // Showing the popup menu
                    show()
                }
            }
            binding.titleTxt.text = playlist[position].name
            if (playlist[position].songs!!.size > 1){
                binding.countSongTxt.text = playlist[position].songs!!.size.toString().plus(" songs")
            }
            else {
                binding.countSongTxt.text = playlist[position].songs!!.size.toString().plus(" song")
            }
            binding.countLengthTxt.visibility = View.GONE
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    fun setData(playlist: List<OnlinePlaylist>){
        this.playlist = playlist
        notifyDataSetChanged()
    }

    interface ItemClickListener {
        fun onMenuClick(action: String, playlist: OnlinePlaylist)
        fun onItemPlaylistClick(playlist: OnlinePlaylist)

    }
}