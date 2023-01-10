package com.example.radiosharp.remote

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Index
import com.example.radiosharp.local.RadioDatabase
import com.example.radiosharp.local.getDatabase
import com.example.radiosharp.model.RadioClass

class Repository(private val api: RadioApiService.UserApi,private val database: RadioDatabase) {


    private var _loadRadio = MutableLiveData<List<RadioClass>>()
    val loadRadio : MutableLiveData<List<RadioClass>>
    get() = _loadRadio

    //TODO Eine leere Favoriten-Liste erstellt damit der User
    // zukünftige Favoriten in dieser Liste abspeichern kann.
    private var _favoritesList = MutableLiveData<MutableList<RadioClass>>(mutableListOf())
    val favoritesList : LiveData<MutableList<RadioClass>>
    get() = _favoritesList

    val radioStations = database.radioDatabaseDao.getAll()


   suspend fun getConnection(format:String,term:String){

       loadRadio.value = api.retrofitService.getServerResponse(format,term)
       val response = api.retrofitService.getServerResponse(format,term)
       database.radioDatabaseDao.insert(response)

   }

    //TODO die Funktion add und remove, bereitgestellt und sie ungleich "null" gesetzt
    // damit bei der Aktivierung der funktion die App nicht abstürzt
    fun addFavorites(radioStation:RadioClass){
        if (favoritesList.value != null){
            val favList : MutableList<RadioClass> = favoritesList.value!!
            favList.add(radioStation)
            _favoritesList.value = favList
            Log.d("removeFavorite","${radioStation.name}")
        }
    }

    fun removeFavorite(radioStation: RadioClass){
        if (favoritesList.value != null) {
            val removeUnitfromList : MutableList<RadioClass> = favoritesList.value!!
            removeUnitfromList.remove(radioStation)
            _favoritesList.value = removeUnitfromList
        }
//        _favoritesList.value?.remove(radioStation)
//        _favoritesList.value = _favoritesList.value
//        Log.d("removeFavorite","${radioStation.name}")
    }

    }

