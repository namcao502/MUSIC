package com.example.music.data.firebase

import com.example.music.models.OnlineSong

interface FirebaseRP {
    fun getAllSongs(): List<OnlineSong>
}