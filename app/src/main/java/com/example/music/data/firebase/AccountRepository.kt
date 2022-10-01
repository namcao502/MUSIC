package com.example.music.data.firebase

import android.net.Uri
import com.example.music.UiState
import com.example.music.data.models.online.OnlineAccount
import com.example.music.data.models.online.OnlineArtist
import com.example.music.data.models.online.OnlinePlaylist
import com.example.music.data.models.online.OnlineSong
import com.google.firebase.auth.FirebaseUser

interface AccountRepository {

    fun getAllAccounts(result: (UiState<List<OnlineAccount>>) -> Unit)

    fun getAccountByID(id: String, result: (UiState<OnlineAccount>) -> Unit)

    fun addAccount(account: OnlineAccount, result: (UiState<String>) -> Unit)

    fun updateAccount(account: OnlineAccount, result: (UiState<String>) -> Unit)

    fun deleteAccount(account: OnlineAccount, result: (UiState<String>) -> Unit)

}