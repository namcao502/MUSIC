package com.example.music.viewModels

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.music.UiState
import com.example.music.data.firebase.FirebaseRepository
import com.example.music.models.OnlinePlaylist
import com.example.music.models.OnlineSong
import com.example.music.ui.adapters.OnlineSongInPlaylistAdapter
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


@HiltViewModel
class FirebaseViewModel @Inject constructor(val repository: FirebaseRepository): ViewModel(){

    private val _songs = MutableLiveData<UiState<List<OnlineSong>>>()
    val song: LiveData<UiState<List<OnlineSong>>> get() = _songs

    private val _songInPlaylist = MutableLiveData<List<OnlineSong>>()
    val songInPlaylist: LiveData<List<OnlineSong>> get() = _songInPlaylist

    private val _playlist = MutableLiveData<UiState<List<OnlinePlaylist>>>()
    val playlist: LiveData<UiState<List<OnlinePlaylist>>> get() = _playlist

    fun deleteSongInPlaylist(song: OnlineSong, playlist: OnlinePlaylist, user: FirebaseUser){
        viewModelScope.launch(Dispatchers.IO) {

            val tempSongs = playlist.songs as ArrayList
            tempSongs.remove(song.id)

            playlist.id?.let {
                FirebaseFirestore.getInstance()
                    .collection("Playlist")
                    .document(user.uid)
                    .collection("User")
                    .document(it).update("songs", tempSongs)
            }
        }
    }

    fun addSongToPlaylist(song: OnlineSong, playlist: OnlinePlaylist, user: FirebaseUser){

        val tempSongs = playlist.songs as ArrayList
        song.id?.let {
            tempSongs.add(it)
        }

        viewModelScope.launch(Dispatchers.IO) {
            playlist.id?.let {
                FirebaseFirestore.getInstance()
                    .collection("Playlist")
                    .document(user.uid)
                    .collection("User")
                    .document(it).update("songs", tempSongs)
            }
        }
    }

    fun getAllSongs() {
        _songs.value = UiState.Loading
        repository.getAllSongs {
            _songs.value = it
        }
    }

    fun getAllSongInPlaylist(playlist: OnlinePlaylist){
        viewModelScope.launch(Dispatchers.IO) {
            playlist.songs?.let { it2 ->
                FirebaseFirestore.getInstance()
                    .collection("OnlineSong")
                    .whereIn("id", it2)
                    .get()
                    .addOnCompleteListener {
                        if (it.isSuccessful){
                            val songs: ArrayList<OnlineSong> = ArrayList()
                            for (document in it.result){
                                val song = document.toObject(OnlineSong::class.java)
                                songs.add(song)
                            }
                            _songInPlaylist.value = songs
                        }
                    }
            }
        }
    }

    fun getAllPlaylistOfUser(user: FirebaseUser){
        _playlist.value = UiState.Loading
        repository.getAllPlaylistOfUser(user){
            _playlist.value = it
        }
    }

    fun addPlaylistForUser(playlist: OnlinePlaylist, user: FirebaseUser) {
        viewModelScope.launch(Dispatchers.IO) {
            val doc: DocumentReference = FirebaseFirestore.getInstance()
                    .collection("Playlist")
                    .document(user.uid)
                    .collection("User").document()
            val tempPlaylist = playlist
            tempPlaylist.id = doc.id

            doc.set(tempPlaylist)
                .addOnCompleteListener {
                    return@addOnCompleteListener
                }
        }
    }

    fun updatePlaylistForUser(playlist: OnlinePlaylist, user: FirebaseUser) {
        viewModelScope.launch(Dispatchers.IO) {
            playlist.id?.let {
                FirebaseFirestore.getInstance()
                    .collection("Playlist")
                    .document(user.uid)
                    .collection("User")
                    .document(it).update("name", playlist.name)
            }
        }
    }

    fun deletePlaylistForUser(playlist: OnlinePlaylist, user: FirebaseUser) {
        viewModelScope.launch(Dispatchers.IO) {
            playlist.id?.let {
                FirebaseFirestore.getInstance()
                    .collection("Playlist")
                    .document(user.uid)
                    .collection("User")
                    .document(it).delete()
            }
        }
    }
}