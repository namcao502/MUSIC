package com.example.music.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.music.models.Playlist
import com.example.music.models.PlaylistWithSongs
import com.example.music.models.SongPlaylistCrossRef
import com.example.music.models.SongWithPlaylists

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addPlaylist(playlist: Playlist)

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    @Update
    suspend fun updatePlaylist(playlist: Playlist)

    @Query("SELECT * FROM playlist ORDER BY name ASC")
    fun readAllPlaylists(): LiveData<List<Playlist>>

}