package com.example.music.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.music.models.*

@Dao
interface SongInPlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSongPlaylistCrossRef(crossRef: SongPlaylistCrossRef)

    @Delete
    suspend fun deleteSongInPlaylistCrossRef(crossRef: SongPlaylistCrossRef)

    @Transaction
    @Query("SELECT * FROM playlist WHERE playlist_id = :playlistId")
    fun getSongsOfPlaylist(playlistId: Int): LiveData<PlaylistWithSongs>

    @Transaction
    @Query("SELECT * FROM song WHERE song_id = :songId")
    fun getPlaylistsOfSong(songId: Int): LiveData<List<SongWithPlaylists>>

}