package com.example.music.online.data.dao

import com.example.music.online.data.models.OnlineSong
import com.example.music.online.data.models.OnlineView
import com.example.music.utils.UiState

interface ViewRepository {

    fun getViewForModel(modelId: String, result: (UiState<Int>) -> Unit)

    fun addViewForModel(view: OnlineView, result: (UiState<String>) -> Unit)

    fun updateViewForModel(modelId: String, result: (UiState<String>) -> Unit)

    fun deleteViewForModel(modelId: String, result: (UiState<String>) -> Unit)

    fun getAllModelIDByName(modelName: String, result: (UiState<List<String>>) -> Unit)

    fun getTrendingSong(listID: List<String>, result: (UiState<List<OnlineSong>>) -> Unit)

}