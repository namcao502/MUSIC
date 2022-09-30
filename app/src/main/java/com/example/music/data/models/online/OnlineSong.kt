package com.example.music.data.models.online

import java.io.Serializable

data class OnlineSong(
    var id: String? = "",
    var name: String? = "",
    var imgFilePath: String? = "",
    var filePath: String? = ""
) : Serializable {

    override fun toString(): String {
        return "$name"
    }

}