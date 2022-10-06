package com.example.music.online.data.dao

import com.example.music.utils.UiState
import com.example.music.online.data.models.OnlineAlbum
import com.example.music.online.data.models.OnlineComment
import com.example.music.online.data.models.OnlineSong

interface CommentRepository {

    fun getAllComments(result: (UiState<List<OnlineComment>>) -> Unit)

    fun getAllCommentForSong(song: OnlineSong, result: (UiState<List<OnlineComment>>) -> Unit)

    fun addComment(comment: OnlineComment, result: (UiState<String>) -> Unit)

    fun updateComment(comment: OnlineComment, result: (UiState<String>) -> Unit)

    fun deleteComment(comment: OnlineComment, result: (UiState<String>) -> Unit)

}