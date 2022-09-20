package com.example.music.data.offline

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.music.data.models.offline.Playlist

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