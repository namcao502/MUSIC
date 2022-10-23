package com.example.music.online.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.music.utils.UiState
import com.example.music.online.data.dao.ArtistRepository
import com.example.music.online.data.models.OnlineArtist
import com.example.music.online.data.models.OnlineSong
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val _artistFromSongID = MutableLiveData<UiState<List<OnlineArtist>>>()
    val artistFromSongID: LiveData<UiState<List<OnlineArtist>>> get() = _artistFromSongID

    private var _artistInSong: List<MutableLiveData<UiState<List<OnlineArtist>>>>
            = List(100, init={MutableLiveData<UiState<List<OnlineArtist>>>()})

    var artistInSong: List<LiveData<UiState<List<OnlineArtist>>>> = _artistInSong

    private var _artistInSong2: List<MutableLiveData<UiState<List<OnlineArtist>>>>
            = List(100, init={MutableLiveData<UiState<List<OnlineArtist>>>()})

    var artistInSong2: List<LiveData<UiState<List<OnlineArtist>>>> = _artistInSong2

    fun getAllArtists() {
        _artists.value = UiState.Loading
        repository.getAllArtists {
            _artists.value = it
        }
    }

    fun getAllArtistFromSong(song: OnlineSong, position: Int){
        _artistInSong[position].value = UiState.Loading
        repository.getAllArtistFromSong(song) {
            _artistInSong[position].value = it
        }
    }

    fun getAllArtistFromSongID(songId: String){
        _artistFromSongID.value = UiState.Loading
        repository.getAllArtistFromSongID(songId) {
            _artistFromSongID.value = it
        }
    }

    fun getAllArtistFromSong2(song: OnlineSong, position: Int){
        _artistInSong2[position].value = UiState.Loading
        repository.getAllArtistFromSong(song) {
            _artistInSong2[position].value = it
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