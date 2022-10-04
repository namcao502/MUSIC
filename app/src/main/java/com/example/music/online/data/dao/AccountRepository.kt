package com.example.music.online.data.dao

import com.example.music.utils.UiState
import com.example.music.online.data.models.OnlineAccount

interface AccountRepository {

    fun getAllAccounts(result: (UiState<List<OnlineAccount>>) -> Unit)

    fun getAccountByID(id: String, result: (UiState<OnlineAccount>) -> Unit)

    fun addAccount(account: OnlineAccount, result: (UiState<String>) -> Unit)

    fun updateAccount(account: OnlineAccount, result: (UiState<String>) -> Unit)

    fun deleteAccount(account: OnlineAccount, result: (UiState<String>) -> Unit)

}