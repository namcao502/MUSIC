package com.example.music.data.firebase

import com.example.music.UiState
import com.example.music.models.OnlinePlaylist
import com.example.music.models.OnlineSong
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseRepositoryImp(val database: FirebaseFirestore): FirebaseRepository {

    override fun deleteSongInPlaylist(song: OnlineSong, playlist: OnlinePlaylist, user: FirebaseUser) {
        TODO("Not yet implemented")
    }

    override fun addSongToPlaylist(song: OnlineSong, playlist: OnlinePlaylist, user: FirebaseUser) {
        TODO("Not yet implemented")
    }

    override fun getAllSongs(result: (UiState<List<OnlineSong>>) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("OnlineSong")
            .get()
            .addOnSuccessListener {
                val songs: ArrayList<OnlineSong> = ArrayList()
                for (document in it){
                    val song = document.toObject(OnlineSong::class.java)
                    songs.add(song)
                }
                result.invoke(
                    UiState.Success(songs)
                )
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
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
                .get()
                .addOnSuccessListener {
                    val songs: ArrayList<OnlineSong> = ArrayList()
                    for (document in it){
                        val song = document.toObject(OnlineSong::class.java)
                        songs.add(song)
                    }
                    result.invoke(
                        UiState.Success(songs)
                    )
                }
                .addOnFailureListener {
                    result.invoke(UiState.Failure(it.localizedMessage))
                }
        }
    }

    override fun getAllPlaylistOfUser(user: FirebaseUser, result: (UiState<List<OnlinePlaylist>>) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("Playlist")
            .document(user.uid)
            .collection("User")
            .get()
            .addOnSuccessListener {
                val playlist: ArrayList<OnlinePlaylist> = ArrayList()
                for (document in it){
                    val tempPlaylist = document.toObject(OnlinePlaylist::class.java)
                    playlist.add(tempPlaylist)
                }
                result.invoke(
                    UiState.Success(playlist)
                )
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun addPlaylistForUser(playlist: OnlinePlaylist, user: FirebaseUser) {
        TODO("Not yet implemented")
    }

    override fun updatePlaylistForUser(playlist: OnlinePlaylist, user: FirebaseUser) {
        TODO("Not yet implemented")
    }

    override fun deletePlaylistForUser(playlist: OnlinePlaylist, user: FirebaseUser) {
        TODO("Not yet implemented")
    }

}