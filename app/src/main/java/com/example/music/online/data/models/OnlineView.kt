package com.example.music.online.data.models

data class OnlineView(
    var id: String? = "",
    var modelId: String? = "",
    var modelName: String? = "",
    var quantity: Int? = 0
) {
    override fun toString(): String {
        return "$modelName, $quantity"
    }
}