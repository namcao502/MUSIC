package com.example.music.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.music.models.Playlist
import com.example.music.models.PlaylistWithSongs
import com.example.music.repositories.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(private val repository: PlaylistRepository): ViewModel() {

    fun readAllPlaylists(): LiveData<List<Playlist>> {
        return repository.readAllPlaylists()
    }

    fun addPlaylist(playlist: Playlist){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addPlaylist(playlist)
        }
    }

    fun deletePlaylist(playlist: Playlist){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePlaylist(playlist)
        }
    }

    fun updatePlaylist(playlist: Playlist){
        viewModelScope.launch(Dispatchers.IO) {
            repository.updatePlaylist(playlist)
        }
    }
}