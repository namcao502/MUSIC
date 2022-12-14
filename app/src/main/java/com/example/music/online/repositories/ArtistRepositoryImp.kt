package com.example.music.online.repositories

import com.example.music.utils.UiState
import com.example.music.online.data.dao.ArtistRepository
import com.example.music.online.data.models.OnlineArtist
import com.example.music.online.data.models.OnlineSong
import com.example.music.online.data.models.OnlineView
import com.example.music.utils.FireStoreCollection
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ArtistRepositoryImp(val database: FirebaseFirestore): ArtistRepository {

    override fun getAllArtists(result: (UiState<List<OnlineArtist>>) -> Unit) {
        database
            .collection(FireStoreCollection.ARTIST).orderBy("name")
            .addSnapshotListener { value, _ ->
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

        val viewTemp = OnlineView("", doc.id, FireStoreCollection.ARTIST, 0)
        val viewRef = database
            .collection(FireStoreCollection.VIEW)
            .document()

        viewTemp.id = viewRef.id
        viewRef.set(viewTemp)
            .addOnSuccessListener {
                result.invoke(UiState.Success("View Added!"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun updateArtist(artist: OnlineArtist, result: (UiState<String>) -> Unit) {
        database
            .collection(FireStoreCollection.ARTIST)
            .document(artist.id!!)
            .update("name", artist.name, "imgFilePath", artist.imgFilePath, "songs", artist.songs)
            .addOnSuccessListener {
                result.invoke(UiState.Success("Artist ${artist.name} updated!"))
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
                            result.invoke(UiState.Success("Artist ${artist.name} deleted!"))
                        }
                        .addOnFailureListener {
                            result.invoke(UiState.Failure(it.localizedMessage))
                        }
                }
                result.invoke(UiState.Success("Artist ${artist.name} deleted!"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }

        //delete view
        database
            .collection(FireStoreCollection.VIEW)
            .whereEqualTo("modelId", artist.id)
            .get()
            .addOnSuccessListener { value ->
                if (value != null){
                    for (doc in value){
                        doc.reference.delete()
                            .addOnSuccessListener {
                                result.invoke(UiState.Success("View deleted!!!"))
                            }
                            .addOnFailureListener {
                                result.invoke(UiState.Failure(it.toString()))
                            }
                    }
                }
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.toString()))
            }

    }

    override fun getAllArtistFromSong(song: OnlineSong, result: (UiState<List<OnlineArtist>>) -> Unit) {
        database
            .collection(FireStoreCollection.ARTIST)
            .whereArrayContains("songs", song.id!!)
            .addSnapshotListener { value, _ ->
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

    override fun getAllArtistFromSongID(songId: String, result: (UiState<List<OnlineArtist>>) -> Unit) {
        database
            .collection(FireStoreCollection.ARTIST)
            .whereArrayContains("songs", songId)
            .addSnapshotListener { value, _ ->
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

}