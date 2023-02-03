package com.example.radiosharp

import android.animation.ObjectAnimator
import android.app.Application
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.radiosharp.local.getDatabase
import com.example.radiosharp.model.FavClass
import com.example.radiosharp.remote.RadioApiService
import com.example.radiosharp.remote.Repository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class ApiStatus { START, LOADING, FOUND_RESULTS, FOUND_NO_RESULTS, ERROR }

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val database = getDatabase(application)

    private var api = RadioApiService.UserApi

    private val repository = Repository(api, database)

    val allRadios = repository.getAllDatabase

    val favRadios = MutableLiveData<MutableList<FavClass>>(mutableListOf())

    fun getFav() {
        viewModelScope.launch {
            favRadios.value = repository.getAllFav()
        }
    }

    fun getFavSearch(term : String) {
        viewModelScope.launch {
            favRadios.value = repository.getAllFavByName(term)
        }
    }

    private var _apiStatus = MutableLiveData<ApiStatus>()
    val apiStatus : LiveData<ApiStatus>
        get() = _apiStatus

    fun searchRadio(format: String, term: String,errortext: TextView) {
        viewModelScope.launch {
            try {
                _apiStatus.value = ApiStatus.LOADING
                Log.d("MVVM", "CHECK")
                repository.getConnection(format, term,this@MainViewModel)

            } catch (e: Exception) {
                Log.d("MainViewModel", "$e")
                errortext.text = "Error: $e"
                _apiStatus.value = ApiStatus.ERROR
                if (apiStatus.value!! == ApiStatus.ERROR) {
                    delay(6000)
                    resetApiStatus()
                }
            }
        }
    }

    fun buttonAnimator(button: Button) {
        // animatorTwo verändert ROTATION_X (X-Achse)
        // von RotateButton laufend von 0f bis 360f
        // innerhalb 2000ms
        val animatorTwo = ObjectAnimator
            .ofFloat(button, View.ROTATION_X, 0f, 360f)
        animatorTwo.duration = 500
        animatorTwo.start()
    }

    fun loadText(text: TextView, context: Context,errortext: TextView) {

        val searchyourRadiotext = text.text.toString()

        if (searchyourRadiotext != "") {
            searchRadio("json", searchyourRadiotext,errortext)
            Log.d("MainViewModel", "Test")
        } else {
            Toast
                .makeText(
                    context, "Bitte Suchbegriff eingeben",
                    Toast.LENGTH_SHORT
                )
                .show()
        }

    }

    fun fillText(text: TextView) {
        val theText = text.text.toString()
        if (theText == "") {
            text.text = "Not found"
        }
    }

    fun addFav(favorite: FavClass) {
        viewModelScope.launch {
            repository.addFavorites(favorite)
        }
    }

    fun removeFav(favorite: FavClass) {
        viewModelScope.launch {
            repository.removeFavorite(favorite)
        }
    }

    fun deleteAllFav() {
        viewModelScope.launch {
            Log.d("DeletedFav","DeleteAllFav")
            repository.dB.deleteAllFav()
        }
    }

    fun resetApiStatus(){
        setApiStatus(ApiStatus.START)
    }

    fun setApiStatus(status: ApiStatus){
        _apiStatus.value = status
    }

    fun deleteAll(){
        viewModelScope.launch {
            repository.dB.deleteAll()
            resetApiStatus()
        }
    }

}

