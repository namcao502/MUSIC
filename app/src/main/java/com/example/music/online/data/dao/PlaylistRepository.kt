package com.example.music.online.data.dao

import com.example.music.utils.UiState
import com.example.music.online.data.models.OnlinePlaylist
import com.example.music.online.data.models.OnlineSong
import com.google.firebase.auth.FirebaseUser

interface PlaylistRepository {

    fun getAllSongInPlaylist(playlist: OnlinePlaylist, result: (UiState<List<OnlineSong>>) -> Unit)

    fun deleteSongInPlaylist(song: OnlineSong, playlist: OnlinePlaylist, user: FirebaseUser, result: (UiState<String>) -> Unit)

    fun getAllPlaylistOfUser(user: FirebaseUser, result: (UiState<List<OnlinePlaylist>>) -> Unit)

    fun addPlaylistForUser(playlist: OnlinePlaylist, user: FirebaseUser, result: (UiState<String>) -> Unit)

    fun updatePlaylistForUser(playlist: OnlinePlaylist, user: FirebaseUser, result: (UiState<String>) -> Unit)

    fun deletePlaylistForUser(playlist: OnlinePlaylist, user: FirebaseUser, result: (UiState<String>) -> Unit)

    fun getAllPlaylists(result: (UiState<List<OnlinePlaylist>>) -> Unit)

    fun addPlaylist(playlist: OnlinePlaylist, result: (UiState<String>) -> Unit)

    fun updatePlaylist(playlist: OnlinePlaylist, result: (UiState<String>) -> Unit)

    fun deletePlaylist(playlist: OnlinePlaylist, result: (UiState<String>) -> Unit)

}