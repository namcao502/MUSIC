package com.example.music.repositories.offline

import androidx.lifecycle.LiveData
import com.example.music.data.offline.SongInPlaylistDao
import com.example.music.data.models.offline.PlaylistWithSongs
import com.example.music.data.models.offline.SongPlaylistCrossRef
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