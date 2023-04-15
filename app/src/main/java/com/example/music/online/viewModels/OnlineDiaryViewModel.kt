package com.example.music.online.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.music.online.data.dao.DiaryRepository
import com.example.music.online.data.models.OnlineDiary
import com.example.music.utils.UiState
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnlineDiaryViewModel @Inject constructor(val repository: DiaryRepository): ViewModel(){

    private val _addDiary = MutableLiveData<UiState<String>>()
    val addDiary: LiveData<UiState<String>> get() = _addDiary

    private val _deleteDiary = MutableLiveData<UiState<String>>()
    val deleteDiary: LiveData<UiState<String>> get() = _deleteDiary

    private val _updateDiary = MutableLiveData<UiState<String>>()
    val updateDiary: LiveData<UiState<String>> get() = _updateDiary

    private val _diaries = MutableLiveData<UiState<List<OnlineDiary>>>()
    val diaries: LiveData<UiState<List<OnlineDiary>>> get() = _diaries

    fun getAllDiaries(user: FirebaseUser) {
        _diaries.value = UiState.Loading
        repository.getAllDiaries(user) {
            _diaries.value = it
        }
    }

    fun addDiary(diary: OnlineDiary, user: FirebaseUser){
        _addDiary.value = UiState.Loading
        repository.addDiary(diary, user){
            _addDiary.value = it
        }
    }

    fun deleteDiary(diary: OnlineDiary, user: FirebaseUser){
        _deleteDiary.value = UiState.Loading
        repository.deleteDiary(diary, user){
            _deleteDiary.value = it
        }
    }

    fun updateDiary(diary: OnlineDiary, user: FirebaseUser){
        _updateDiary.value = UiState.Loading
        repository.updateDiary(diary, user){
            _updateDiary.value = it
        }
    }

}