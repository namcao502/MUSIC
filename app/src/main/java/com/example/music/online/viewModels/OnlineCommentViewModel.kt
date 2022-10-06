package com.example.music.online.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.music.utils.UiState
import com.example.music.online.data.dao.AlbumRepository
import com.example.music.online.data.dao.CommentRepository
import com.example.music.online.data.models.OnlineAlbum
import com.example.music.online.data.models.OnlineComment
import com.example.music.online.data.models.OnlineSong
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class OnlineCommentViewModel @Inject constructor(val repository: CommentRepository): ViewModel(){

    private val _addComment = MutableLiveData<UiState<String>>()
    val addComment: LiveData<UiState<String>> get() = _addComment

    private val _deleteComment = MutableLiveData<UiState<String>>()
    val deleteComment: LiveData<UiState<String>> get() = _deleteComment

    private val _updateComment = MutableLiveData<UiState<String>>()
    val updateComment: LiveData<UiState<String>> get() = _updateComment

    private val _comment = MutableLiveData<UiState<List<OnlineComment>>>()
    val comment: LiveData<UiState<List<OnlineComment>>> get() = _comment

    fun getAllCommentForSong(onlineSong: OnlineSong) {
        _comment.value = UiState.Loading
        repository.getAllCommentForSong(onlineSong) {
            _comment.value = it
        }
    }

    fun getAllComments() {
        _comment.value = UiState.Loading
        repository.getAllComments {
            _comment.value = it
        }
    }

    fun addComment(comment: OnlineComment){
        _addComment.value = UiState.Loading
        repository.addComment(comment){
            _addComment.value = it
        }
    }

    fun deleteComment(comment: OnlineComment){
        _deleteComment.value = UiState.Loading
        repository.deleteComment(comment){
            _deleteComment.value = it
        }
    }

    fun updateComment(comment: OnlineComment){
        _updateComment.value = UiState.Loading
        repository.updateComment(comment){
            _updateComment.value = it
        }
    }

}