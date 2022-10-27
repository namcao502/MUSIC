package com.example.music.online.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.music.utils.UiState
import com.example.music.online.data.dao.AlbumRepository
import com.example.music.online.data.dao.ViewRepository
import com.example.music.online.data.models.OnlineAlbum
import com.example.music.online.data.models.OnlineSong
import com.example.music.online.data.models.OnlineView
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class OnlineViewViewModel @Inject constructor(val repository: ViewRepository): ViewModel(){

    private val _getView = MutableLiveData<UiState<Int>>()
    val getView: LiveData<UiState<Int>> get() = _getView

    private val _addView = MutableLiveData<UiState<String>>()
    val addView: LiveData<UiState<String>> get() = _addView

    private val _updateView = MutableLiveData<UiState<String>>()
    val updateView: LiveData<UiState<String>> get() = _updateView

    private val _deleteView = MutableLiveData<UiState<String>>()
    val deleteView: LiveData<UiState<String>> get() = _deleteView

    private val _getAllModelIDByName = MutableLiveData<UiState<List<String>>>()
    val getAllModelIDByName: LiveData<UiState<List<String>>> get() = _getAllModelIDByName

    private val _getTrendingSong = MutableLiveData<UiState<List<OnlineSong>>>()
    val getTrendingSong: LiveData<UiState<List<OnlineSong>>> get() = _getTrendingSong

    fun getViewForModel(modelId: String) {
        _getView.value = UiState.Loading
        repository.getViewForModel(modelId) {
            _getView.value = it
        }
    }

    fun addView(view: OnlineView){
        _addView.value = UiState.Loading
        repository.addViewForModel(view){
            _addView.value = it
        }
    }

    fun updateView(modelId: String){
        _updateView.value = UiState.Loading
        repository.updateViewForModel(modelId){
            _updateView.value = it
        }
    }

    fun deleteView(modelId: String){
        _deleteView.value = UiState.Loading
        repository.deleteViewForModel(modelId){
            _deleteView.value = it
        }
    }

    fun getAllModelIDByName(modelName: String){
        _getAllModelIDByName.value = UiState.Loading
        repository.getAllModelIDByName(modelName){
            _getAllModelIDByName.value = it
        }
    }

    fun getTrendingSong(listID: List<String>){
        _getTrendingSong.value = UiState.Loading
        repository.getTrendingSong(listID){
            _getTrendingSong.value = it
        }
    }

}