package com.example.music.data.models.online

data class OnlineArtist(
    var id: String? = "",
    var name: String? = "",
    val songs: List<String>? = emptyList(),
    val imgFilePath: String? = ""
)