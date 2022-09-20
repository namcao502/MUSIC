package com.example.music.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.music.R
import com.example.music.UiState
import com.example.music.databinding.PlaylistRowItemBinding
import com.example.music.data.models.online.OnlinePlaylist
import com.example.music.data.models.online.OnlineSong
import com.example.music.viewModels.FirebaseViewModel

class OnlinePlaylistAdapter(
    private val context: Context,
    private val itemPlaylistClickListener: ItemPlaylistClickListener,
    private val lifecycle: LifecycleOwner,
    private val firebaseViewModel: FirebaseViewModel)
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
                        itemPlaylistClickListener.callBackFromMenuPlaylistClick(menuItem.title.toString(), playlist[position])
                        true
                    }
                    // Showing the popup menu
                    show()
                }
            }

            with(playlist[position]){

                val onlineSongInPlaylistAdapter: OnlineSongInPlaylistAdapter by lazy {
                    OnlineSongInPlaylistAdapter(context, object : OnlineSongInPlaylistAdapter.ItemSongInPlaylistClickListener{

                        override fun callBackFromSongInPlaylist(songList: List<OnlineSong>, position: Int) {
                            itemPlaylistClickListener.callBackFromSongInPlaylist(songList, position)
                        }

                        override fun callBackFromMenuSongInPlaylist(action: String, songList: List<OnlineSong>, position: Int, playlist: OnlinePlaylist) {
                            itemPlaylistClickListener.callBackFromMenuSongInPlaylist(action, songList, position, this@with)
                        }
                    })
                }

                binding.songInPlaylistRecyclerView.apply {
                    adapter = onlineSongInPlaylistAdapter
                    layoutManager = LinearLayoutManager(context)
                }
                binding.titleTxt.text = this.name

//                firebaseViewModel.getAllSongInPlaylist(this)
//                firebaseViewModel.songInPlaylist.observe(lifecycle, Observer {
//                    if (it != null){
//                        onlineSongInPlaylistAdapter.setData(it)
//                    }
//                })

                firebaseViewModel.getAllSongInPlaylist(playlist[position], position)
                firebaseViewModel.songInPlaylist[position].observe(lifecycle, Observer {
                    when(it){
                        is UiState.Loading -> {

                        }
                        is UiState.Failure -> {

                        }
                        is UiState.Success -> {
                            onlineSongInPlaylistAdapter.setData(it.data)
                        }
                    }
                })

                //load count length and count song
//                songInPlaylistViewModel.getSongsOfPlaylist(this.playlist_id).observe(lifecycle, Observer {
//                    if (it != null){
//                        songInPlaylistAdapter.setData(it.listSong)
//                        val countSong = it.listSong.size.toString()
//                        var countDuration = 0
//                        for (x in it.listSong){
//                            countDuration += x.duration
//                        }
//                        binding.countLengthTxt.text = SimpleDateFormat("mm:ss").format(countDuration).toString()
//                        binding.countSongTxt.text = countSong.plus(" songs")
//
////                        Log.i("TAG502", "onBindViewHolder: ${it.listSong}")
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

    interface ItemPlaylistClickListener {
        fun callBackFromMenuPlaylistClick(action: String, playlist: OnlinePlaylist)
        fun callBackFromSongInPlaylist(songList: List<OnlineSong>, position: Int)
        fun callBackFromMenuSongInPlaylist(action: String, songList: List<OnlineSong>, position: Int, playlist: OnlinePlaylist)
    }
}