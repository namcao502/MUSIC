package com.example.music.repositories.online

import android.net.Uri
import com.example.music.UiState
import com.example.music.data.firebase.SongRepository
import com.example.music.data.models.online.OnlinePlaylist
import com.example.music.data.models.online.OnlineSong
import com.example.music.utils.FireStoreCollection
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SongRepositoryImp(val database: FirebaseFirestore,
                        private val storage: StorageReference): SongRepository {

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
            .collection(FireStoreCollection.SONG)
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

    override suspend fun uploadSingleSongFile(fileName: String, fileUri: Uri, result: (UiState<Uri>) -> Unit) {
        try {
            val uri: Uri = withContext(Dispatchers.IO) {
                storage.child("Songs/$fileName")
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

    override suspend fun uploadSingleImageFile(
        directory: String,
        fileName: String,
        fileUri: Uri,
        result: (UiState<Uri>) -> Unit
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

    override fun getSongFromListSongID(songs: List<String>, result: (UiState<List<OnlineSong>>) -> Unit) {
        database
            .collection(FireStoreCollection.SONG)
            .whereIn("id", songs)
            .addSnapshotListener { value, _ ->
                val songList: ArrayList<OnlineSong> = ArrayList()
                if (value != null) {
                    for (document in value){
                        val song = document.toObject(OnlineSong::class.java)
                        songList.add(song)
                    }
                }
                result.invoke(
                    UiState.Success(songList)
                )
            }

    }

}