package com.example.music.data.firebase

import android.net.Uri
import com.example.music.UiState
import com.example.music.data.models.online.OnlineArtist
import com.example.music.data.models.online.OnlinePlaylist
import com.example.music.data.models.online.OnlineSong
import com.google.firebase.auth.FirebaseUser

interface FirebaseRepository {

    fun getAllSongs(result: (UiState<List<OnlineSong>>) -> Unit)

    fun getSongFromListSongID(songs: List<String>, result: (UiState<List<OnlineSong>>) -> Unit)

    suspend fun uploadSingleSongFile(fileName: String, fileUri: Uri, result: (UiState<Uri>) -> Unit)

    suspend fun uploadSingleImageFile(directory: String, fileName: String, fileUri: Uri, result: (UiState<Uri>) -> Unit)

}