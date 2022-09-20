package com.example.music.data.models.online

data class OnlineAlbum(
    var id: String? = "",
    var name: String? = "",
    val songs: List<String>? = emptyList(),
    val imgFilePath: String? = ""
)