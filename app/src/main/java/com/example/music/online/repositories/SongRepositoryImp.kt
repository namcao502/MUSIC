package com.example.music.online.repositories

import com.example.music.online.data.dao.SongRepository
import com.example.music.online.data.models.OnlinePlaylist
import com.example.music.online.data.models.OnlineSong
import com.example.music.utils.FireStoreCollection
import com.example.music.utils.UiState
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage

class SongRepositoryImp(val database: FirebaseFirestore): SongRepository {

    override fun addSongToPlaylist(song: OnlineSong, playlist: OnlinePlaylist, user: FirebaseUser, result: (UiState<String>) -> Unit) {

        val tempSongs = playlist.songs as ArrayList
        song.id?.let {
            tempSongs.add(it)
        }

        playlist.id?.let { playlistID ->
            database
                .collection(FireStoreCollection.PLAYLIST)
                .document(user.uid)
                .collection(FireStoreCollection.USER)
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
        database
            .collection(FireStoreCollection.SONG).orderBy("name")
            .addSnapshotListener { value, _ ->
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

    override fun getAllSongForSearch(result: (UiState<List<OnlineSong>>) -> Unit) {
        database
            .collection(FireStoreCollection.SONG).orderBy("name")
            .get()
            .addOnCompleteListener{
                val songs: ArrayList<OnlineSong> = ArrayList()
                if (it.isSuccessful) {
                    for (doc in it.result){
                        val song = doc.toObject(OnlineSong::class.java)
                        songs.add(song)
                    }
                }
                result.invoke(
                    UiState.Success(songs)
                )

            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun addSong(song: OnlineSong, result: (UiState<String>) -> Unit) {
        val doc = database
            .collection(FireStoreCollection.SONG)
            .document()
        song.id = doc.id
        doc.set(song)
            .addOnSuccessListener {
                result.invoke(UiState.Success("Song ${song.name} added!"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun deleteSong(song: OnlineSong, result: (UiState<String>) -> Unit) {
        database
            .collection(FireStoreCollection.SONG)
            .document(song.id.toString())
            .delete()
            .addOnSuccessListener {
                val songRef = FirebaseStorage.getInstance().getReferenceFromUrl(song.filePath.toString())
                songRef.delete()
                    .addOnSuccessListener {
                        result.invoke(UiState.Success("Song ${song.name} deleted!"))
                    }
                    .addOnFailureListener {
                        result.invoke(UiState.Failure(it.localizedMessage))
                    }
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }

    }

    override fun updateSong(song: OnlineSong, result: (UiState<String>) -> Unit) {
        database
            .collection(FireStoreCollection.SONG)
            .document(song.id!!)
            .update("name", song.name, "filePath", song.filePath, "imgFilePath", song.imgFilePath)
            .addOnSuccessListener {
                result.invoke(UiState.Success("Song ${song.name} updated!"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }

    }

    override fun getTrendingSong(result: (UiState<List<String>>) -> Unit) {
        database
            .collection(FireStoreCollection.SONG).orderBy("views", Query.Direction.DESCENDING).limit(10)
            .addSnapshotListener { value, _ ->
                val songs: ArrayList<String> = ArrayList()
                if (value != null) {
                    for (document in value){
                        val song = document.toObject(OnlineSong::class.java)
                        songs.add(song.id!!)
                    }
                }
                result.invoke(
                    UiState.Success(songs)
                )
            }
    }

    override fun updateViewForSong(song: OnlineSong, result: (UiState<String>) -> Unit) {
        database
            .collection(FireStoreCollection.SONG)
            .document(song.id!!)
            .update("views", song.views)
            .addOnSuccessListener {
                result.invoke(UiState.Success("${song.name}'s view updated"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

}