package com.example.music.online.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.music.R
import com.example.music.databinding.CommentRowItemBinding
import com.example.music.online.data.models.OnlineComment

class CommentDialogAdapter(val context: Context, private val clickAComment: ClickAComment)
    : RecyclerView.Adapter<CommentDialogAdapter.ViewHolder>() {

    var commentList = emptyList<OnlineComment>()

    inner class ViewHolder(val binding: CommentRowItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CommentRowItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        with(holder){

            binding.menuBtn.setOnClickListener {
                PopupMenu(context, binding.menuBtn).apply {
                    menuInflater.inflate(R.menu.row_comment_menu, this.menu)
                    setOnMenuItemClickListener { menuItem ->
                        clickAComment.callBackFromMenuClickComment(menuItem.title.toString(), commentList[position])
                        true
                    }
                    // Showing the popup menu
                    show()
                }
            }

            with(commentList[position]){
                binding.titleTxt.text = message
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(commentList: List<OnlineComment>){
        this.commentList = commentList
        notifyDataSetChanged()
    }

    interface ClickAComment{
        fun callBackFromMenuClickComment(action: String, comment: OnlineComment)
    }

}