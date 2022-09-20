package com.example.music.data.offline

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.music.data.models.offline.Song

@Dao
interface SongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSong(song: Song)

    @Delete
    suspend fun deleteSong(song: Song)

    @Update
    suspend fun updateSong(song: Song)

    @Query("SELECT * FROM song ORDER BY name ASC")
    fun readAllSongs(): LiveData<List<Song>>

    @Query("DELETE FROM song")
    fun deleteAllSongs()
}