package com.example.music.offline.ui.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.Settings.Global
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.music.R
import com.example.music.databinding.SongRowItemBinding
import com.example.music.offline.data.models.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SongAdapter(private val context: Context, private val itemClickListener: ItemSongClickListener): RecyclerView.Adapter<SongAdapter.ViewHolder>() {

    var songList = emptyList<Song>()

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
                itemClickListener.callBackFromSongClick(songList, position)
            }

            binding.menuBtn.setOnClickListener {

                PopupMenu(context, binding.menuBtn).apply {
                    menuInflater.inflate(R.menu.row_song_menu, this.menu)
                    setOnMenuItemClickListener { menuItem ->
                        itemClickListener.callBackFromMenuSongClick(menuItem.title.toString(), songList, position)
                        true
                    }
                    // Showing the popup menu
                    show()
                }
            }

            with(songList[position]){
                binding.titleTxt.text = this.name
                val minutes = this.duration / 1000 / 60
                val seconds = this.duration / 1000 % 60
                if (seconds < 10){
                    binding.lengthTxt.text = "$minutes:0$seconds"
                }
                else {
                    binding.lengthTxt.text = "$minutes:$seconds"
                }

                binding.authorTxt.text = this.artists

//                val image = try {
//                    Glide.with(context).asBitmap().load(uri).submit().get()
//                } catch (e: Exception){
//                    BitmapFactory.decodeResource(context.resources, R.drawable.music_default)
//                }
//                Glide.with(context).load(uri).into(binding.imageView)
//                binding.imageView.setImageBitmap(image)

                GlobalScope.launch {
                    val albumId: String = this@with.album_id
                    val albumUri: Uri = Uri.parse("content://media/external/audio/albumart")
                    val uri: Uri = ContentUris.withAppendedId(albumUri, albumId.toLong())
                    val image = withContext(Dispatchers.IO) {
                        try {
                            Glide.with(context).asBitmap().load(uri).submit().get()
                        }
                        catch (e: Exception){
                            BitmapFactory.decodeResource(context.resources, R.drawable.icons8_music_note_default_68)
                        }
                    }
                    (context as Activity).runOnUiThread {
                        binding.imageView.setImageBitmap(image)
                    }
                }

            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(songList: List<Song>){
        this.songList = songList
        notifyDataSetChanged()
    }

    interface ItemSongClickListener {
        fun callBackFromMenuSongClick(action: String, songList: List<Song>, position: Int)
        fun callBackFromSongClick(songList: List<Song>, position: Int)
    }

}