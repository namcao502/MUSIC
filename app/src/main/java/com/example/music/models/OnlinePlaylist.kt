package com.example.music.models

import java.io.Serializable

data class OnlinePlaylist(
    var id: String? = "",
    var name: String? = "",
    val songs: List<String>? = emptyList()
) : Serializable