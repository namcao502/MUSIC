package com.example.music.online.data.dao

import com.example.music.utils.UiState
import com.example.music.online.data.models.OnlinePlaylist
import com.example.music.online.data.models.OnlineSong
import com.google.firebase.auth.FirebaseUser

interface SongRepository {

    fun addSongToPlaylist(song: OnlineSong, playlist: OnlinePlaylist, user: FirebaseUser, result: (UiState<String>) -> Unit)

    fun getAllSongs(result: (UiState<List<OnlineSong>>) -> Unit)

    fun addSong(song: OnlineSong, result: (UiState<String>) -> Unit)

    fun deleteSong(song: OnlineSong, result: (UiState<String>) -> Unit)

    fun updateSong(song: OnlineSong, result: (UiState<String>) -> Unit)

    fun countSong(result: (UiState<Int>) -> Unit)

    fun updateViewForSong(song: OnlineSong, result: (UiState<String>) -> Unit)

    fun getAllSongForSearch(result: (UiState<List<OnlineSong>>) -> Unit)

}