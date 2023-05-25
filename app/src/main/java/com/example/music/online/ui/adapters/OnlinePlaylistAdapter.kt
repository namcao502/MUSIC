package com.example.music.online.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.music.R
import com.example.music.databinding.OnlinePlaylistRowItemBinding
import com.example.music.databinding.PlaylistRowItemBinding
import com.example.music.online.data.models.OnlinePlaylist
import com.example.music.utils.PopupMenuCustomLayout

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

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {

        with(holder){

            itemView.setOnClickListener {
                clickAPlaylist.callBackFromPlaylistClick(playlist[position])
            }

            binding.menuBtn.setOnClickListener {
                val popupMenu = PopupMenuCustomLayout(
                    context, R.layout.custom_popup_menu_playlist,
                    object : PopupMenuCustomLayout.PopupMenuCustomOnClickListener {
                        override fun onClick(menuItemId: Int) {
                            when (menuItemId) {
                                R.id.popup_menu_custom_item_a -> {
                                    clickAPlaylist.callBackFromMenuPlaylistClick("Rename", playlist[position])
                                }
                                R.id.popup_menu_custom_item_b -> {
                                    clickAPlaylist.callBackFromMenuPlaylistClick("Delete", playlist[position])
                                }
                            }
                        }
                    })
                popupMenu.show(binding.menuBtn, Gravity.CENTER, 0, 0)
//                val wrapper: Context = ContextThemeWrapper(context, R.style.PopupMenu)
//                PopupMenu(wrapper, binding.menuBtn).apply {
//                    menuInflater.inflate(R.menu.row_playlist_menu, this.menu)
//                    setOnMenuItemClickListener { menuItem ->
//                        clickAPlaylist.callBackFromMenuPlaylistClick(menuItem.title.toString(), playlist[position])
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

    interface ClickAPlaylist {
        fun callBackFromMenuPlaylistClick(action: String, playlist: OnlinePlaylist)
        fun callBackFromPlaylistClick(playlist: OnlinePlaylist)
    }
}