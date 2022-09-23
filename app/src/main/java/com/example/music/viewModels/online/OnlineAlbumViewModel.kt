package com.example.music.viewModels.online

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.music.UiState
import com.example.music.data.firebase.AlbumRepository
import com.example.music.data.firebase.ArtistRepository
import com.example.music.data.firebase.FirebaseRepository
import com.example.music.data.models.online.OnlineAlbum
import com.example.music.data.models.online.OnlineArtist
import com.example.music.data.models.online.OnlinePlaylist
import com.example.music.data.models.online.OnlineSong
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class OnlineAlbumViewModel @Inject constructor(val repository: AlbumRepository): ViewModel(){

    private val _addAlbum = MutableLiveData<UiState<String>>()
    val addAlbum: LiveData<UiState<String>> get() = _addAlbum

    private val _deleteAlbum = MutableLiveData<UiState<String>>()
    val deleteAlbum: LiveData<UiState<String>> get() = _deleteAlbum

    private val _updateAlbum = MutableLiveData<UiState<String>>()
    val updateAlbum: LiveData<UiState<String>> get() = _updateAlbum

    private val _albums = MutableLiveData<UiState<List<OnlineAlbum>>>()
    val album: LiveData<UiState<List<OnlineAlbum>>> get() = _albums

    fun getAllAlbums() {
        _albums.value = UiState.Loading
        repository.getAllAlbums {
            _albums.value = it
        }
    }

    fun addAlbum(album: OnlineAlbum){
        _addAlbum.value = UiState.Loading
        repository.addAlbum(album){
            _addAlbum.value = it
        }
    }

    fun deleteAlbum(album: OnlineAlbum){
        _deleteAlbum.value = UiState.Loading
        repository.deleteAlbum(album){
            _deleteAlbum.value = it
        }
    }

    fun updateAlbum(album: OnlineAlbum){
        _updateAlbum.value = UiState.Loading
        repository.updateAlbum(album){
            _updateAlbum.value = it
        }
    }

}