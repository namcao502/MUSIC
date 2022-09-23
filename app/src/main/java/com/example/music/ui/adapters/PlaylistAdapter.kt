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
import com.example.music.databinding.PlaylistRowItemBinding
import com.example.music.data.models.offline.Playlist
import com.example.music.data.models.offline.Song
import com.example.music.viewModels.offline.SongInPlaylistViewModel
import java.text.SimpleDateFormat

class PlaylistAdapter(
    private val context: Context,
    private val itemPlaylistClickListener: ItemPlaylistClickListener,
    private val lifecycle: LifecycleOwner,
    private val songInPlaylistViewModel: SongInPlaylistViewModel
)
    : RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

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
//                songInPlaylistViewModel.getPlaylistId(playlist[position].playlist_id)

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

                val songInPlaylistAdapter: SongInPlaylistAdapter by lazy {
                    SongInPlaylistAdapter(context, object : SongInPlaylistAdapter.ItemSongInPlaylistClickListener{

                        override fun callBackFromSongInPlaylist(songList: List<Song>, position: Int) {
                            itemPlaylistClickListener.callBackFromSongInPlaylist(songList, position)
                        }

                        override fun callBackFromMenuSongInPlaylist(action: String, songList: List<Song>, position: Int, playlist: Playlist) {
                            itemPlaylistClickListener.callBackFromMenuSongInPlaylist(action, songList, position, this@with)
                        }

                    })
                }

                binding.songInPlaylistRecyclerView.apply {
                    adapter = songInPlaylistAdapter
                    layoutManager = LinearLayoutManager(context)
                }

                //load count length and count song

                this.playlist_id.let { playlistID ->
                    songInPlaylistViewModel.getSongsOfPlaylist(playlistID).observe(lifecycle, Observer {
                        if (it != null){
                            songInPlaylistAdapter.setData(it.listSong)
                            val countSong = it.listSong.size.toString()
                            var countDuration = 0
                            for (x in it.listSong){
                                countDuration += x.duration
                            }
                            binding.countLengthTxt.text = SimpleDateFormat("mm:ss").format(countDuration).toString()
                            binding.countSongTxt.text = countSong.plus(" songs")
                        //                        Log.i("TAG502", "onBindViewHolder: ${it.listSong}")
                        }
                    })
                }
                binding.titleTxt.text = this.name
                binding.imageView.visibility = View.GONE
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
        fun callBackFromSongInPlaylist(songList: List<Song>, position: Int)
        fun callBackFromMenuSongInPlaylist(action: String, songList: List<Song>, position: Int, playlist: Playlist)
    }
}