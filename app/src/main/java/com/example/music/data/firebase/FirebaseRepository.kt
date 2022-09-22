package com.example.music.data.firebase

import android.net.Uri
import com.example.music.UiState
import com.example.music.data.models.online.OnlineArtist
import com.example.music.data.models.online.OnlinePlaylist
import com.example.music.data.models.online.OnlineSong
import com.google.firebase.auth.FirebaseUser

interface FirebaseRepository {

    fun deleteSongInPlaylist(song: OnlineSong, playlist: OnlinePlaylist, user: FirebaseUser, result: (UiState<String>) -> Unit)

    fun addSongToPlaylist(song: OnlineSong, playlist: OnlinePlaylist, user: FirebaseUser, result: (UiState<String>) -> Unit)

    fun getAllSongs(result: (UiState<List<OnlineSong>>) -> Unit)

    fun getAllSongInPlaylist(playlist: OnlinePlaylist, result: (UiState<List<OnlineSong>>) -> Unit)

    fun getAllPlaylistOfUser(user: FirebaseUser, result: (UiState<List<OnlinePlaylist>>) -> Unit)

    fun addPlaylistForUser(playlist: OnlinePlaylist, user: FirebaseUser, result: (UiState<String>) -> Unit)

    fun updatePlaylistForUser(playlist: OnlinePlaylist, user: FirebaseUser, result: (UiState<String>) -> Unit)

    fun deletePlaylistForUser(playlist: OnlinePlaylist, user: FirebaseUser, result: (UiState<String>) -> Unit)

    fun getAllPlaylistOfSong(song: OnlineSong, user: FirebaseUser, result: (UiState<List<OnlinePlaylist>>) -> Unit)

    fun getAllArtists(result: (UiState<List<OnlineArtist>>) -> Unit)

    fun addArtist(artist: OnlineArtist, result: (UiState<String>) -> Unit)

    fun updateArtist(artist: OnlineArtist, result: (UiState<String>) -> Unit)

    fun deleteArtist(artist: OnlineArtist, result: (UiState<String>) -> Unit)

    fun addSong(song: OnlineSong, result: (UiState<String>) -> Unit)

    fun deleteSong(song: OnlineSong, result: (UiState<String>) -> Unit)

    fun updateSong(song: OnlineSong, result: (UiState<String>) -> Unit)

    suspend fun uploadSingleSongFile(fileName: String, fileUri: Uri, result: (UiState<Uri>) -> Unit)

    suspend fun uploadSingleImageFile(directory: String, fileName: String, fileUri: Uri, result: (UiState<Uri>) -> Unit)

}