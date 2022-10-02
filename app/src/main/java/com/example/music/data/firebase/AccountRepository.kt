package com.example.music.data.firebase

import com.example.music.UiState
import com.example.music.data.models.online.OnlineAccount

interface AccountRepository {

    fun getAllAccounts(result: (UiState<List<OnlineAccount>>) -> Unit)

    fun getAccountByID(id: String, result: (UiState<OnlineAccount>) -> Unit)

    fun addAccount(account: OnlineAccount, result: (UiState<String>) -> Unit)

    fun updateAccount(account: OnlineAccount, result: (UiState<String>) -> Unit)

    fun deleteAccount(account: OnlineAccount, result: (UiState<String>) -> Unit)

}