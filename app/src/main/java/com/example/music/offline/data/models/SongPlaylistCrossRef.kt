package com.example.music.offline.data.models

import androidx.room.Entity

@Entity(primaryKeys = ["song_id", "playlist_id"])
data class SongPlaylistCrossRef(
    val song_id: Int,
    val playlist_id: Int
)