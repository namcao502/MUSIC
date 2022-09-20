package com.example.music.data.models.offline

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "song")
data class Song(
    val uri: String,
    val name: String,
    val artists: String,
    val duration: Int,
    val size: Int,
    val filePath: String,
    @PrimaryKey
    val song_id: Int
): Serializable
