package com.example.music.online.data.models

data class OnlineAccount(
    var id: String? = "",
    var userID: String? = "",
    var name: String? = "",
    var email: String? = "",
    var password: String? = "",
    var role: String? = "",
    var imgFilePath: String? = ""
) {
    override fun toString(): String {
        return "$email, $name"
    }
}