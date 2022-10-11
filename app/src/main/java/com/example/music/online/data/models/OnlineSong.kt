package com.example.music.online.data.models

import java.io.Serializable

data class OnlineSong(
    var id: String? = "",
    var name: String? = "",
    var imgFilePath: String? = "",
    var filePath: String? = "",
    var views: String? = "0"
) : Serializable {

    override fun toString(): String {
        return "$name"
    }

}