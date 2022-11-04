package com.example.music.online.data.dao

import com.example.music.utils.UiState


interface AuthenticationRepository {

    fun signUpWithEmailPassword(email: String, password: String, result: (UiState<String>) -> Unit)

    fun signInWithEmailPassword(email: String, password: String, result: (UiState<String>) -> Unit)

    fun signOut(result: (UiState<String>) -> Unit)

    fun sendPasswordReset(email: String, result: (UiState<String>) -> Unit)
}
