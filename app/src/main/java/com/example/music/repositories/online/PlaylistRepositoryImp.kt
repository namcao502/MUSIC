package com.example.music.repositories.online

import com.example.music.UiState
import com.example.music.data.firebase.PlaylistRepository
import com.example.music.data.models.online.OnlineGenre
import com.example.music.data.models.online.OnlinePlaylist
import com.example.music.data.models.online.OnlineSong
import com.example.music.utils.FireStoreCollection
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class PlaylistRepositoryImp(val database: FirebaseFirestore): PlaylistRepository {

    override fun getAllSongInPlaylist(playlist: OnlinePlaylist, result: (UiState<List<OnlineSong>>) -> Unit) {

        if (playlist.songs!!.isEmpty()){
            return
        }
        else {
            database
                .collection(FireStoreCollection.SONG)
                .whereIn("id", playlist.songs!!)
                .addSnapshotListener { value, _ ->
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

    override fun deleteSongInPlaylist(song: OnlineSong, playlist: OnlinePlaylist, user: FirebaseUser, result: (UiState<String>) -> Unit) {

        val tempSongs = playlist.songs as ArrayList
        tempSongs.remove(song.id)

        playlist.id?.let { playlistID ->
            database
                .collection(FireStoreCollection.PLAYLIST)
                .document(user.uid)
                .collection(FireStoreCollection.USER)
                .document(playlistID).update("songs", tempSongs)
                .addOnSuccessListener {
                    result.invoke(UiState.Success("${song.name} in ${playlist.name} deleted!"))
                }
                .addOnFailureListener {
                    result.invoke(UiState.Failure(it.localizedMessage))
                }
        }
    }

    override fun getAllPlaylistOfUser(user: FirebaseUser, result: (UiState<List<OnlinePlaylist>>) -> Unit) {
        database
            .collection(FireStoreCollection.PLAYLIST)
            .document(user.uid)
            .collection(FireStoreCollection.USER)
            .addSnapshotListener { value, _ ->
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
        val doc = database
            .collection(FireStoreCollection.PLAYLIST)
            .document(user.uid)
            .collection(FireStoreCollection.USER).document()
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
            database
                .collection(FireStoreCollection.PLAYLIST)
                .document(user.uid)
                .collection(FireStoreCollection.USER)
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
            database
                .collection(FireStoreCollection.PLAYLIST)
                .document(user.uid)
                .collection(FireStoreCollection.USER)
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

    override fun getAllPlaylists(result: (UiState<List<OnlinePlaylist>>) -> Unit) {
        database
            .collection(FireStoreCollection.PLAYLIST)
            .addSnapshotListener { value, _ ->
                val playlists: ArrayList<OnlinePlaylist> = ArrayList()
                if (value != null) {
                    for (document in value){
                        val playlist = document.toObject(OnlinePlaylist::class.java)
                        playlists.add(playlist)
                    }
                }
                result.invoke(
                    UiState.Success(playlists)
                )
            }
    }

    override fun addPlaylist(playlist: OnlinePlaylist, result: (UiState<String>) -> Unit) {
        val doc = database
            .collection(FireStoreCollection.PLAYLIST)
            .document()
        playlist.id = doc.id
        doc.set(playlist)
            .addOnSuccessListener {
                result.invoke(UiState.Success("Playlist ${playlist.name} added!"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun updatePlaylist(playlist: OnlinePlaylist, result: (UiState<String>) -> Unit) {
        database
            .collection(FireStoreCollection.PLAYLIST)
            .document(playlist.id!!)
            .update("name", playlist.name, "imgFilePath", playlist.imgFilePath, "songs", playlist.songs)
            .addOnSuccessListener {
                result.invoke(UiState.Success("Playlist ${playlist.name} updated!"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun deletePlaylist(playlist: OnlinePlaylist, result: (UiState<String>) -> Unit) {
        database
            .collection(FireStoreCollection.PLAYLIST)
            .document(playlist.id.toString())
            .delete()
            .addOnSuccessListener {
                if (playlist.imgFilePath!!.isNotEmpty()){
                    val songRef = FirebaseStorage.getInstance().getReferenceFromUrl(playlist.imgFilePath.toString())
                    songRef.delete()
                        .addOnSuccessListener {
                            result.invoke(UiState.Success("Playlist ${playlist.name} deleted!"))
                        }
                        .addOnFailureListener {
                            result.invoke(UiState.Failure(it.localizedMessage))
                        }
                }
                result.invoke(UiState.Success("Playlist ${playlist.name} deleted!"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

}