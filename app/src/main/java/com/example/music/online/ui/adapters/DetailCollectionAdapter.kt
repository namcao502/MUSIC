package com.example.music.online.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
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
import com.example.music.online.data.models.OnlineSong
import com.example.music.online.viewModels.OnlineArtistViewModel
import com.example.music.utils.PopupMenuCustomLayout
import com.example.music.utils.UiState
class DetailCollectionAdapter(
    private val context: Context,
    private val clickASong: ClickASong,
    private val artistViewModel: OnlineArtistViewModel,
    private val lifecycleOwner: LifecycleOwner
): RecyclerView.Adapter<DetailCollectionAdapter.ViewHolder>() {

    var songList = emptyList<OnlineSong>()
//    private var artistList: ArrayList<String> = ArrayList()
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

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {

        with(holder){
            itemView.setOnClickListener {
                clickASong.callBackFromDetailClick(songList, position)
            }

            binding.menuBtn.setOnClickListener {
                val popupMenu = PopupMenuCustomLayout(
                    context, R.layout.custom_popup_menu_song_detail,
                    object : PopupMenuCustomLayout.PopupMenuCustomOnClickListener {
                        override fun onClick(menuItemId: Int) {
                            when (menuItemId) {
                                R.id.popup_menu_custom_item_a -> {
                                    clickASong.callBackFromMenuDetailClick("Play", songList, position)
                                }
                                R.id.popup_menu_custom_item_b -> {
                                    clickASong.callBackFromMenuDetailClick("Add to playlist", songList, position)
                                }
                                R.id.popup_menu_custom_item_c -> {
                                    clickASong.callBackFromMenuDetailClick("Delete", songList, position)
                                }
                            }
                        }
                    })
                popupMenu.show(binding.menuBtn, Gravity.BOTTOM, 0, 0)
//                val wrapper: Context = ContextThemeWrapper(context, R.style.PopupMenu)
//                PopupMenu(wrapper, binding.menuBtn).apply {
//                    menuInflater.inflate(R.menu.row_song_menu, this.menu)
//                    setOnMenuItemClickListener { menuItem ->
//                        clickASong.callBackFromMenuDetailClick(menuItem.title.toString(), songList, position)
//                        true
//                    }
//                    // Showing the popup menu
//                    try {
//                        val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
//                        fieldMPopup.isAccessible = true
//                        val mPopup = fieldMPopup.get(this)
//                        mPopup.javaClass
//                            .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
//                            .invoke(mPopup, true)
//                    } catch (_: Exception){
//
//                    } finally {
//                        show()
//                    }
//                }
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

                artistViewModel.getAllArtistFromSong2(this, position)
                artistViewModel.artistInSong2[position].observe(lifecycleOwner){
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

    interface ClickASong{
        fun callBackFromDetailClick(songList: List<OnlineSong>, position: Int)
        fun callBackFromMenuDetailClick(action: String, songList: List<OnlineSong>, position: Int)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(songList: List<OnlineSong>){
        this.songList = songList
        notifyDataSetChanged()
    }
//
//    @SuppressLint("NotifyDataSetChanged")
//    fun setDataForArtist(artistList: ArrayList<String>){
//        this.artistList = artistList
//        notifyDataSetChanged()
//    }

}