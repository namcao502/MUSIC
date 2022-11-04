package com.example.music.online.repositories

import android.util.Log
import com.example.music.online.data.dao.AuthenticationRepository
import com.example.music.online.data.dao.BaseAuthenticator
import com.example.music.online.data.models.OnlineAccount
import com.example.music.utils.FireStoreCollection
import com.example.music.utils.UiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class AuthenticationRepositoryImp(val auth: FirebaseAuth,
                                  val database: FirebaseFirestore): AuthenticationRepository {

    override fun signUpWithEmailPassword(email: String, password: String, result: (UiState<String>) -> Unit) {

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                result.invoke(UiState.Success("Account created"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure("Can't create account for you, sorry..."))
            }

        //add user's information to user collection
        val doc = database.collection(FireStoreCollection.ACCOUNT).document()
        val user = OnlineAccount(doc.id, Firebase.auth.currentUser!!.uid,"", email, password, "", "")

        doc.set(user)
            .addOnSuccessListener {
                Log.i("TAG502", "signUpWithEmailPassword: OK")
            }
            .addOnFailureListener {
                Log.i("TAG502", "signUpWithEmailPassword: NOT OK")
            }
    }

    override fun signInWithEmailPassword(email: String, password: String, result: (UiState<String>) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                result.invoke(UiState.Success("Signed in!!!"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure("Can't let you in..."))
            }
    }

    override fun signOut(result: (UiState<String>) -> Unit) {
        auth.signOut()
        if (auth.currentUser == null){
            result.invoke(UiState.Success("Signed out successful!!!"))
        }
        else {
            result.invoke(UiState.Failure("Can't leave"))
        }
    }

    override fun sendPasswordReset(email: String, result: (UiState<String>) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                result.invoke(UiState.Success("Check your email"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure("Can't send..."))
            }
    }
}
