package com.example.music.online.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.music.R
import com.example.music.online.data.models.OnlineArtist
import com.example.music.databinding.ColumnItemBinding

class OnlineArtistAdapter(val context: Context, private val clickAnArtist: ClickAnArtist)
    : RecyclerView.Adapter<OnlineArtistAdapter.ViewHolder>() {

    var artist = emptyList<OnlineArtist>()

    inner class ViewHolder(val binding: ColumnItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ColumnItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return artist.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        with(holder){
            itemView.setOnClickListener {
                clickAnArtist.callBackFromArtistClick(artist[position])
            }
            with(artist[position]){
                binding.itemName.text = this.name
                val imgUrl = this.imgFilePath
                if (imgUrl!!.isNotEmpty()){
                    Glide.with(context).load(imgUrl).into(binding.itemImg)
                }
                else {
                    binding.itemImg.setImageResource(R.drawable.ic_baseline_people_outline_24)
                }
            }
        }

    }

    interface ClickAnArtist{
        fun callBackFromArtistClick(artist: OnlineArtist)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(artist: List<OnlineArtist>){
        this.artist = artist
        notifyDataSetChanged()
    }

}