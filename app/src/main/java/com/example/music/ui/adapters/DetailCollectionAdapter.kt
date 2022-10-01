package com.example.music.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.example.music.R
import com.example.music.UiState
import com.example.music.data.models.online.OnlineArtist
import com.example.music.databinding.SongRowItemBinding
import com.example.music.data.models.online.OnlineSong
import com.example.music.viewModels.online.OnlineArtistViewModel

class DetailCollectionAdapter(
    private val context: Context,
    private val clickASong: ClickASong,
    private val lifecycle: LifecycleOwner,
    private val artistViewModel: OnlineArtistViewModel)
    : RecyclerView.Adapter<DetailCollectionAdapter.ViewHolder>() {

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
                //click on a song
                clickASong.callBackFromDetailClick(songList, position)
            }

            binding.menuBtn.setOnClickListener {
                PopupMenu(context, binding.menuBtn).apply {
                    menuInflater.inflate(R.menu.row_song_menu, this.menu)
                    setOnMenuItemClickListener { menuItem ->
                        true
                    }
                    // Showing the popup menu
                    show()
                }
            }

            with(songList[position]){
                binding.titleTxt.text = this.name
                binding.lengthTxt.visibility = View.GONE
                artistViewModel.getAllArtistFromSong(this, position)
                artistViewModel.artistInSong[position].observe(lifecycle){
                    when(it){
                        is UiState.Loading -> {

                        }
                        is UiState.Failure -> {

                        }
                        is UiState.Success -> {
                            Log.i("TAG502", "onBindViewHolder: ${it.data}")
                            var text = ""
                            for (x in it.data){
                                text += x.name.plus(", ")
                            }
                            binding.authorTxt.text = text.dropLast(2)
                        }
                    }
                }
            }
        }

    }

    interface ClickASong{
        fun callBackFromDetailClick(songList: List<OnlineSong>, position: Int)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(songList: List<OnlineSong>){
        this.songList = songList
        notifyDataSetChanged()
    }

}