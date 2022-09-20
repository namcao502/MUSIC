package com.example.music.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.music.R
import com.example.music.databinding.PlaylistRowItemBinding
import com.example.music.data.models.online.OnlinePlaylist

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
                PopupMenu(context, binding.menuBtn).apply {
                    menuInflater.inflate(R.menu.row_playlist_menu, this.menu)
                    setOnMenuItemClickListener { menuItem ->
                        itemClickListener.onMenuClick(menuItem.title.toString(), playlist[position])
                        true
                    }
                    // Showing the popup menu
                    show()
                }
            }

            with(playlist[position]){
                binding.titleTxt.text = this.name
//                songInPlaylistViewModel.getSongsOfPlaylist(this.playlist_id).observe(lifecycle, Observer {
//                    if (it != null){
//                        val countSong = it.listSong.size.toString()
//                        var countDuration = 0
//                        for (x in it.listSong){
//                            countDuration += x.duration
//                        }
//                        binding.countLengthTxt.text = SimpleDateFormat("mm:ss").format(countDuration).toString()
//                        binding.countSongTxt.text = countSong.plus(" songs")
//                    }
//                })
            }
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