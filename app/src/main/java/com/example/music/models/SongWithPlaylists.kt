package com.example.music.models

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class SongWithPlaylists(
    @Embedded val song: Song,
    @Relation(
        parentColumn = "song_id",
        entityColumn = "playlist_id",
        associateBy = Junction(SongPlaylistCrossRef::class)
    )
    val playlists: List<Playlist>
)