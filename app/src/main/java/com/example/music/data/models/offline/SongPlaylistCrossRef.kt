package com.example.music.data.models.offline

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(primaryKeys = ["song_id", "playlist_id"])
data class SongPlaylistCrossRef(
    val song_id: Int,
    val playlist_id: Int
)