package com.example.music.online.data.models

import java.io.Serializable

data class OnlinePlaylist(
    var id: String? = "",
    var name: String? = "",
    var songs: List<String>? = emptyList(),
    var imgFilePath: String? = ""
) : Serializable {
    override fun toString(): String {
        return "$name"
    }
}