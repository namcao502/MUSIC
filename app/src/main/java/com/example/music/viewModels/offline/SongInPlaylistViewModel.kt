package com.example.music.viewModels.offline

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.music.data.models.offline.PlaylistWithSongs
import com.example.music.data.models.offline.SongPlaylistCrossRef
import com.example.music.repositories.offline.SongInPlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongInPlaylistViewModel @Inject constructor(private val repository: SongInPlaylistRepository): ViewModel() {

    var playlistId = -1

    fun addSongPlaylistCrossRef(songPlaylistCrossRef: SongPlaylistCrossRef){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addSongPlaylistCrossRef(songPlaylistCrossRef)
        }
    }

    fun deleteSongPlaylistCrossRef(songPlaylistCrossRef: SongPlaylistCrossRef){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSongPlaylistCrossRef(songPlaylistCrossRef)
        }
    }

    fun getSongsOfPlaylist(playlistId: Int): LiveData<PlaylistWithSongs> {
        return repository.getSongsOfPlaylist(playlistId)
    }

    fun getPlaylistId(playlistId: Int) {
        this.playlistId = playlistId
    }

}