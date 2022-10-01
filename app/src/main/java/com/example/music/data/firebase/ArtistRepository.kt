package com.example.music.data.firebase

import android.net.Uri
import com.example.music.UiState
import com.example.music.data.models.online.OnlineArtist
import com.example.music.data.models.online.OnlinePlaylist
import com.example.music.data.models.online.OnlineSong
import com.google.firebase.auth.FirebaseUser

interface ArtistRepository {

    fun getAllArtists(result: (UiState<List<OnlineArtist>>) -> Unit)

    fun addArtist(artist: OnlineArtist, result: (UiState<String>) -> Unit)

    fun updateArtist(artist: OnlineArtist, result: (UiState<String>) -> Unit)

    fun deleteArtist(artist: OnlineArtist, result: (UiState<String>) -> Unit)

    fun getAllArtistFromSong(song: OnlineSong, result: (UiState<List<OnlineArtist>>) -> Unit)

}