package com.example.music.online.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.music.online.data.dao.AuthenticationRepository
import com.example.music.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(private val repository: AuthenticationRepository): ViewModel() {

    private val _signIn = MutableLiveData<UiState<String>>()
    val signIn: LiveData<UiState<String>> get() = _signIn

    private val _signUp = MutableLiveData<UiState<String>>()
    val signUp: LiveData<UiState<String>> get() = _signUp

    private val _signOut = MutableLiveData<UiState<String>>()
    val signOut: LiveData<UiState<String>> get() = _signOut

    private val _reset = MutableLiveData<UiState<String>>()
    val reset: LiveData<UiState<String>> get() = _reset

    fun signInWithEmailPassword(email: String, password: String){
        _signIn.value = UiState.Loading
        repository.signInWithEmailPassword(email, password){
            _signIn.value = it
        }
    }

    fun signUpWithEmailPassword(email: String, password: String){
        _signUp.value = UiState.Loading
        repository.signUpWithEmailPassword(email, password){
            _signUp.value = it
        }
    }


    fun signOut(){
        _signOut.value = UiState.Loading
        repository.signOut{
            _signOut.value = it
        }
    }


    fun sendPasswordReset(email: String){
        _reset.value = UiState.Loading
        repository.sendPasswordReset(email){
            _reset.value = it
        }
    }

}
