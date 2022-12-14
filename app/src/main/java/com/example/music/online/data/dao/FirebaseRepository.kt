package com.example.music.online.data.dao

import android.content.Context
import android.net.Uri
import com.example.music.utils.UiState
import com.example.music.online.data.models.OnlineSong

interface FirebaseRepository {

    fun getAllSongs(result: (UiState<List<OnlineSong>>) -> Unit)

    fun getSongFromListSongID(songs: List<String>, result: (UiState<List<OnlineSong>>) -> Unit)

    fun getSongFromSongID(songId: String, result: (UiState<OnlineSong>) -> Unit)

    suspend fun uploadSingleSongFile(fileName: String, fileUri: Uri, result: (UiState<Uri>) -> Unit)

    suspend fun uploadSingleImageFile(directory: String, fileName: String, fileUri: Uri, result: (UiState<Uri>) -> Unit)

    fun downloadSingleSongFile(context: Context, fileName: String, filePath: String, result: (UiState<String>) -> Unit)

    fun updateModelById(name: String, id: String, listSong: ArrayList<String>, result: (UiState<String>) -> Unit)


}