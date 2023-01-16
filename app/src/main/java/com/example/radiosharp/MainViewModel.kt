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
import androidx.lifecycle.viewModelScope
import com.example.radiosharp.local.getDatabase
import com.example.radiosharp.model.RadioClass
import com.example.radiosharp.remote.RadioApiService
import com.example.radiosharp.remote.Repository
import kotlinx.coroutines.launch



class MainViewModel(application: Application) : AndroidViewModel(application) {

    val database = getDatabase(application)

    private var api = RadioApiService.UserApi

    private val repository = Repository(api, database)

    val allRadios = repository.allRadios

    val favoritenListe = repository.favoritesList

    val radioDatabase = repository.radioDatabase


    fun searchRadio(format: String, term: String) {
        viewModelScope.launch {
            try {
                Log.d("MVVM","CHECK")
                repository.getConnection(format, term)
            } catch (e: Exception) {
                Log.d("MainViewModel", "$e")
            }
        }
    }

    fun buttonAnimator(button: Button) {
        // animatorTwo verändert ROTATION_X (X-Achse) von RotateButton laufend von 0f bis 360f
        // innerhalb 2000ms
        val animatorTwo = ObjectAnimator.ofFloat(button, View.ROTATION_X, 0f, 360f)
        animatorTwo.duration = 500
        animatorTwo.start()
    }

    fun loadText(text: TextView, context: Context) {

        val searchyourRadiotext = text.text.toString()

        if (searchyourRadiotext != "") {
            searchRadio("json", searchyourRadiotext)
            Log.d("MainViewModel", "Test")
        } else {
            Toast.makeText(context, "Bitte Suchbegriff eingeben", Toast.LENGTH_SHORT)
                .show()
        }

    }

    fun fillText(text: TextView) {
        val theText = text.text.toString()
        if (theText == "") {
            text.text = "Not found"
        }
    }

    fun addFav(radioStation: RadioClass) {
        viewModelScope.launch {
            repository.addFavorites(radioStation)
        }
    }

    fun removeFav(radioStation: RadioClass) {
        viewModelScope.launch {
            repository.removeFavorite(radioStation)
        }
    }

    fun deteleData(radioStation: LiveData<List<RadioClass>>){
        viewModelScope.launch {
            repository.dB.deleteAll()
        }
    }

}

