package com.example.music.data.models.online

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
        return "Email = $email, Name = $name"
    }
}