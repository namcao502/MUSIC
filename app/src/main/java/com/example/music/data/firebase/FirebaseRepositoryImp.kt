package com.example.music.data.firebase

import com.example.music.UiState
import com.example.music.models.OnlinePlaylist
import com.example.music.models.OnlineSong
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseRepositoryImp(val database: FirebaseFirestore): FirebaseRepository {

    override fun deleteSongInPlaylist(song: OnlineSong, playlist: OnlinePlaylist, user: FirebaseUser, result: (UiState<String>) -> Unit) {

        val tempSongs = playlist.songs as ArrayList
        tempSongs.remove(song.id)

        playlist.id?.let { playlistID ->
            FirebaseFirestore.getInstance()
                .collection("Playlist")
                .document(user.uid)
                .collection("User")
                .document(playlistID).update("songs", tempSongs)
                .addOnSuccessListener {
                    result.invoke(UiState.Success("${song.name} in ${playlist.name} deleted!"))
                }
                .addOnFailureListener {
                    result.invoke(UiState.Failure(it.localizedMessage))
                }
        }
    }

    override fun addSongToPlaylist(song: OnlineSong, playlist: OnlinePlaylist, user: FirebaseUser, result: (UiState<String>) -> Unit) {

        val tempSongs = playlist.songs as ArrayList
        song.id?.let {
            tempSongs.add(it)
        }

        playlist.id?.let { playlistID ->
            FirebaseFirestore.getInstance()
                .collection("Playlist")
                .document(user.uid)
                .collection("User")
                .document(playlistID).update("songs", tempSongs)
                .addOnSuccessListener {
                    result.invoke(UiState.Success("Playlist ${playlist.name} added to ${playlist.name}!"))
                }
                .addOnFailureListener {
                    result.invoke(UiState.Failure(it.localizedMessage))
                }
        }
    }

    override fun getAllSongs(result: (UiState<List<OnlineSong>>) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("OnlineSong")
            .addSnapshotListener { value, error ->
                val songs: ArrayList<OnlineSong> = ArrayList()
                if (value != null) {
                    for (document in value){
                        val song = document.toObject(OnlineSong::class.java)
                        songs.add(song)
                    }
                }
                result.invoke(
                    UiState.Success(songs)
                )
            }
    }

    override fun getAllSongInPlaylist(playlist: OnlinePlaylist, result: (UiState<List<OnlineSong>>) -> Unit) {

        if (playlist.songs!!.isEmpty()){
            return
        }
        else {
            FirebaseFirestore.getInstance()
                .collection("OnlineSong")
                .whereIn("id", playlist.songs)
                .addSnapshotListener { value, error ->
                    val songs: ArrayList<OnlineSong> = ArrayList()
                    if (value != null) {
                        for (document in value){
                            val song = document.toObject(OnlineSong::class.java)
                            songs.add(song)
                        }
                    }
                    result.invoke(UiState.Success(songs))
                }
        }
    }

    override fun getAllPlaylistOfUser(user: FirebaseUser, result: (UiState<List<OnlinePlaylist>>) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("Playlist")
            .document(user.uid)
            .collection("User")
            .addSnapshotListener { value, error ->
                val playlist: ArrayList<OnlinePlaylist> = ArrayList()
                if (value != null) {
                    for (document in value){
                        val tempPlaylist = document.toObject(OnlinePlaylist::class.java)
                        playlist.add(tempPlaylist)
                    }
                }
                result.invoke(
                    UiState.Success(playlist)
                )
            }
    }

    override fun addPlaylistForUser(playlist: OnlinePlaylist, user: FirebaseUser, result: (UiState<String>) -> Unit) {
        val doc = FirebaseFirestore.getInstance()
            .collection("Playlist")
            .document(user.uid)
            .collection("User").document()
        playlist.id = doc.id

        doc.set(playlist)
            .addOnSuccessListener {
                result.invoke(UiState.Success("Playlist ${playlist.name} added!"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun updatePlaylistForUser(playlist: OnlinePlaylist, user: FirebaseUser, result: (UiState<String>) -> Unit) {
        playlist.id?.let { playlistID ->
            FirebaseFirestore.getInstance()
                .collection("Playlist")
                .document(user.uid)
                .collection("User")
                .document(playlistID)
                .update("name", playlist.name)
                .addOnSuccessListener {
                    result.invoke(UiState.Success("Playlist ${playlist.name} updated!"))
                }
                .addOnFailureListener {
                    result.invoke(UiState.Failure(it.localizedMessage))
                }
        }
    }

    override fun deletePlaylistForUser(playlist: OnlinePlaylist, user: FirebaseUser, result: (UiState<String>) -> Unit) {
        playlist.id?.let { playlistID ->
            FirebaseFirestore.getInstance()
                .collection("Playlist")
                .document(user.uid)
                .collection("User")
                .document(playlistID)
                .delete()
                .addOnSuccessListener {
                    result.invoke(UiState.Success("Playlist ${playlist.name} deleted!"))
                }
                .addOnFailureListener {
                    result.invoke(UiState.Failure(it.localizedMessage))
                }
        }
    }

}