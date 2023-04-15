package com.example.music.online.data.models

import java.io.Serializable

data class OnlineDiary(
    var id: String? = "",
    var subject: String? = "",
    var content: String? = "",
    var dateTime: String? = "",
    var from: String? = "",
    var songId: String? = ""
) : Serializable {
    override fun toString(): String {
        return "OnlineDiary(id=$id, subject=$subject, content=$content, from=$from, songId=$songId)"
    }
}