package com.example.music.online.repositories

import com.example.music.online.data.dao.DiaryRepository
import com.example.music.utils.UiState
import com.example.music.online.data.dao.PlaylistRepository
import com.example.music.online.data.models.OnlineDiary
import com.example.music.online.data.models.OnlinePlaylist
import com.example.music.online.data.models.OnlineSong
import com.example.music.online.data.models.OnlineView
import com.example.music.utils.FireStoreCollection
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage

class DiaryRepositoryImp(val database: FirebaseFirestore): DiaryRepository {
    override fun getAllDiaries(user: FirebaseUser, result: (UiState<List<OnlineDiary>>) -> Unit) {
        database
            .collection(FireStoreCollection.DIARY)
            .document(user.uid)
            .collection(FireStoreCollection.USER).orderBy("subject")
            .addSnapshotListener { value, _ ->
                val diaries: ArrayList<OnlineDiary> = ArrayList()
                if (value != null) {
                    for (document in value){
                        diaries.add(document.toObject(OnlineDiary::class.java))
                    }
                }
                result.invoke(
                    UiState.Success(diaries)
                )
            }
    }

    override fun addDiary(diary: OnlineDiary, user: FirebaseUser, result: (UiState<String>) -> Unit) {
        val doc = database
            .collection(FireStoreCollection.DIARY)
            .document(user.uid)
            .collection(FireStoreCollection.USER).document()
        diary.id = doc.id

        doc.set(diary)
            .addOnSuccessListener {
                result.invoke(UiState.Success("Diary ${diary.subject} added!"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun updateDiary(diary: OnlineDiary, user: FirebaseUser, result: (UiState<String>) -> Unit) {
        diary.id?.let { diaryID ->
            database
                .collection(FireStoreCollection.DIARY)
                .document(user.uid)
                .collection(FireStoreCollection.USER)
                .document(diaryID)
                .update("subject", diary.subject,
                    "content", diary.content, "dateTime", diary.dateTime
                )
                .addOnSuccessListener {
                    result.invoke(UiState.Success("Diary ${diary.subject} updated!"))
                }
                .addOnFailureListener {
                    result.invoke(UiState.Failure(it.localizedMessage))
                }
        }
    }

    override fun deleteDiary(diary: OnlineDiary, user: FirebaseUser, result: (UiState<String>) -> Unit) {
        diary.id?.let { diaryID ->
            database
                .collection(FireStoreCollection.DIARY)
                .document(user.uid)
                .collection(FireStoreCollection.USER)
                .document(diaryID)
                .delete()
                .addOnSuccessListener {
                    result.invoke(UiState.Success("Diary ${diary.subject} deleted!"))
                }
                .addOnFailureListener {
                    result.invoke(UiState.Failure(it.localizedMessage))
                }
        }
    }

}