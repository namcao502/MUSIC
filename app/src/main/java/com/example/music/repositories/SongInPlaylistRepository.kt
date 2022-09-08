package com.example.music.repositories

import androidx.lifecycle.LiveData
import com.example.music.data.local.SongInPlaylistDao
import com.example.music.models.PlaylistWithSongs
import com.example.music.models.SongPlaylistCrossRef
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