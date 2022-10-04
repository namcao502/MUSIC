package com.example.music.online.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.music.R
import com.example.music.online.data.models.OnlineGenre
import com.example.music.databinding.ColumnItemBinding

class OnlineGenreAdapter(val context: Context, val clickAGenre: ClickAGenre)
    : RecyclerView.Adapter<OnlineGenreAdapter.ViewHolder>() {

    var genre = emptyList<OnlineGenre>()

    inner class ViewHolder(val binding: ColumnItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ColumnItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return genre.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        with(holder){
            itemView.setOnClickListener {
                clickAGenre.callBackFromGenreClick(genre[position])
            }
            with(genre[position]){
                binding.itemName.text = this.name
                val imgUrl = this.imgFilePath
                if (imgUrl!!.isNotEmpty()){
                    Glide.with(context).load(imgUrl).into(binding.itemImg)
                }
                else {
                    binding.itemImg.setImageResource(R.drawable.ic_baseline_category_24)
                }
            }
        }

    }

    interface ClickAGenre{
        fun callBackFromGenreClick(genre: OnlineGenre)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(genre: List<OnlineGenre>){
        this.genre = genre
        notifyDataSetChanged()
    }

}