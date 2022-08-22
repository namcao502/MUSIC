package com.example.music.viewModels

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.music.models.Song
import java.util.concurrent.TimeUnit

class ScanSongInStorage(context: Context) {

    private val collection: Uri =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            )
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

    private val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.DISPLAY_NAME,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.SIZE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DATA

    )

    // Show only audios that are at least 1 minute in duration.
    private val selection = "${MediaStore.Audio.Media.DURATION} >= ?"
    private val selectionArgs = arrayOf(
        TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES).toString()
    )

    // Display audios in alphabetical order based on their display name.
    private val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

    private val query = context.contentResolver.query(
        collection,
        projection,
        selection,
        selectionArgs,
        sortOrder
    )

    fun getAllSongs(): List<Song>{

        val songList = mutableListOf<Song>()

        query?.use { cursor ->
            // Cache column indices.
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val artistsColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val filePathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (cursor.moveToNext()) {
                // Get values of columns for a given audio.
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val duration = cursor.getInt(durationColumn)
                val size = cursor.getInt(sizeColumn)
                val artists = cursor.getString(artistsColumn)
                val filePath = cursor.getString(filePathColumn)

                val contentUri: Uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                // Stores column values and the contentUri in a local object
                // that represents the media file.
                songList.add(Song(contentUri.toString(), name, artists, duration, size, 0, filePath, 0))
            }
        }
        return songList
    }
}