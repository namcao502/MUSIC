package com.example.music.online.repositories

import com.example.music.utils.UiState
import com.example.music.online.data.dao.AccountRepository
import com.example.music.online.data.models.OnlineAccount
import com.example.music.utils.FireStoreCollection
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class AccountRepositoryImp(val database: FirebaseFirestore): AccountRepository {

    override fun getAllAccounts(result: (UiState<List<OnlineAccount>>) -> Unit) {
        database
            .collection(FireStoreCollection.ACCOUNT).orderBy("name")
            .addSnapshotListener { value, _ ->
                val accounts: ArrayList<OnlineAccount> = ArrayList()
                if (value != null) {
                    for (document in value){
                        val account = document.toObject(OnlineAccount::class.java)
                        accounts.add(account)
                    }
                }
                result.invoke(
                    UiState.Success(accounts)
                )
            }
    }

    override fun getAccountByID(id: String, result: (UiState<OnlineAccount>) -> Unit) {
        database
            .collection(FireStoreCollection.ACCOUNT)
            .whereEqualTo("userID", id)
            .addSnapshotListener { value, _ ->
                val accounts: ArrayList<OnlineAccount> = ArrayList()
                if (value != null){
                    for (document in value){
                        val account = document.toObject(OnlineAccount::class.java)
                        accounts.add(account)
                    }
                }
                result.invoke(
                    UiState.Success(accounts[0])
                )
            }
    }


    override fun addAccount(account: OnlineAccount, result: (UiState<String>) -> Unit) {
        val doc = database
            .collection(FireStoreCollection.ACCOUNT)
            .document()
        account.id = doc.id

        doc.set(account)
            .addOnSuccessListener {
                result.invoke(UiState.Success("Account ${account.name} added!"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun updateAccount(account: OnlineAccount, result: (UiState<String>) -> Unit) {
        //attention
        database
            .collection(FireStoreCollection.ACCOUNT)
            .document(account.id!!)
            .update("name", account.name,
                "imgFilePath", account.imgFilePath,
                                    "email", account.email, "password", account.password, "role", account.role)
            .addOnSuccessListener {
                result.invoke(UiState.Success("Account ${account.name} updated!"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun deleteAccount(account: OnlineAccount, result: (UiState<String>) -> Unit) {
        database
            .collection(FireStoreCollection.ACCOUNT)
            .document(account.id.toString())
            .delete()
            .addOnSuccessListener {
                //delete img
                if (account.imgFilePath!!.isNotEmpty()){
                    val songRef = FirebaseStorage.getInstance().getReferenceFromUrl(account.imgFilePath.toString())
                    songRef.delete()
                        .addOnSuccessListener {
                            result.invoke(UiState.Success("Account ${account.name} deleted!"))
                        }
                        .addOnFailureListener {
                            result.invoke(UiState.Failure(it.localizedMessage))
                        }
                }
                result.invoke(UiState.Success("Account ${account.name} deleted!"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

}