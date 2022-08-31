package com.example.music.repositories

import androidx.lifecycle.LiveData
import com.example.music.data.PlaylistDao
import com.example.music.models.Playlist
import com.example.music.models.PlaylistWithSongs
import com.example.music.models.SongPlaylistCrossRef
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