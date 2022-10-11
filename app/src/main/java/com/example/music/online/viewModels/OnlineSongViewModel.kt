package com.example.music.online.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.music.utils.UiState
import com.example.music.online.data.dao.SongRepository
import com.example.music.online.data.models.OnlinePlaylist
import com.example.music.online.data.models.OnlineSong
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnlineSongViewModel @Inject constructor(val repository: SongRepository): ViewModel(){

    private val _songs = MutableLiveData<UiState<List<OnlineSong>>>()
    val song: LiveData<UiState<List<OnlineSong>>> get() = _songs

    private val _addSongInPlaylist = MutableLiveData<UiState<String>>()
    val addSongInPlaylist: LiveData<UiState<String>> get() = _addSongInPlaylist

    private val _addSong = MutableLiveData<UiState<String>>()
    val addSong: LiveData<UiState<String>> get() = _addSong

    private val _deleteSong = MutableLiveData<UiState<String>>()
    val deleteSong: LiveData<UiState<String>> get() = _deleteSong

    private val _updateSong = MutableLiveData<UiState<String>>()
    val updateSong: LiveData<UiState<String>> get() = _updateSong

    private val _songCount= MutableLiveData<UiState<Int>>()
    val songCount: LiveData<UiState<Int>> get() = _songCount

    private val _updateView = MutableLiveData<UiState<String>>()
    val updateView: LiveData<UiState<String>> get() = _updateView

    fun countSong(){
        _songCount.value = UiState.Loading
        repository.countSong {
            _songCount.value = it
        }
    }

    fun updateViewForSong(song: OnlineSong){
        _updateView.value = UiState.Loading
        repository.updateViewForSong(song){
            _updateView.value = it
        }
    }

    fun addSongToPlaylist(song: OnlineSong, playlist: OnlinePlaylist, user: FirebaseUser){
        _addSongInPlaylist.value = UiState.Loading
        repository.addSongToPlaylist(song, playlist, user){
            _addSongInPlaylist.value = it
        }
    }

    fun getAllSongs() {
        _songs.value = UiState.Loading
        repository.getAllSongs {
            _songs.value = it
        }
    }

    fun addSong(song: OnlineSong){
        _addSong.value = UiState.Loading
        repository.addSong(song){
            _addSong.value = it
        }
    }

    fun deleteSong(song: OnlineSong){
        _deleteSong.value = UiState.Loading
        repository.deleteSong(song){
            _deleteSong.value = it
        }
    }

    fun updateSong(song: OnlineSong){
        _updateSong.value = UiState.Loading
        repository.updateSong(song){
            _updateSong.value = it
        }
    }

}