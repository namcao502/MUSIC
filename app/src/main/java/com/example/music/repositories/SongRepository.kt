package com.example.music.repositories

import androidx.lifecycle.LiveData
import com.example.music.data.SongDao
import com.example.music.models.Song
import javax.inject.Inject

class SongRepository @Inject constructor(private val songDao: SongDao) {

    fun readAllSongs(): LiveData<List<Song>> {
        return songDao.readAllSongs()
    }

    suspend fun addSong(song: Song) {
        songDao.addSong(song)
    }

    suspend fun updateSong(song: Song){
        songDao.updateSong(song)
    }

    suspend fun deleteSong(song: Song){
        songDao.deleteSong(song)
    }

    suspend fun deleteAllSongs(){
        songDao.deleteAllSongs()
    }

}