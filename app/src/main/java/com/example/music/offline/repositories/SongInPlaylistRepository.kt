package com.example.music.offline.repositories

import androidx.lifecycle.LiveData
import com.example.music.offline.data.dao.SongInPlaylistDao
import com.example.music.offline.data.models.PlaylistWithSongs
import com.example.music.offline.data.models.SongPlaylistCrossRef
import javax.inject.Inject

class SongInPlaylistRepository @Inject constructor(private val songInPlaylistDao: SongInPlaylistDao) {

    suspend fun addSongPlaylistCrossRef(songPlaylistCrossRef: SongPlaylistCrossRef){
        return songInPlaylistDao.addSongPlaylistCrossRef(songPlaylistCrossRef)
    }

    suspend fun deleteSongPlaylistCrossRef(songPlaylistCrossRef: SongPlaylistCrossRef){
        return songInPlaylistDao.deleteSongInPlaylistCrossRef(songPlaylistCrossRef)
    }

    fun getSongsOfPlaylist(playlistId: Int): LiveData<PlaylistWithSongs>{
        return songInPlaylistDao.getSongsOfPlaylist(playlistId)
    }

}