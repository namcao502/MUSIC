package com.example.music.data.models.offline

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class PlaylistWithSongs(
    @Embedded val playlist: Playlist,
    @Relation(
                parentColumn = "playlist_id",
                entityColumn = "song_id",
                associateBy = Junction(SongPlaylistCrossRef::class)
        )
        val listSong: List<Song>
)