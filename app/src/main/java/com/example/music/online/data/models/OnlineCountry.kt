package com.example.music.online.data.models

data class OnlineCountry(
    var id: String? = "",
    var name: String? = "",
    var songs: List<String>? = emptyList(),
    var imgFilePath: String? = ""
) {
    override fun toString(): String {
        return "ID = $id, name = $name"
    }
}