package com.example.music.viewModels

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.music.models.OnlinePlaylist
import com.example.music.models.OnlineSong
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


@HiltViewModel
class FirebaseViewModel @Inject constructor(): ViewModel(){

    private val _songs = MutableLiveData<List<OnlineSong>>()
    val song: LiveData<List<OnlineSong>> get() = _songs

    private val _playlist = MutableLiveData<List<OnlinePlaylist>>()
    val playlist: LiveData<List<OnlinePlaylist>> get() = _playlist

    fun getAllSongs() {
        viewModelScope.launch(Dispatchers.IO) {
            FirebaseFirestore.getInstance().collection("OnlineSong")
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        val songs: ArrayList<OnlineSong> = ArrayList()
                        for (document in it.result){
                            val song = document.toObject(OnlineSong::class.java)
                            songs.add(song)
                        }
                        _songs.value = songs
                    }
                }
        }
    }

    fun getAllPlaylistOfUser(user: FirebaseUser){
        viewModelScope.launch(Dispatchers.IO) {
            FirebaseFirestore.getInstance()
                .collection("Playlist")
                .document(user.uid)
                .collection("User")
                .addSnapshotListener { value, error ->

                    if (error != null) {
                        return@addSnapshotListener
                    }

                    val playlist: ArrayList<OnlinePlaylist> = ArrayList()
                    if (value != null) {
                        for (document in value){
                            val tempPlaylist = document.toObject(OnlinePlaylist::class.java)
                            playlist.add(tempPlaylist)
                        }
                    }
                    _playlist.value = playlist
                 }
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