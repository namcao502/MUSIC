package com.example.music.viewModels.online

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.music.UiState
import com.example.music.data.firebase.AccountRepository
import com.example.music.data.firebase.ArtistRepository
import com.example.music.data.firebase.FirebaseRepository
import com.example.music.data.models.online.OnlineAccount
import com.example.music.data.models.online.OnlineArtist
import com.example.music.data.models.online.OnlinePlaylist
import com.example.music.data.models.online.OnlineSong
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class OnlineAccountViewModel @Inject constructor(val repository: AccountRepository): ViewModel(){

    private val _addAccount = MutableLiveData<UiState<String>>()
    val addAccount: LiveData<UiState<String>> get() = _addAccount

    private val _deleteAccount = MutableLiveData<UiState<String>>()
    val deleteAccount: LiveData<UiState<String>> get() = _deleteAccount

    private val _updateAccount = MutableLiveData<UiState<String>>()
    val updateAccount: LiveData<UiState<String>> get() = _updateAccount

    private val _accounts = MutableLiveData<UiState<List<OnlineAccount>>>()
    val account: LiveData<UiState<List<OnlineAccount>>> get() = _accounts

    private val _accountByID = MutableLiveData<UiState<OnlineAccount>>()
    val accountByID: LiveData<UiState<OnlineAccount>> get() = _accountByID

    fun getAllAccounts() {
        _accounts.value = UiState.Loading
        repository.getAllAccounts {
            _accounts.value = it
        }
    }

    fun getAccountByID(id: String){
        _accountByID.value = UiState.Loading
        repository.getAccountByID(id){
            _accountByID.value = it
        }
    }

    fun addAccount(account: OnlineAccount){
        _addAccount.value = UiState.Loading
        repository.addAccount(account){
            _addAccount.value = it
        }
    }

    fun deleteAccount(account: OnlineAccount){
        _deleteAccount.value = UiState.Loading
        repository.deleteAccount(account){
            _deleteAccount.value = it
        }
    }

    fun updateAccount(account: OnlineAccount){
        _updateAccount.value = UiState.Loading
        repository.updateAccount(account){
            _updateAccount.value = it
        }
    }

}