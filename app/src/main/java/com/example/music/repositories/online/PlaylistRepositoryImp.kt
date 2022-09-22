package com.example.music.repositories.online

import android.net.Uri
import com.example.music.UiState
import com.example.music.data.firebase.PlaylistRepository
import com.example.music.data.models.online.OnlinePlaylist
import com.example.music.data.models.online.OnlineSong
import com.example.music.utils.FireStoreCollection
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PlaylistRepositoryImp(val database: FirebaseFirestore,
                            private val storage: StorageReference): PlaylistRepository {

    override fun getAllSongInPlaylist(playlist: OnlinePlaylist, result: (UiState<List<OnlineSong>>) -> Unit) {

        if (playlist.songs!!.isEmpty()){
            return
        }
        else {
            database
                .collection(FireStoreCollection.SONG)
                .whereIn("id", playlist.songs)
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

    override suspend fun uploadSingleImageFile(
        directory: String,
        fileName: String,
        fileUri: Uri, result: (UiState<Uri>) -> Unit
    ) {
        try {
            val uri: Uri = withContext(Dispatchers.IO) {
                storage.child("$directory/$fileName")
                    .putFile(fileUri)
                    .await()
                    .storage
                    .downloadUrl
                    .await()
            }
            result.invoke(UiState.Success(uri))
        } catch (e: FirebaseException){
            result.invoke(UiState.Failure(e.message))
        }catch (e: Exception){
            result.invoke(UiState.Failure(e.message))
        }
    }

}