package com.example.music.online.repositories

import com.example.music.utils.UiState
import com.example.music.online.data.dao.AlbumRepository
import com.example.music.online.data.models.OnlineAlbum
import com.example.music.utils.FireStoreCollection
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class AlbumRepositoryImp(val database: FirebaseFirestore): AlbumRepository {

    override fun getAllAlbums(result: (UiState<List<OnlineAlbum>>) -> Unit) {
        database
            .collection(FireStoreCollection.ALBUM)
            .addSnapshotListener { value, _ ->
                val albums: ArrayList<OnlineAlbum> = ArrayList()
                if (value != null) {
                    for (document in value){
                        val album = document.toObject(OnlineAlbum::class.java)
                        albums.add(album)
                    }
                }
                result.invoke(
                    UiState.Success(albums)
                )
            }
    }


    override fun addAlbum(album: OnlineAlbum, result: (UiState<String>) -> Unit) {
        val doc = database
            .collection(FireStoreCollection.ALBUM)
            .document()
        album.id = doc.id
        doc.set(album)
            .addOnSuccessListener {
                result.invoke(UiState.Success("Album ${album.name} added!"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun updateAlbum(album: OnlineAlbum, result: (UiState<String>) -> Unit) {
        database
            .collection(FireStoreCollection.ALBUM)
            .document(album.id!!)
            .update("name", album.name, "imgFilePath", album.imgFilePath, "songs", album.songs)
            .addOnSuccessListener {
                result.invoke(UiState.Success("Album ${album.name} updated!"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun deleteAlbum(album: OnlineAlbum, result: (UiState<String>) -> Unit) {
        database
            .collection(FireStoreCollection.ALBUM)
            .document(album.id.toString())
            .delete()
            .addOnSuccessListener {
                if (album.imgFilePath!!.isNotEmpty()){
                    val songRef = FirebaseStorage.getInstance().getReferenceFromUrl(album.imgFilePath.toString())
                    songRef.delete()
                        .addOnSuccessListener {
                            result.invoke(UiState.Success("Album ${album.name} deleted!"))
                        }
                        .addOnFailureListener {
                            result.invoke(UiState.Failure(it.localizedMessage))
                        }
                }
                result.invoke(UiState.Success("Album ${album.name} deleted!"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

}