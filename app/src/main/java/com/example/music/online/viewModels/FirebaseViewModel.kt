package com.example.music.online.viewModels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.music.online.data.dao.FirebaseRepository
import com.example.music.online.data.models.OnlineSong
import com.example.music.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class FirebaseViewModel @Inject constructor(val repository: FirebaseRepository): ViewModel(){

    private val _songs = MutableLiveData<UiState<List<OnlineSong>>>()
    val song: LiveData<UiState<List<OnlineSong>>> get() = _songs

    private val _songFromID = MutableLiveData<UiState<List<OnlineSong>>>()
    val songFromID: LiveData<UiState<List<OnlineSong>>> get() = _songFromID

    private val _songFromIDRecent = MutableLiveData<UiState<List<OnlineSong>>>()
    val songFromIDRecent: LiveData<UiState<List<OnlineSong>>> get() = _songFromIDRecent

    private var _songFromID2: List<MutableLiveData<UiState<OnlineSong>>>
            = List(100, init={MutableLiveData<UiState<OnlineSong>>()})

    var songFromID2: List<LiveData<UiState<OnlineSong>>> = _songFromID2

    private val _updateModel = MutableLiveData<UiState<String>>()
    val updateModel: LiveData<UiState<String>> get() = _updateModel

    fun updateModelById(name: String, id: String, listSong: ArrayList<String>){
        _updateModel.value = UiState.Loading
        repository.updateModelById(name, id, listSong){
            _updateModel.value = it
        }
    }

    fun getAllSongs() {
        _songs.value = UiState.Loading
        repository.getAllSongs {
            _songs.value = it
        }
    }

    fun getSongFromListSongID(songs: List<String>){
        _songFromID.value = UiState.Loading
        repository.getSongFromListSongID(songs){
            _songFromID.value = it
        }
    }

    fun getSongFromListSongIDForRecent(songs: List<String>){
        _songFromIDRecent.value = UiState.Loading
        repository.getSongFromListSongID(songs){
            _songFromIDRecent.value = it
        }
    }

    fun getSongFromSongID(songId: String, position: Int){
        _songFromID2[position].value = UiState.Loading
        repository.getSongFromSongID(songId){
            _songFromID2[position].value = it
        }
    }

    fun uploadSingleSongFile(fileName: String, fileUri: Uri, result: (UiState<Uri>) -> Unit){
        result.invoke(UiState.Loading)
        viewModelScope.launch {
            repository.uploadSingleSongFile(fileName, fileUri, result)
        }
    }

    fun uploadSingleImageFile(directory: String, fileName: String, fileUri: Uri, result: (UiState<Uri>) -> Unit){
        result.invoke(UiState.Loading)
        viewModelScope.launch {
            repository.uploadSingleImageFile(directory, fileName, fileUri, result)
        }
    }

    fun downloadSingleSongFile(context: Context, fileName: String, filePath: String, result: (UiState<String>) -> Unit){
        result.invoke(UiState.Loading)
        repository.downloadSingleSongFile(context, fileName, filePath, result)
    }

}