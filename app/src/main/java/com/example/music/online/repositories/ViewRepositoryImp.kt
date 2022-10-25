package com.example.music.online.repositories

import com.example.music.utils.UiState
import com.example.music.online.data.dao.AlbumRepository
import com.example.music.online.data.dao.ViewRepository
import com.example.music.online.data.models.OnlineAlbum
import com.example.music.online.data.models.OnlineView
import com.example.music.utils.FireStoreCollection
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ViewRepositoryImp(val database: FirebaseFirestore): ViewRepository {

    override fun getViewForModel(modelId: String, result: (UiState<Int>) -> Unit) {
        database
            .collection(FireStoreCollection.VIEW)
            .document(modelId)
            .addSnapshotListener { value, _ ->
                if (value != null) {
                    val view = value.toObject(OnlineView::class.java)
                    result.invoke(UiState.Success(view!!.quantity!!))
                }
            }
    }

    override fun updateViewForModel(modelId: String, result: (UiState<String>) -> Unit) {
        database
            .collection(FireStoreCollection.VIEW)
            .whereEqualTo("modelId", modelId)
            .get()
            .addOnSuccessListener { value ->
                if (value != null){
                    for (doc in value){
                        doc.reference.update("quantity", FieldValue.increment(1))
                            .addOnSuccessListener {
                                result.invoke(UiState.Success("View updated!!!"))
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

    override fun deleteViewForModel(modelId: String, result: (UiState<String>) -> Unit) {
        database
            .collection(FireStoreCollection.VIEW)
            .document(modelId)
            .delete()
            .addOnSuccessListener {
                result.invoke(UiState.Success("View deleted!!!"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.toString()))
            }
    }

    override fun addViewForModel(view: OnlineView, result: (UiState<String>) -> Unit) {

        val doc = database
            .collection(FireStoreCollection.VIEW)
            .document()

        view.id = doc.id
        doc.set(view)
            .addOnSuccessListener {
                result.invoke(UiState.Success("Added!"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

}