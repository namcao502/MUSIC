package com.example.music.data.firebase

import com.example.music.UiState
import com.example.music.models.OnlinePlaylist
import com.example.music.models.OnlineSong
import com.google.firebase.auth.FirebaseUser

interface FirebaseRepository {

    fun deleteSongInPlaylist(song: OnlineSong, playlist: OnlinePlaylist, user: FirebaseUser)

    fun addSongToPlaylist(song: OnlineSong, playlist: OnlinePlaylist, user: FirebaseUser)

    fun getAllSongs(result: (UiState<List<OnlineSong>>) -> Unit)

    fun getAllSongInPlaylist(playlist: OnlinePlaylist)

    fun getAllPlaylistOfUser(user: FirebaseUser, result: (UiState<List<OnlinePlaylist>>) -> Unit)

    fun addPlaylistForUser(playlist: OnlinePlaylist, user: FirebaseUser)

    fun updatePlaylistForUser(playlist: OnlinePlaylist, user: FirebaseUser)

    fun deletePlaylistForUser(playlist: OnlinePlaylist, user: FirebaseUser)

}