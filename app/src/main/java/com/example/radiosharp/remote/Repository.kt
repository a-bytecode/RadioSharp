package com.example.radiosharp.remote

import android.opengl.Visibility
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.room.Index
import com.example.radiosharp.ApiStatus
import com.example.radiosharp.local.RadioDatabase
import com.example.radiosharp.local.getDatabase
import com.example.radiosharp.model.RadioClass

class Repository(private val api: RadioApiService.UserApi,private val database: RadioDatabase) {


    private var _loadRadio = MutableLiveData<List<RadioClass>>()
    val loadRadio : LiveData<List<RadioClass>>
    get() = _loadRadio

    // Eine leere Favoriten-Liste erstellt damit der User
    // zukünftige Favoriten in dieser Liste abspeichern kann.
    private var _favoritesList = MutableLiveData<MutableList<RadioClass>>(mutableListOf())
    val favoritesList : LiveData<MutableList<RadioClass>>
    get() = _favoritesList

    val dB = database.radioDatabaseDao
    val radioDatabase = database.radioDatabaseDao.getFav()

    suspend fun getConnection(format:String,term:String){
       val response = api.retrofitService.getServerResponse(format,term)
       _loadRadio.value = response
       database.radioDatabaseDao.insert(response)
   }
    // Die Funktion add und remove, bereitgestellt und sie ungleich "null" gesetzt
    // damit bei der Aktivierung der funktion die App nicht abstürzt
    suspend fun addFavorites(radioStation:RadioClass){
        if (favoritesList.value != null){
            radioStation.favorite = true
            dB.update(radioStation)
            val favList : MutableList<RadioClass> = favoritesList.value!!
            favList.add(radioStation)
            _favoritesList.value = favList
            Log.d("removeFavorite","${radioStation.name}")
        }
    }

    suspend fun removeFavorite(radioStation: RadioClass){
        if (favoritesList.value != null) {
            radioStation.favorite = false
            dB.update(radioStation)
            val removeUnitfromList : MutableList<RadioClass> = favoritesList.value!!
            removeUnitfromList.remove(radioStation)
            _favoritesList.value = removeUnitfromList
        }
    }

    // mit diesen Zeilen geben wir der "RadioClass" Bescheid, dass sie erkennen soll
    // das wir VOR der aktuellen "position" und NACH der aktuellen "position" eine weitere "position" haben.
    // Und wir ermöglichen es somit, die Ausführung der Previous und Next Wiedergabe.
    fun setPrevAndNext() {
        // Temporäre Liste erstellt.
            val tempList = loadRadio.value
        // Wird nur ausgeführt wenn TempLuste nicht "null" und nicht "leer" ist.
            if (!tempList.isNullOrEmpty()){

                for (position in tempList.indices) {
                    val currentStation = tempList[position]

                    if (position < tempList.size -1){
                        // Damit die Liste nicht "out of Bounds" geht geben wir der "position" +1, somit kann mann vor schalten ohne Absturz.
                        val nextStation = tempList[position +1]
                        currentStation.nextStation = nextStation.stationuuid
                    }
                    // Das selbe für zurück schalten.
                    if (position > 0) {
                        val previousRadio = tempList[position -1]
                        currentStation.previousStation = previousRadio.stationuuid
                    }
            }
                // hiermit Triggern wir die Live data.
              _loadRadio.value = tempList.toMutableList()
        }
    }

    }

