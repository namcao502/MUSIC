package com.example.music.data.models.online

import java.io.Serializable

data class OnlineSong(
    var id: String? = "",
    val name: String? = "",
    val imgFilePath: String? = "",
    val filePath: String? = ""
) : Serializable {
    override fun toString(): String {
        return "ID = $id, name = $name"
    }
}