package com.example.music.online.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.music.databinding.DiaryRowItemBinding
import com.example.music.online.data.models.OnlineDiary

class OnlineDiaryAdapter (private val clickADiary: ClickADiary)
    : RecyclerView.Adapter<OnlineDiaryAdapter.ViewHolder>() {

    var diaries = emptyList<OnlineDiary>()

    inner class ViewHolder(val binding: DiaryRowItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DiaryRowItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return diaries.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        with(holder){

            itemView.setOnClickListener {
                clickADiary.callBackFromDiaryClick(diaries[position])
            }

            binding.deleteBtn.setOnClickListener {
                //delete a diary
                clickADiary.callBackFromDeleteDiaryClick(diaries[position])
            }

            binding.titleTxt.text = diaries[position].subject
            binding.datetimeTxt.text = diaries[position].dateTime
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(diaries: List<OnlineDiary>){
        this.diaries = diaries
        notifyDataSetChanged()
    }

    interface ClickADiary {
        fun callBackFromDiaryClick(diary: OnlineDiary)
        fun callBackFromDeleteDiaryClick(diary: OnlineDiary)
    }
}