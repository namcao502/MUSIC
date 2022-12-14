package com.example.music.offline.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.music.offline.data.models.Song
import com.example.music.offline.repositories.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongViewModel @Inject constructor(private val repository: SongRepository): ViewModel() {

    fun readAllSongs(): LiveData<List<Song>> {
        return repository.readAllSongs()
    }

    fun addSong(song: Song){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addSong(song)
        }
    }

    fun deleteSong(song: Song){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSong(song)
        }
    }

    fun updateSong(song: Song){
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSong(song)
        }
    }

    fun deleteAllSongs(){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllSongs()
        }
    }

}