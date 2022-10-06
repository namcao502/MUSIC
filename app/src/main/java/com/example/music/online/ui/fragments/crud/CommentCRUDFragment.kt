package com.example.music.online.ui.fragments.crud

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import com.example.music.R
import com.example.music.databinding.FragmentAlbumCrudBinding
import com.example.music.databinding.FragmentCommentCrudBinding
import com.example.music.online.data.models.OnlineAlbum
import com.example.music.online.data.models.OnlineComment
import com.example.music.online.viewModels.OnlineCommentViewModel
import com.example.music.utils.UiState
import com.example.music.utils.createProgressDialog
import com.example.music.utils.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommentCRUDFragment : Fragment() {

    private var _binding: FragmentCommentCrudBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var currentComment: OnlineComment? = null

    private val onlineCommentViewModel: OnlineCommentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCommentCrudBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var comments: List<OnlineComment> = emptyList()

        onlineCommentViewModel.getAllComments()
        onlineCommentViewModel.comment.observe(viewLifecycleOwner){
            when(it){
                is UiState.Loading -> {

                }
                is UiState.Failure -> {

                }
                is UiState.Success -> {
                    comments = it.data
                    with(binding.listView){
                        adapter = ArrayAdapter(requireContext(),
                            androidx.appcompat.R.layout.
                            support_simple_spinner_dropdown_item, comments)
                    }
                }
            }
        }

        binding.listView.setOnItemClickListener { _, _, i, _ ->
            currentComment = comments[i]
            binding.messageEt.setText(currentComment!!.message)
        }

        binding.deleteBtn.setOnClickListener {
            if (currentComment == null){
                toast("Please pick a comment to delete...")
                return@setOnClickListener
            }
            val progressDialog = createProgressDialog("Deleting a comment...")
            onlineCommentViewModel.deleteComment(currentComment!!)
            onlineCommentViewModel.deleteComment.observe(viewLifecycleOwner){
                when (it) {
                    is UiState.Loading -> {
                        progressDialog.show()
                    }
                    is UiState.Failure -> {
                        progressDialog.cancel()
                        toast("$it")
                    }
                    is UiState.Success -> {
                        progressDialog.cancel()
                        toast(it.data)
                        currentComment = null
                    }
                }
            }
        }

        binding.updateBtn.setOnClickListener {
            if (currentComment == null){
                toast("Please pick a comment to update...")
                return@setOnClickListener
            }

            val message = binding.messageEt.text.toString()
            if (message.isEmpty()){
                toast("Please type something...")
                return@setOnClickListener
            }

            currentComment!!.message = message
            val progressDialog = createProgressDialog("Updating a comment...")
            onlineCommentViewModel.updateComment(currentComment!!)
            onlineCommentViewModel.updateComment.observe(viewLifecycleOwner){
                when (it) {
                    is UiState.Loading -> {
                        progressDialog.show()
                    }
                    is UiState.Failure -> {
                        progressDialog.cancel()
                        toast("$it")
                    }
                    is UiState.Success -> {
                        progressDialog.cancel()
                        toast(it.data)
                        currentComment = null
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}