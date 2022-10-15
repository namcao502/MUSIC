package com.example.music.online.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.music.online.data.dao.CountryRepository
import com.example.music.online.data.models.OnlineCountry
import com.example.music.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class OnlineCountryViewModel @Inject constructor(val repository: CountryRepository): ViewModel(){

    private val _addCountry = MutableLiveData<UiState<String>>()
    val addCountry: LiveData<UiState<String>> get() = _addCountry

    private val _deleteCountry = MutableLiveData<UiState<String>>()
    val deleteCountry: LiveData<UiState<String>> get() = _deleteCountry

    private val _updateCountry = MutableLiveData<UiState<String>>()
    val updateCountry: LiveData<UiState<String>> get() = _updateCountry

    private val _country = MutableLiveData<UiState<List<OnlineCountry>>>()
    val country: LiveData<UiState<List<OnlineCountry>>> get() = _country

    fun getAllCountries() {
        _country.value = UiState.Loading
        repository.getAllCountries {
            _country.value = it
        }
    }

    fun addCountry(country: OnlineCountry){
        _addCountry.value = UiState.Loading
        repository.addCountry(country){
            _addCountry.value = it
        }
    }

    fun deleteCountry(country: OnlineCountry){
        _deleteCountry.value = UiState.Loading
        repository.deleteCountry(country){
            _deleteCountry.value = it
        }
    }

    fun updateCountry(country: OnlineCountry){
        _updateCountry.value = UiState.Loading
        repository.updateCountry(country){
            _updateCountry.value = it
        }
    }

}