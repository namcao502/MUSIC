package com.example.music.online.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.music.R
import com.example.music.databinding.ColumnItemBinding
import com.example.music.online.data.models.OnlineAlbum
import com.example.music.online.data.models.OnlineCountry

class OnlineCountryAdapter(val context: Context, private val clickACountry: ClickACountry)
    : RecyclerView.Adapter<OnlineCountryAdapter.ViewHolder>() {

    var countries = emptyList<OnlineCountry>()

    inner class ViewHolder(val binding: ColumnItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ColumnItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return countries.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        with(holder){
            itemView.setOnClickListener {
                clickACountry.callBackFromCountryClick(countries[position])
            }
            with(countries[position]){
                binding.itemName.text = this.name
                val imgUrl = this.imgFilePath
                if (imgUrl!!.isNotEmpty()){
                    Glide.with(context).load(imgUrl).into(binding.itemImg)
                }
                else {
                    binding.itemImg.setImageResource(R.drawable.ic_baseline_location_city_24)
                }
            }
        }

    }

    interface ClickACountry{
        fun callBackFromCountryClick(country: OnlineCountry)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(countries: List<OnlineCountry>){
        this.countries = countries
        notifyDataSetChanged()
    }
}