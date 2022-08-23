package com.example.music.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.music.models.PlaylistWithSongs
import com.example.music.models.SongPlaylistCrossRef
import com.example.music.repositories.SongInPlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongInPlaylistViewModel @Inject constructor(private val repository: SongInPlaylistRepository): ViewModel() {

    fun addSongPlaylistCrossRef(songPlaylistCrossRef: SongPlaylistCrossRef){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addSongPlaylistCrossRef(songPlaylistCrossRef)
        }
    }

    fun getSongsOfPlaylist(playlistId: Int): LiveData<List<PlaylistWithSongs>> {
        return repository.getSongsOfPlaylist(playlistId)
    }

}