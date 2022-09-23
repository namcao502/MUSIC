package com.example.music.data.firebase

import android.net.Uri
import com.example.music.UiState
import com.example.music.data.models.online.OnlineArtist
import com.example.music.data.models.online.OnlinePlaylist
import com.example.music.data.models.online.OnlineSong
import com.google.firebase.auth.FirebaseUser

interface SongRepository {

    fun addSongToPlaylist(song: OnlineSong, playlist: OnlinePlaylist, user: FirebaseUser, result: (UiState<String>) -> Unit)

    fun getAllSongs(result: (UiState<List<OnlineSong>>) -> Unit)

    fun addSong(song: OnlineSong, result: (UiState<String>) -> Unit)

    fun deleteSong(song: OnlineSong, result: (UiState<String>) -> Unit)

    fun updateSong(song: OnlineSong, result: (UiState<String>) -> Unit)

}