package com.example.music.online.data.dao

import com.example.music.utils.UiState
import com.example.music.online.data.models.OnlineAlbum
import com.example.music.online.data.models.OnlineCountry

interface CountryRepository {

    fun getAllCountries(result: (UiState<List<OnlineCountry>>) -> Unit)

    fun addCountry(country: OnlineCountry, result: (UiState<String>) -> Unit)

    fun updateCountry(country: OnlineCountry, result: (UiState<String>) -> Unit)

    fun deleteCountry(country: OnlineCountry, result: (UiState<String>) -> Unit)

}