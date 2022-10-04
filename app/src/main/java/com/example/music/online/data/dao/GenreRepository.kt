package com.example.music.online.data.dao

import com.example.music.utils.UiState
import com.example.music.online.data.models.OnlineGenre

interface GenreRepository {

    fun getAllGenres(result: (UiState<List<OnlineGenre>>) -> Unit)

    fun addGenre(genre: OnlineGenre, result: (UiState<String>) -> Unit)

    fun updateGenre(genre: OnlineGenre, result: (UiState<String>) -> Unit)

    fun deleteGenre(genre: OnlineGenre, result: (UiState<String>) -> Unit)

}