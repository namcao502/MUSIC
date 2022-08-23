package com.example.music.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.music.models.Playlist
import com.example.music.models.Song
import com.example.music.models.SongPlaylistCrossRef

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