package com.example.music.viewModels.online

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.music.UiState
import com.example.music.data.firebase.FirebaseRepository
import com.example.music.data.firebase.PlaylistRepository
import com.example.music.data.models.online.OnlineArtist
import com.example.music.data.models.online.OnlinePlaylist
import com.example.music.data.models.online.OnlineSong
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class OnlinePlaylistViewModel @Inject constructor(val repository: PlaylistRepository): ViewModel(){

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


    fun deleteSongInPlaylist(song: OnlineSong, playlist: OnlinePlaylist, user: FirebaseUser){
        _deleteSongInPlaylist.value = UiState.Loading
        repository.deleteSongInPlaylist(song, playlist, user){
            _deleteSongInPlaylist.value = it
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

    fun uploadSingleImageFile(directory: String, fileName: String, fileUri: Uri, result: (UiState<Uri>) -> Unit){
        result.invoke(UiState.Loading)
        viewModelScope.launch {
            repository.uploadSingleImageFile(directory, fileName, fileUri, result)
        }
    }

    fun getAllSongInPlaylist(playlist: OnlinePlaylist, position: Int) {
        _songInPlaylist[position].value = UiState.Loading
        repository.getAllSongInPlaylist(playlist){
            _songInPlaylist[position].value = it
        }
    }

}