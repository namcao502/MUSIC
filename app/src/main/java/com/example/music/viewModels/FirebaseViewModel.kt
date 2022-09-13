package com.example.music.viewModels

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class FirebaseViewModel @Inject constructor(): ViewModel(){

    private val _songs = MutableLiveData<List<OnlineSong>>()
    val song: LiveData<List<OnlineSong>> get() = _songs

    private val _songInPlaylist = MutableLiveData<List<OnlineSong>>()
    val songInPlaylist: LiveData<List<OnlineSong>> get() = _songInPlaylist

    private val _playlist = MutableLiveData<List<OnlinePlaylist>>()
    val playlist: LiveData<List<OnlinePlaylist>> get() = _playlist

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
        viewModelScope.launch(Dispatchers.IO) {
            FirebaseFirestore.getInstance()
                .collection("OnlineSong")
                .addSnapshotListener { value, error ->

                    if (error != null) {
                        return@addSnapshotListener
                    }

                    val songs: ArrayList<OnlineSong> = ArrayList()
                    if (value != null) {
                        for (document in value){
                            val song = document.toObject(OnlineSong::class.java)
                            songs.add(song)
                        }
                    }
                    _songs.value = songs
                }
        }
    }

    fun getAllSongInPlaylist(playlist: OnlinePlaylist){
        viewModelScope.launch(Dispatchers.IO) {
            playlist.songs?.let {
                FirebaseFirestore.getInstance()
                    .collection("OnlineSong")
                    .whereIn("id", it)
                    .addSnapshotListener { value, error ->

                        if (error != null) {
                            return@addSnapshotListener
                        }

                        val songs: ArrayList<OnlineSong> = ArrayList()
                        if (value != null) {
                            for (document in value){
                                val song = document.toObject(OnlineSong::class.java)
                                songs.add(song)
                            }
                        }
                        _songInPlaylist.value = songs
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