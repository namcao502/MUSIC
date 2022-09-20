package com.example.music.data.models.online

data class OnlineAccount(
    var id: String? = "",
    var name: String? = "",
    val email: String? = "",
    val password: String? = "",
    val role: String? = ""
)