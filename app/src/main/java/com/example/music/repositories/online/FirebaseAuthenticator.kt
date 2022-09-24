package com.example.music.repositories.online

import android.util.Log
import com.example.music.data.firebase.BaseAuthenticator
import com.example.music.data.models.online.OnlineAccount
import com.example.music.utils.FireStoreCollection
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import kotlinx.coroutines.tasks.await

class FirebaseAuthenticator: BaseAuthenticator {

    override suspend fun signUpWithEmailPassword(email: String, password: String): FirebaseUser? {
        Firebase.auth.createUserWithEmailAndPassword(email, password).await()
        val doc = FirebaseFirestore.getInstance().collection(FireStoreCollection.USER).document()
        val user = OnlineAccount(Firebase.auth.currentUser!!.uid, "", email, password, "", "")

        doc.set(user)
            .addOnSuccessListener {
                Log.i("TAG502", "signUpWithEmailPassword: OK")
            }
            .addOnFailureListener {
                Log.i("TAG502", "signUpWithEmailPassword: NOT OK")
            }
        return Firebase.auth.currentUser
    }

    override suspend fun signInWithEmailPassword(email: String, password: String): FirebaseUser? {
        Firebase.auth.signInWithEmailAndPassword(email, password).await()
        return Firebase.auth.currentUser
    }

    override fun signOut(): FirebaseUser? {
        Firebase.auth.signOut()
        return Firebase.auth.currentUser
    }

    override fun getUser(): FirebaseUser? {
        return Firebase.auth.currentUser
    }

    override suspend fun sendPasswordReset(email: String) {
        Firebase.auth.sendPasswordResetEmail(email).await()
    }
}
