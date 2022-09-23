package com.example.music.data.firebase

import android.net.Uri
import com.example.music.UiState
import com.example.music.data.models.online.OnlineArtist
import com.example.music.data.models.online.OnlineGenre
import com.example.music.data.models.online.OnlinePlaylist
import com.example.music.data.models.online.OnlineSong
import com.google.firebase.auth.FirebaseUser

interface GenreRepository {

    fun getAllGenres(result: (UiState<List<OnlineGenre>>) -> Unit)

    fun addGenre(genre: OnlineGenre, result: (UiState<String>) -> Unit)

    fun updateGenre(genre: OnlineGenre, result: (UiState<String>) -> Unit)

    fun deleteGenre(genre: OnlineGenre, result: (UiState<String>) -> Unit)

}