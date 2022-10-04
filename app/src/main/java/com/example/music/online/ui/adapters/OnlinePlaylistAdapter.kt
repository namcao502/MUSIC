package com.example.music.online.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.music.R
import com.example.music.databinding.PlaylistRowItemBinding
import com.example.music.online.data.models.OnlinePlaylist

class OnlinePlaylistAdapter(
    private val context: Context,
    private val clickAPlaylist: ClickAPlaylist
)
    : RecyclerView.Adapter<OnlinePlaylistAdapter.ViewHolder>() {

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
                clickAPlaylist.callBackFromPlaylistClick(playlist[position])
            }

            binding.menuBtn.setOnClickListener {
                PopupMenu(context, binding.menuBtn).apply {
                    menuInflater.inflate(R.menu.row_playlist_menu, this.menu)
                    setOnMenuItemClickListener { menuItem ->
                        clickAPlaylist.callBackFromMenuPlaylistClick(menuItem.title.toString(), playlist[position])
                        true
                    }
                    // Showing the popup menu
                    show()
                }
            }

            with(playlist[position]){

                binding.titleTxt.text = this.name
                binding.countLengthTxt.visibility = View.GONE
                binding.countSongTxt.visibility = View.GONE

                if (this.imgFilePath!!.isNotEmpty()){
                    Glide.with(context).load(this.imgFilePath).into(binding.imageView)
                }
                else {
                    binding.imageView.visibility = View.GONE
                }
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(playlist: List<OnlinePlaylist>){
        this.playlist = playlist
        notifyDataSetChanged()
    }

    interface ClickAPlaylist {
        fun callBackFromMenuPlaylistClick(action: String, playlist: OnlinePlaylist)
        fun callBackFromPlaylistClick(playlist: OnlinePlaylist)
    }
}