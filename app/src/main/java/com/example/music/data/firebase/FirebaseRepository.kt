package com.example.music.data.firebase

import com.example.music.models.OnlineSong
import com.google.firebase.firestore.FirebaseFirestore


class FirebaseRepository(private val database: FirebaseFirestore) : FirebaseRP{
    override fun getAllSongs(): List<OnlineSong> {
        return emptyList()
    }
}