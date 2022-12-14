package com.example.music.online.repositories

import com.example.music.utils.UiState
import com.example.music.online.data.dao.GenreRepository
import com.example.music.online.data.models.OnlineGenre
import com.example.music.online.data.models.OnlineView
import com.example.music.utils.FireStoreCollection
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class GenreRepositoryImp(val database: FirebaseFirestore): GenreRepository {

    override fun getAllGenres(result: (UiState<List<OnlineGenre>>) -> Unit) {
        database
            .collection(FireStoreCollection.GENRE).orderBy("name")
            .addSnapshotListener { value, _ ->
                val genres: ArrayList<OnlineGenre> = ArrayList()
                if (value != null) {
                    for (document in value){
                        val genre = document.toObject(OnlineGenre::class.java)
                        genres.add(genre)
                    }
                }
                result.invoke(
                    UiState.Success(genres)
                )
            }
    }

    override fun addGenre(genre: OnlineGenre, result: (UiState<String>) -> Unit) {
        val doc = database
            .collection(FireStoreCollection.GENRE)
            .document()
        genre.id = doc.id
        doc.set(genre)
            .addOnSuccessListener {
                result.invoke(UiState.Success("Genre ${genre.name} added!"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }

        val viewTemp = OnlineView("", doc.id, FireStoreCollection.GENRE, 0)
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

    override fun updateGenre(genre: OnlineGenre, result: (UiState<String>) -> Unit) {
        database
            .collection(FireStoreCollection.GENRE)
            .document(genre.id!!)
            .update("name", genre.name, "imgFilePath", genre.imgFilePath, "songs", genre.songs)
            .addOnSuccessListener {
                result.invoke(UiState.Success("Genre ${genre.name} updated!"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun deleteGenre(genre: OnlineGenre, result: (UiState<String>) -> Unit) {
        database
            .collection(FireStoreCollection.GENRE)
            .document(genre.id.toString())
            .delete()
            .addOnSuccessListener {
                if (genre.imgFilePath!!.isNotEmpty()){
                    val songRef = FirebaseStorage.getInstance().getReferenceFromUrl(genre.imgFilePath.toString())
                    songRef.delete()
                        .addOnSuccessListener {
                            result.invoke(UiState.Success("Genre ${genre.name} deleted!"))
                        }
                        .addOnFailureListener {
                            result.invoke(UiState.Failure(it.localizedMessage))
                        }
                }
                result.invoke(UiState.Success("Genre ${genre.name} deleted!"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }

        //delete view
        database
            .collection(FireStoreCollection.VIEW)
            .whereEqualTo("modelId", genre.id)
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

}