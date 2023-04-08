package com.example.music.offline.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.example.music.R
import com.example.music.databinding.PlaylistRowItemBinding
import com.example.music.offline.data.models.Playlist
import com.example.music.offline.viewModels.SongInPlaylistViewModel
import java.text.SimpleDateFormat

class DialogPlaylistAdapter(
    private val context: Context,
    private val itemClickListener: ItemClickListener,
    private val lifecycle: LifecycleOwner,
    private val songInPlaylistViewModel: SongInPlaylistViewModel
    ): RecyclerView.Adapter<DialogPlaylistAdapter.ViewHolder>() {

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

    @SuppressLint("SimpleDateFormat")
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
                this.playlist_id.let { it2 ->
                    songInPlaylistViewModel.getSongsOfPlaylist(it2).observe(lifecycle, Observer {
                        if (it != null){
                            val countSong = it.listSong.size.toString()
                            var countDuration = 0
                            for (x in it.listSong){
                                countDuration += x.duration
                            }
                            binding.countLengthTxt.text = SimpleDateFormat("mm:ss").format(countDuration).toString()
                            binding.countSongTxt.text = countSong.plus(" songs")
                        }
                    })
                }
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(playlist: List<Playlist>){
        this.playlist = playlist
        notifyDataSetChanged()
    }

    interface ItemClickListener {
        fun onMenuClick(action: String, playlist: Playlist)
        fun onItemPlaylistClick(playlist: Playlist)
    }

}