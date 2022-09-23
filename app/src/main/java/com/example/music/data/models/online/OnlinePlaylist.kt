package com.example.music.data.models.online

data class OnlinePlaylist(
    var id: String? = "",
    var name: String? = "",
    var songs: List<String>? = emptyList(),
    var imgFilePath: String? = ""
) {
    override fun toString(): String {
        return "ID = $id, name = $name"
    }
}