package com.example.music.online.repositories

import com.example.music.utils.UiState
import com.example.music.online.data.dao.AlbumRepository
import com.example.music.online.data.dao.CountryRepository
import com.example.music.online.data.models.OnlineAlbum
import com.example.music.online.data.models.OnlineCountry
import com.example.music.utils.FireStoreCollection
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class CountryRepositoryImp(val database: FirebaseFirestore): CountryRepository {

    override fun getAllCountries(result: (UiState<List<OnlineCountry>>) -> Unit) {
        database
            .collection(FireStoreCollection.COUNTRY)
            .addSnapshotListener { value, _ ->
                val countries: ArrayList<OnlineCountry> = ArrayList()
                if (value != null) {
                    for (document in value){
                        val country = document.toObject(OnlineCountry::class.java)
                        countries.add(country)
                    }
                }
                result.invoke(
                    UiState.Success(countries)
                )
            }
    }


    override fun addCountry(country: OnlineCountry, result: (UiState<String>) -> Unit) {
        val doc = database
            .collection(FireStoreCollection.COUNTRY)
            .document()
        country.id = doc.id
        doc.set(country)
            .addOnSuccessListener {
                result.invoke(UiState.Success("Country ${country.name} added!"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun updateCountry(country: OnlineCountry, result: (UiState<String>) -> Unit) {
        database
            .collection(FireStoreCollection.COUNTRY)
            .document(country.id!!)
            .update("name", country.name, "imgFilePath", country.imgFilePath, "songs", country.songs)
            .addOnSuccessListener {
                result.invoke(UiState.Success("Country ${country.name} updated!"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun deleteCountry(country: OnlineCountry, result: (UiState<String>) -> Unit) {
        database
            .collection(FireStoreCollection.COUNTRY)
            .document(country.id.toString())
            .delete()
            .addOnSuccessListener {
                if (country.imgFilePath!!.isNotEmpty()){
                    val songRef = FirebaseStorage.getInstance().getReferenceFromUrl(country.imgFilePath.toString())
                    songRef.delete()
                        .addOnSuccessListener {
                            result.invoke(UiState.Success("Country ${country.name} deleted!"))
                        }
                        .addOnFailureListener {
                            result.invoke(UiState.Failure(it.localizedMessage))
                        }
                }
                result.invoke(UiState.Success("Country ${country.name} deleted!"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

}