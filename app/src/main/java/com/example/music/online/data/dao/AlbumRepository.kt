package com.example.music.online.data.dao

import com.example.music.utils.UiState
import com.example.music.online.data.models.OnlineAlbum

interface AlbumRepository {

    fun getAllAlbums(result: (UiState<List<OnlineAlbum>>) -> Unit)

    fun addAlbum(album: OnlineAlbum, result: (UiState<String>) -> Unit)

    fun updateAlbum(album: OnlineAlbum, result: (UiState<String>) -> Unit)

    fun deleteAlbum(album: OnlineAlbum, result: (UiState<String>) -> Unit)

}