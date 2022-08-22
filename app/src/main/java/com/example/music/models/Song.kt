package com.example.music.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "song")
data class Song(
    val uri: String,
    val name: String,
    val artists: String,
    val duration: Int,
    val size: Int,
    val playlistID: Int,
    val filePath: String,
    @PrimaryKey(autoGenerate = true)
    val id: Int
)
