package com.example.music.data.models.online

data class OnlineArtist(
    var id: String? = "",
    var name: String? = "",
    val songs: List<String>? = emptyList(),
    var imgFilePath: String? = ""
) {
    override fun toString(): String {
        return "ID = $id, name = $name"
    }
}