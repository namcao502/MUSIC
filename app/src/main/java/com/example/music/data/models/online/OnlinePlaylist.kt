package com.example.music.data.models.online

import java.io.Serializable

data class OnlinePlaylist(
    var id: String? = "",
    var name: String? = "",
    var songs: List<String>? = emptyList(),
    var imgFilePath: String? = ""
) : Serializable {
    override fun toString(): String {
        return "ID = $id, name = $name"
    }
}