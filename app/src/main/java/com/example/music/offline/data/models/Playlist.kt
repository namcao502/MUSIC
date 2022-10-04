package com.example.music.offline.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlist")
data class Playlist(
    @PrimaryKey(autoGenerate = true)
    val playlist_id: Int,
    val name: String
)