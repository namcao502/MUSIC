package com.example.music.viewModels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.music.UiState
import com.example.music.data.firebase.FirebaseRepository
import com.example.music.data.models.online.OnlineArtist
import com.example.music.data.models.online.OnlinePlaylist
import com.example.music.data.models.online.OnlineSong
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class FirebaseViewModel @Inject constructor(val repository: FirebaseRepository): ViewModel(){

    private val _songs = MutableLiveData<UiState<List<OnlineSong>>>()
    val song: LiveData<UiState<List<OnlineSong>>> get() = _songs

//    private val _songInPlaylist = MutableLiveData<UiState<List<OnlineSong>>>()
    private var _songInPlaylist: List<MutableLiveData<UiState<List<OnlineSong>>>>
    = List(10, init= {i: Int -> MutableLiveData<UiState<List<OnlineSong>>>()})

    var songInPlaylist: List<LiveData<UiState<List<OnlineSong>>>> = _songInPlaylist
//    val songInPlaylist: LiveData<UiState<List<OnlineSong>>> get() = _songInPlaylist

    private val _playlist = MutableLiveData<UiState<List<OnlinePlaylist>>>()
    val playlist: LiveData<UiState<List<OnlinePlaylist>>> get() = _playlist

    private val _addPlaylist = MutableLiveData<UiState<String>>()
    val addPlaylist: LiveData<UiState<String>> get() = _addPlaylist

    private val _deletePlaylist = MutableLiveData<UiState<String>>()
    val deletePlaylist: LiveData<UiState<String>> get() = _deletePlaylist

    private val _updatePlaylist = MutableLiveData<UiState<String>>()
    val updatePlaylist: LiveData<UiState<String>> get() = _updatePlaylist

    private val _deleteSongInPlaylist = MutableLiveData<UiState<String>>()
    val deleteSongInPlaylist: LiveData<UiState<String>> get() = _deleteSongInPlaylist

    private val _addSongInPlaylist = MutableLiveData<UiState<String>>()
    val addSongInPlaylist: LiveData<UiState<String>> get() = _addSongInPlaylist

    private val _addArtists = MutableLiveData<UiState<String>>()
    val addArtists: LiveData<UiState<String>> get() = _addArtists

    private val _addSong = MutableLiveData<UiState<String>>()
    val addSong: LiveData<UiState<String>> get() = _addSong

    private val _deleteSong = MutableLiveData<UiState<String>>()
    val deleteSong: LiveData<UiState<String>> get() = _deleteSong

    private val _updateSong = MutableLiveData<UiState<String>>()
    val updateSong: LiveData<UiState<String>> get() = _updateSong


    fun deleteSongInPlaylist(song: OnlineSong, playlist: OnlinePlaylist, user: FirebaseUser){
        _deleteSongInPlaylist.value = UiState.Loading
        repository.deleteSongInPlaylist(song, playlist, user){
            _deleteSongInPlaylist.value = it
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

    fun getAllSongInPlaylist(playlist: OnlinePlaylist, position: Int){
        _songInPlaylist[position].value = UiState.Loading
        repository.getAllSongInPlaylist(playlist){
//            when(it){
//                is UiState.Success -> {
//                    val length = it.data.size
//                    for (x in 0 until length){
//                        _songInPlaylist[position].value = it
//                    }
//                }
//                else -> {}
//            }
            _songInPlaylist[position].value = it
        }
    }

    fun getAllPlaylistOfUser(user: FirebaseUser){
        _playlist.value = UiState.Loading
        repository.getAllPlaylistOfUser(user){
            _playlist.value = it
        }
    }

    fun addPlaylistForUser(playlist: OnlinePlaylist, user: FirebaseUser) {
        _addPlaylist.value = UiState.Loading
        repository.addPlaylistForUser(playlist, user){
            _addPlaylist.value = it
        }
    }

    fun updatePlaylistForUser(playlist: OnlinePlaylist, user: FirebaseUser) {
        _updatePlaylist.value = UiState.Loading
        repository.updatePlaylistForUser(playlist, user){
            _updatePlaylist.value = it
        }
    }

    fun deletePlaylistForUser(playlist: OnlinePlaylist, user: FirebaseUser) {
        _deletePlaylist.value = UiState.Loading
        repository.deletePlaylistForUser(playlist, user){
            _deletePlaylist.value = it
        }
    }

    fun getAllPlaylistOfSong(song: OnlineSong, user: FirebaseUser){
        _playlist.value = UiState.Loading
        repository.getAllPlaylistOfSong(song, user){
            _playlist.value = it
        }
    }

    fun addArtist(artist: OnlineArtist){
        _addArtists.value = UiState.Loading
        repository.addArtist(artist){
            _addArtists.value = it
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

    fun uploadSingleSongFile(fileName: String, fileUri: Uri, result: (UiState<Uri>) -> Unit){
        result.invoke(UiState.Loading)
        viewModelScope.launch {
            repository.uploadSingleSongFile(fileName, fileUri, result)
        }
    }

    fun uploadSingleImageFile(fileName: String, fileUri: Uri, result: (UiState<Uri>) -> Unit){
        result.invoke(UiState.Loading)
        viewModelScope.launch {
            repository.uploadSingleImageFile(fileName, fileUri, result)
        }
    }

}