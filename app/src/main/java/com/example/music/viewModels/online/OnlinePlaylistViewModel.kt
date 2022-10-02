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
import com.example.music.data.models.online.OnlineGenre
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

    //playlist that everyone can access
    private val _playlist2 = MutableLiveData<UiState<List<OnlinePlaylist>>>()
    val playlist2: LiveData<UiState<List<OnlinePlaylist>>> get() = _playlist2

    private val _addPlaylist2 = MutableLiveData<UiState<String>>()
    val addPlaylist2: LiveData<UiState<String>> get() = _addPlaylist2

    private val _deletePlaylist2 = MutableLiveData<UiState<String>>()
    val deletePlaylist2: LiveData<UiState<String>> get() = _deletePlaylist2

    private val _updatePlaylist2 = MutableLiveData<UiState<String>>()
    val updatePlaylist2: LiveData<UiState<String>> get() = _updatePlaylist2


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

    fun getAllSongInPlaylist(playlist: OnlinePlaylist, position: Int) {
        _songInPlaylist[position].value = UiState.Loading
        repository.getAllSongInPlaylist(playlist){
            _songInPlaylist[position].value = it
        }
    }

    fun getAllPlaylists() {
        _playlist2.value = UiState.Loading
        repository.getAllPlaylists {
            _playlist2.value = it
        }
    }

    fun addPlaylist(playlist: OnlinePlaylist){
        _addPlaylist2.value = UiState.Loading
        repository.addPlaylist(playlist){
            _addPlaylist2.value = it
        }
    }

    fun deletePlaylist(playlist: OnlinePlaylist){
        _deletePlaylist2.value = UiState.Loading
        repository.deletePlaylist(playlist){
            _deletePlaylist2.value = it
        }
    }

    fun updatePlaylist(playlist: OnlinePlaylist){
        _updatePlaylist2.value = UiState.Loading
        repository.updatePlaylist(playlist){
            _updatePlaylist2.value = it
        }
    }

}