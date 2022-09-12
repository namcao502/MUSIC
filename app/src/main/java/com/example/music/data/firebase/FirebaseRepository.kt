package com.example.music.data.firebase

import com.example.music.models.OnlineSong
import com.google.firebase.firestore.FirebaseFirestore


class FirebaseRepository(private val database: FirebaseFirestore): FirebaseRP {

    override fun getAllSongs(): List<OnlineSong> {
        val songs: ArrayList<OnlineSong> = ArrayList()
        database.collection("OnlineSong")
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful){
                    for (document in it.result){
                        val song = document.toObject(OnlineSong::class.java)
                        songs.add(song)
                    }
                }
            }
        return songs
    }
}