package com.example.music.online.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.music.R
import com.example.music.databinding.SongRowItemBinding
import com.example.music.online.data.models.OnlineArtist
import com.example.music.online.data.models.OnlineSong
import com.example.music.online.viewModels.OnlineArtistViewModel
import com.example.music.utils.UiState

class OnlineSongAdapter(
    private val context: Context,
    private val itemClickListener: ItemSongClickListener,
    private val lifecycleOwner: LifecycleOwner,
    private val artistViewModel: OnlineArtistViewModel
): RecyclerView.Adapter<OnlineSongAdapter.ViewHolder>() {

    var songList = emptyList<OnlineSong>()
    private var artistList: ArrayList<List<OnlineArtist>> = ArrayList()

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

    @SuppressLint("RestrictedApi")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        with(holder){

            itemView.setOnClickListener {
                itemClickListener.callBackFromSongClick(songList, position)
            }

            binding.menuBtn.setOnClickListener {
                val wrapper: Context = ContextThemeWrapper(context, R.style.PopupMenu)
                PopupMenu(wrapper, binding.menuBtn).apply {
                    menuInflater.inflate(R.menu.song_menu, this.menu)
                    setForceShowIcon(true)
                    setOnMenuItemClickListener { menuItem ->
                        itemClickListener.callBackFromMenuSongClick(menuItem.title.toString(), songList, position)
                        true
                    }
                    // Showing the popup menu
                    try {
                        val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                        fieldMPopup.isAccessible = true
                        val mPopup = fieldMPopup.get(this)
                        mPopup.javaClass
                            .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                            .invoke(mPopup, true)
                    } catch (_: Exception){

                    } finally {
                        show()
                    }
                }
            }

            with(songList[position]){

                binding.titleTxt.text = name
                binding.lengthTxt.visibility = View.GONE
                Glide.with(context).load(imgFilePath).into(binding.imageView)

//                var text = "Unknown"
//                if (artistList.isEmpty()){
//                    binding.authorTxt.text = text
//                }
//                else {
//                    for (x in artistList[position]){
//                        text = ""
//                        text += x.name.plus(", ")
//                    }
//                    binding.authorTxt.text = text.dropLast(2)
//                }

                artistViewModel.getAllArtistFromSong(this, position)
                artistViewModel.artistInSong[position].observe(lifecycleOwner){
                    when(it){
                        is UiState.Loading -> {

                        }
                        is UiState.Failure -> {

                        }
                        is UiState.Success -> {
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

    @SuppressLint("NotifyDataSetChanged")
    fun setData(songList: List<OnlineSong>){
        this.songList = songList
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setDataForArtist(artistList: ArrayList<List<OnlineArtist>>){
        this.artistList = artistList
        notifyDataSetChanged()
    }

    interface ItemSongClickListener {
        fun callBackFromMenuSongClick(action: String, songList: List<OnlineSong>, position: Int)
        fun callBackFromSongClick(songList: List<OnlineSong>, position: Int)
    }

}