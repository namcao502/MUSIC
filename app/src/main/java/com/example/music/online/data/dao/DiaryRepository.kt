package com.example.music.online.data.dao

import com.example.music.online.data.models.OnlineDiary
import com.example.music.utils.UiState
import com.example.music.online.data.models.OnlinePlaylist
import com.example.music.online.data.models.OnlineSong
import com.google.firebase.auth.FirebaseUser

interface DiaryRepository {

    fun getAllDiaries(user: FirebaseUser, result: (UiState<List<OnlineDiary>>) -> Unit)

    fun addDiary(diary: OnlineDiary, user: FirebaseUser, result: (UiState<String>) -> Unit)

    fun updateDiary(diary: OnlineDiary, user: FirebaseUser, result: (UiState<String>) -> Unit)

    fun deleteDiary(diary: OnlineDiary, user: FirebaseUser, result: (UiState<String>) -> Unit)

}