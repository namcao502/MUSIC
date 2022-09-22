package com.example.music.repositories.online

import android.net.Uri
import com.example.music.UiState
import com.example.music.data.firebase.FirebaseRepository
import com.example.music.data.models.online.OnlineArtist
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

class FirebaseRepositoryImp(val database: FirebaseFirestore,
                            private val storage: StorageReference): FirebaseRepository {

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
            database
                .collection(FireStoreCollection.SONG)
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
        database
            .collection(FireStoreCollection.PLAYLIST)
            .document(user.uid)
            .collection(FireStoreCollection.USER)
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

    override fun getAllPlaylistOfSong(song: OnlineSong, user: FirebaseUser, result: (UiState<List<OnlinePlaylist>>) -> Unit) {
        song.id?.let { songID ->
            database
                .collection(FireStoreCollection.PLAYLIST)
                .document(user.uid)
                .collection(FireStoreCollection.USER)
                .whereArrayContains("songs", songID)
                .get()
                .addOnSuccessListener { value ->
                    val playlist: ArrayList<OnlinePlaylist> = ArrayList()
                    if (value != null) {
                        for (document in value){
                            val tempPlaylist = document.toObject(OnlinePlaylist::class.java)
                            playlist.add(tempPlaylist)
                        }
                    }
                    result.invoke(UiState.Success(playlist))
                }
                .addOnFailureListener {
                    result.invoke(UiState.Failure(it.localizedMessage))
                }
        }
    }

    override fun getAllArtists(result: (UiState<List<OnlineArtist>>) -> Unit) {
        database
            .collection(FireStoreCollection.ARTIST)
            .addSnapshotListener { value, error ->
                val artists: ArrayList<OnlineArtist> = ArrayList()
                if (value != null) {
                    for (document in value){
                        val artist = document.toObject(OnlineArtist::class.java)
                        artists.add(artist)
                    }
                }
                result.invoke(
                    UiState.Success(artists)
                )
            }
    }


    override fun addArtist(artist: OnlineArtist, result: (UiState<String>) -> Unit) {
        val doc = database
            .collection(FireStoreCollection.ARTIST)
            .document()
        artist.id = doc.id
        doc.set(artist)
            .addOnSuccessListener {
                result.invoke(UiState.Success("Artist ${artist.name} added!"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun updateArtist(artist: OnlineArtist, result: (UiState<String>) -> Unit) {
        database
            .collection(FireStoreCollection.ARTIST)
            .document(artist.id!!)
            .update("name", artist.name, "imgFilePath", artist.imgFilePath)
            .addOnSuccessListener {
                result.invoke(UiState.Success("Song ${artist.name} updated!"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun deleteArtist(artist: OnlineArtist, result: (UiState<String>) -> Unit) {
        database
            .collection(FireStoreCollection.ARTIST)
            .document(artist.id.toString())
            .delete()
            .addOnSuccessListener {
                if (artist.imgFilePath!!.isNotEmpty()){
                    val songRef = FirebaseStorage.getInstance().getReferenceFromUrl(artist.imgFilePath.toString())
                    songRef.delete()
                        .addOnSuccessListener {
                            result.invoke(UiState.Success("Song ${artist.name} deleted!"))
                        }
                        .addOnFailureListener {
                            result.invoke(UiState.Failure(it.localizedMessage))
                        }
                }
                result.invoke(UiState.Success("Song ${artist.name} deleted!"))
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

}