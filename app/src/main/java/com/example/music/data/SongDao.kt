package com.example.music.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.music.models.Song

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
}