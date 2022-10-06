package com.example.music.online.repositories

import com.example.music.utils.UiState
import com.example.music.online.data.dao.AlbumRepository
import com.example.music.online.data.dao.CommentRepository
import com.example.music.online.data.models.OnlineAlbum
import com.example.music.online.data.models.OnlineComment
import com.example.music.online.data.models.OnlineSong
import com.example.music.utils.FireStoreCollection
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class CommentRepositoryImp(val database: FirebaseFirestore): CommentRepository {
    override fun getAllComments(result: (UiState<List<OnlineComment>>) -> Unit) {
        database
            .collection(FireStoreCollection.COMMENT)
            .addSnapshotListener { value, _ ->
                val comments: ArrayList<OnlineComment> = ArrayList()
                if (value != null) {
                    for (document in value){
                        val comment = document.toObject(OnlineComment::class.java)
                        comments.add(comment)
                    }
                }
                result.invoke(
                    UiState.Success(comments)
                )
            }
    }

    override fun getAllCommentForSong(song: OnlineSong, result: (UiState<List<OnlineComment>>) -> Unit) {
        database
            .collection(FireStoreCollection.COMMENT)
            .whereEqualTo("songId", song.id)
            .addSnapshotListener { value, _ ->
                val comments: ArrayList<OnlineComment> = ArrayList()
                if (value != null) {
                    for (document in value){
                        val comment = document.toObject(OnlineComment::class.java)
                        comments.add(comment)
                    }
                }
                result.invoke(
                    UiState.Success(comments)
                )
            }
    }


    override fun addComment(comment: OnlineComment, result: (UiState<String>) -> Unit) {
        val doc = database
            .collection(FireStoreCollection.COMMENT)
            .document()
        comment.id = doc.id
        doc.set(comment)
            .addOnSuccessListener {
                result.invoke(UiState.Success("Comment ${comment.message} added!"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun updateComment(comment: OnlineComment, result: (UiState<String>) -> Unit) {
        database
            .collection(FireStoreCollection.COMMENT)
            .document(comment.id!!)
            .update("message", comment.message)
            .addOnSuccessListener {
                result.invoke(UiState.Success("Comment updated!"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun deleteComment(comment: OnlineComment, result: (UiState<String>) -> Unit) {
        database
            .collection(FireStoreCollection.COMMENT)
            .document(comment.id.toString())
            .delete()
            .addOnSuccessListener {
                result.invoke(UiState.Success("Comment ${comment.message} deleted!"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

}