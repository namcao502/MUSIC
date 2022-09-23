package com.example.music.data.models.online

data class OnlineAlbum(
    var id: String? = "",
    var name: String? = "",
    var songs: List<String>? = emptyList(),
    var imgFilePath: String? = ""
) {
    override fun toString(): String {
        return "ID = $id, name = $name"
    }
}