package com.example.music.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.music.R
import com.example.music.databinding.PlaylistRowItemBinding
import com.example.music.models.Playlist
import com.example.music.viewModels.SongInPlaylistViewModel
import java.text.SimpleDateFormat

class PlaylistAdapter(
    private val context: Context,
    private val itemClickListener: ItemPlaylistClickListener,
    private val lifecycle: LifecycleOwner,
    private val songInPlaylistViewModel: SongInPlaylistViewModel,
    private val songInPlaylistAdapter: SongInPlaylistAdapter): RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

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

        with(holder){

            itemView.setOnClickListener {
                if (binding.songInPlaylistRecyclerView.visibility == View.VISIBLE){
                    binding.songInPlaylistRecyclerView.visibility = View.GONE
                }
                else {
                    binding.songInPlaylistRecyclerView.visibility = View.VISIBLE
                }
            }

            binding.menuBtn.setOnClickListener {
                PopupMenu(context, binding.menuBtn).apply {
                    menuInflater.inflate(R.menu.row_playlist_menu, this.menu)
                    setOnMenuItemClickListener { menuItem ->
                        itemClickListener.callBackFromMenuPlaylistClick(menuItem.title.toString(), playlist[position])
                        true
                    }
                    // Showing the popup menu
                    show()
                }
            }

            binding.songInPlaylistRecyclerView.apply {
                adapter = songInPlaylistAdapter
                layoutManager = LinearLayoutManager(context)
            }

            with(playlist[position]){
                //load count length and count song
                songInPlaylistViewModel.getPlaylistId(this.playlist_id)
                songInPlaylistViewModel.getSongsOfPlaylist(this.playlist_id).observe(lifecycle, Observer {
                    if (it != null){
                        songInPlaylistAdapter.setData(it.listSong)
                        val countSong = it.listSong.size.toString()
                        var countDuration = 0
                        for (x in it.listSong){
                            countDuration += x.duration
                        }
                        binding.countLengthTxt.text = SimpleDateFormat("mm:ss").format(countDuration).toString()
                        binding.countSongTxt.text = countSong.plus(" songs")

                        Log.i("TAG502", "onBindViewHolder: ${it.listSong}")
                    }
                })
                binding.titleTxt.text = this.name
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(playlist: List<Playlist>){
        this.playlist = playlist
        notifyDataSetChanged()
    }

    interface ItemPlaylistClickListener {
        fun callBackFromMenuPlaylistClick(action: String, playlist: Playlist)
    }

}