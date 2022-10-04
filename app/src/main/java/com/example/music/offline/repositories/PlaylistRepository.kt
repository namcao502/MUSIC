package com.example.music.offline.repositories

import androidx.lifecycle.LiveData
import com.example.music.offline.data.dao.PlaylistDao
import com.example.music.offline.data.models.Playlist
import javax.inject.Inject

class PlaylistRepository @Inject constructor(private val playlistDao: PlaylistDao) {

    fun readAllPlaylists(): LiveData<List<Playlist>> {
        return playlistDao.readAllPlaylists()
    }

    suspend fun addPlaylist(playlist: Playlist) {
        playlistDao.addPlaylist(playlist)
    }

    suspend fun updatePlaylist(playlist: Playlist){
        playlistDao.updatePlaylist(playlist)
    }

    suspend fun deletePlaylist(playlist: Playlist){
        playlistDao.deletePlaylist(playlist)
    }

}