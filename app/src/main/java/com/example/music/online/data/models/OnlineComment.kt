package com.example.music.online.data.models

data class OnlineComment(
    var id: String? = "",
    var message: String? = "",
    val songId: String? = "",
    val userId: String? = ""
) {
    override fun toString(): String {
        return "$message, $id"
    }
}