package com.example.music.data.models.online

data class OnlinePlaylist(
    var id: String? = "",
    var name: String? = "",
    val songs: List<String>? = emptyList()
)