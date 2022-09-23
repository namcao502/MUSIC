package com.example.music.data.firebase

import android.net.Uri
import com.example.music.UiState
import com.example.music.data.models.online.OnlineAlbum
import com.example.music.data.models.online.OnlineArtist
import com.example.music.data.models.online.OnlinePlaylist
import com.example.music.data.models.online.OnlineSong
import com.google.firebase.auth.FirebaseUser

interface AlbumRepository {

    fun getAllAlbums(result: (UiState<List<OnlineAlbum>>) -> Unit)

    fun addAlbum(album: OnlineAlbum, result: (UiState<String>) -> Unit)

    fun updateAlbum(album: OnlineAlbum, result: (UiState<String>) -> Unit)

    fun deleteAlbum(album: OnlineAlbum, result: (UiState<String>) -> Unit)

}