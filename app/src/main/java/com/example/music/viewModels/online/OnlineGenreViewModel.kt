package com.example.music.viewModels.online

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.music.UiState
import com.example.music.data.firebase.ArtistRepository
import com.example.music.data.firebase.FirebaseRepository
import com.example.music.data.firebase.GenreRepository
import com.example.music.data.models.online.OnlineArtist
import com.example.music.data.models.online.OnlineGenre
import com.example.music.data.models.online.OnlinePlaylist
import com.example.music.data.models.online.OnlineSong
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class OnlineGenreViewModel @Inject constructor(val repository: GenreRepository): ViewModel(){

    private val _addGenre = MutableLiveData<UiState<String>>()
    val addGenre: LiveData<UiState<String>> get() = _addGenre

    private val _deleteGenre = MutableLiveData<UiState<String>>()
    val deleteGenre: LiveData<UiState<String>> get() = _deleteGenre

    private val _updateGenre = MutableLiveData<UiState<String>>()
    val updateGenre: LiveData<UiState<String>> get() = _updateGenre

    private val _genres = MutableLiveData<UiState<List<OnlineGenre>>>()
    val genre: LiveData<UiState<List<OnlineGenre>>> get() = _genres

    fun getAllGenres() {
        _genres.value = UiState.Loading
        repository.getAllGenres {
            _genres.value = it
        }
    }

    fun addGenre(genre: OnlineGenre){
        _addGenre.value = UiState.Loading
        repository.addGenre(genre){
            _addGenre.value = it
        }
    }

    fun deleteGenre(genre: OnlineGenre){
        _deleteGenre.value = UiState.Loading
        repository.deleteGenre(genre){
            _deleteGenre.value = it
        }
    }

    fun updateGenre(genre: OnlineGenre){
        _updateGenre.value = UiState.Loading
        repository.updateGenre(genre){
            _updateGenre.value = it
        }
    }

}