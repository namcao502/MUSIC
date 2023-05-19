package com.example.music.online.repositories

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.example.music.online.data.dao.FirebaseRepository
import com.example.music.online.data.models.OnlineSong
import com.example.music.utils.FireStoreCollection
import com.example.music.utils.UiState
import com.example.music.utils.downloadFile
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseRepositoryImp(val database: FirebaseFirestore,
                            private val storage: StorageReference): FirebaseRepository {

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

    override fun getSongFromListSongID(songs: List<String>, result: (UiState<List<OnlineSong>>) -> Unit) {

        if (songs.isEmpty()){
            return
        }

//        if (songs.size <= 9){
//            database
//                .collection(FireStoreCollection.SONG)
//                .whereIn("id", songs)
//                .addSnapshotListener { value, _ ->
//                    val songList: ArrayList<OnlineSong> = ArrayList()
//                    if (value != null) {
//                        for (document in value){
//                            val song = document.toObject(OnlineSong::class.java)
//                            songList.add(song)
//                        }
//                    }
//                    result.invoke(
//                        UiState.Success(songList)
//                    )
//                }
//        }
//        else {
//            val songList: ArrayList<OnlineSong> = ArrayList()
//            for (listID in songs.chunked(9)){
//                database
//                    .collection(FireStoreCollection.SONG)
//                    .whereIn("id", listID)
//                    .addSnapshotListener { value, _ ->
//                        if (value != null) {
//                            for (document in value){
//                                val song = document.toObject(OnlineSong::class.java)
//                                songList.add(song)
//                            }
//                        }
//                    }
//            }
//            result.invoke(
//                UiState.Success(songList)
//            )
//        }

        database
            .collection(FireStoreCollection.SONG).limit(10)
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

    override fun getSongFromSongID(songId: String, result: (UiState<OnlineSong>) -> Unit) {
//        database
//            .collection(FireStoreCollection.SONG)
//            .whereEqualTo("id", songId)
//            .addSnapshotListener { value, _ ->
//                if (value != null) {
//                    for (document in value) {
//                        val song = document.toObject(OnlineSong::class.java)
//                        Log.i("TAG502", "getSongFromSongID: $song")
//                        result.invoke(UiState.Success(song))
//                        break
//                    }
//                }
//            }
        database
            .collection(FireStoreCollection.SONG)
            .whereEqualTo("id", songId).get()
            .addOnSuccessListener {
                if (it != null) {
                    for (document in it) {
                        val song = document.toObject(OnlineSong::class.java)
                        result.invoke(UiState.Success(song))
                        break
                    }
                }
            }
    }

    private fun getSongById(result: ArrayList<OnlineSong>, songs: List<String>, size: Int) {
        if (size == 0){
            return
        }
        else {
//            database
//                .collection(FireStoreCollection.SONG)
//                .whereIn("id", songs[size - 1])
//                .addSnapshotListener { value, _ ->
//                    if (value != null) {
//                        for (document in value){
//                            val song = document.toObject(OnlineSong::class.java)
//                            result.add(song)
//                        }
//                    }
//                }
            database
                .collection(FireStoreCollection.SONG)
                .orderBy("name")
                .whereEqualTo("id", songs[size - 1])
                .addSnapshotListener { value, _ ->
                    if (value != null) {
                        for (document in value) {
                            val song = document.toObject(OnlineSong::class.java)
                            result.add(song)
                            Log.i("TAG502", "getSongFromListSongID2, each: $song")
                        }
                    }
                }
            return getSongById(result, songs, size - 1)
        }
    }

    override suspend fun uploadSingleSongFile(fileName: String, fileUri: Uri, result: (UiState<Uri>) -> Unit) {
        try {
            val uri: Uri = withContext(Dispatchers.IO) {
                storage
                    .child("Songs/$fileName")
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
                storage
                    .child("$directory/$fileName")
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

    override fun downloadSingleSongFile(
        context: Context,
        fileName: String,
        filePath: String,
        result: (UiState<String>) -> Unit
    ) {
        Firebase.storage
            .getReferenceFromUrl(filePath)
            .downloadUrl
            .addOnSuccessListener { uri: Uri ->
                val url = uri.toString()
                downloadFile(context, fileName, "", Environment.DIRECTORY_DOWNLOADS, url)
                result.invoke(UiState.Success("Downloading..."))
            }.addOnFailureListener { e: Exception ->
                result.invoke(UiState.Failure(e.toString()))
            }
    }

    override fun updateModelById(name: String, id: String, listSong: ArrayList<String>, result: (UiState<String>) -> Unit) {
        database
            .collection(name)
            .document(id)
            .update("songs", listSong)
            .addOnSuccessListener {
                result.invoke(UiState.Success("Updated"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.message))
            }
    }

}