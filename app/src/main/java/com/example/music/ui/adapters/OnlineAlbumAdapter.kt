package com.example.music.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.music.R
import com.example.music.data.models.online.*
import com.example.music.databinding.SongRowItemBinding
import com.example.music.databinding.ColumnItemBinding

class OnlineAlbumAdapter(val context: Context)
    : RecyclerView.Adapter<OnlineAlbumAdapter.ViewHolder>() {

    var album = emptyList<OnlineAlbum>()

    inner class ViewHolder(val binding: ColumnItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ColumnItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return album.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        with(holder){

            with(album[position]){
                binding.itemName.text = this.name
                val imgUrl = this.imgFilePath
                if (imgUrl!!.isNotEmpty()){
                    Glide.with(context).load(imgUrl).into(binding.itemImg)
                }
                else {
                    binding.itemImg.setImageResource(R.drawable.ic_baseline_album_24)
                }
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(album: List<OnlineAlbum>){
        this.album = album
        notifyDataSetChanged()
    }

}