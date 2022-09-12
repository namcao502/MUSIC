package com.example.music.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.music.data.firebase.FirebaseRepository
import com.example.music.models.OnlineSong
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class FirebaseViewModel @Inject constructor(val repository: FirebaseRepository): ViewModel(){

    private val _songs = MutableLiveData<List<OnlineSong>>()
    val song: LiveData<List<OnlineSong>> get() = _songs

    fun getAllSongs() {
        _songs.value = repository.getAllSongs()
    }
}