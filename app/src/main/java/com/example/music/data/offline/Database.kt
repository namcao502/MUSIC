package com.example.music.data.offline

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.music.data.models.offline.Playlist
import com.example.music.data.models.offline.Song
import com.example.music.data.models.offline.SongPlaylistCrossRef

@Database(
    entities = [
        Song::class,
        Playlist::class,
        SongPlaylistCrossRef::class],
    version = 1,
    exportSchema = false)
abstract class Database: RoomDatabase(){

    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun songInPlaylistDao(): SongInPlaylistDao

}