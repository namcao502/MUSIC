package com.example.music.offline.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.music.offline.data.models.Playlist

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