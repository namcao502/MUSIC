package com.example.music.online.repositories

import android.net.Uri
import com.example.music.utils.UiState
import com.example.music.online.data.dao.FirebaseRepository
import com.example.music.online.data.models.OnlineSong
import com.example.music.utils.FireStoreCollection
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
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

        val songList: ArrayList<OnlineSong> = ArrayList()
        for (list10ID in songs.chunked(10)){
            database
                .collection(FireStoreCollection.SONG)
                .whereIn("id", list10ID)
                .addSnapshotListener { value, _ ->
                    if (value != null) {
                        for (document in value){
                            val song = document.toObject(OnlineSong::class.java)
                            songList.add(song)
                        }
                    }
                }
        }
        Thread.sleep(100)
        result.invoke(
            UiState.Success(songList)
        )

//        database
//            .collection(FireStoreCollection.SONG)
//            .whereIn("id", songs)
//            .addSnapshotListener { value, _ ->
//                val songList: ArrayList<OnlineSong> = ArrayList()
//                if (value != null) {
//                    for (document in value){
//                        val song = document.toObject(OnlineSong::class.java)
//                        songList.add(song)
//                    }
//                }
//                result.invoke(
//                    UiState.Success(songList)
//                )
//            }
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