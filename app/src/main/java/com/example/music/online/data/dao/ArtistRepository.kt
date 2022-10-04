package com.example.music.online.data.dao

import com.example.music.utils.UiState
import com.example.music.online.data.models.OnlineArtist
import com.example.music.online.data.models.OnlineSong

interface ArtistRepository {

    fun getAllArtists(result: (UiState<List<OnlineArtist>>) -> Unit)

    fun addArtist(artist: OnlineArtist, result: (UiState<String>) -> Unit)

    fun updateArtist(artist: OnlineArtist, result: (UiState<String>) -> Unit)

    fun deleteArtist(artist: OnlineArtist, result: (UiState<String>) -> Unit)

    fun getAllArtistFromSong(song: OnlineSong, result: (UiState<List<OnlineArtist>>) -> Unit)

}