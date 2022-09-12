package com.example.music.models

import java.io.Serializable

data class OnlineSong(
    val id: String,
    val name: String,
    val filePath: String,
    val imgFilePath: String
) : Serializable