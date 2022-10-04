package com.example.music.online.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.music.utils.UiState
import com.example.music.online.data.dao.GenreRepository
import com.example.music.online.data.models.OnlineGenre
import dagger.hilt.android.lifecycle.HiltViewModel
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