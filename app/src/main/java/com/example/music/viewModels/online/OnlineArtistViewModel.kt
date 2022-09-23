package com.example.music.viewModels.online

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.music.UiState
import com.example.music.data.firebase.ArtistRepository
import com.example.music.data.firebase.FirebaseRepository
import com.example.music.data.models.online.OnlineArtist
import com.example.music.data.models.online.OnlinePlaylist
import com.example.music.data.models.online.OnlineSong
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class OnlineArtistViewModel @Inject constructor(val repository: ArtistRepository): ViewModel(){

    private val _addArtist = MutableLiveData<UiState<String>>()
    val addArtist: LiveData<UiState<String>> get() = _addArtist

    private val _deleteArtist = MutableLiveData<UiState<String>>()
    val deleteArtist: LiveData<UiState<String>> get() = _deleteArtist

    private val _updateArtist = MutableLiveData<UiState<String>>()
    val updateArtist: LiveData<UiState<String>> get() = _updateArtist

    private val _artists = MutableLiveData<UiState<List<OnlineArtist>>>()
    val artist: LiveData<UiState<List<OnlineArtist>>> get() = _artists

    fun getAllArtists() {
        _artists.value = UiState.Loading
        repository.getAllArtists {
            _artists.value = it
        }
    }

    fun addArtist(artist: OnlineArtist){
        _addArtist.value = UiState.Loading
        repository.addArtist(artist){
            _addArtist.value = it
        }
    }

    fun deleteArtist(artist: OnlineArtist){
        _deleteArtist.value = UiState.Loading
        repository.deleteArtist(artist){
            _deleteArtist.value = it
        }
    }

    fun updateArtist(artist: OnlineArtist){
        _updateArtist.value = UiState.Loading
        repository.updateArtist(artist){
            _updateArtist.value = it
        }
    }

}