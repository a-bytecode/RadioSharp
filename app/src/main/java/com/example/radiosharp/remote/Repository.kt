package com.example.radiosharp.remote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.radiosharp.local.RadioDatabase
import com.example.radiosharp.model.FavClass
import com.example.radiosharp.model.RadioClass


class Repository(private val api: RadioApiService.UserApi, private val database: RadioDatabase) {

    val allRadios = database.radioDatabaseDao.getAll()

    // Eine leere Favoriten-Liste erstellt damit der User
    // zukünftige Favoriten in dieser Liste abspeichern kann.
    private var _favoritesList = MutableLiveData<MutableList<RadioClass>>(mutableListOf())
    val favoritesList: LiveData<MutableList<RadioClass>>
        get() = _favoritesList

    val dB = database.radioDatabaseDao
    val getFavDatabase = dB.getFav() //FAV List

    suspend fun getConnection(format: String, term: String) {
        val response = api.retrofitService.getServerResponse(format, term)
        dB.deleteAll()
        setPrevAndNext(response)
        dB.insert(response as MutableList<RadioClass>)
    }

    // Die Funktion add und remove, bereitgestellt und sie ungleich "null" gesetzt
    // damit bei der Aktivierung der funktion die App nicht abstürzt
    suspend fun addFavorites(favorite: FavClass) {

        val radioStation = allRadios.value!!.find {
            it.stationuuid == favorite.stationuuid
        }
        if (radioStation != null) {
            _favoritesList.value?.add(radioStation)
            dB.insertFav(favorite)
        }
    }

    suspend fun removeFavorite(favorite: FavClass) {
        val radioStation = allRadios.value!!.find {
            it.stationuuid == favorite.stationuuid
        }
        if (radioStation != null) {
            _favoritesList.value?.remove(radioStation)
            dB.deleteFav(favorite)
        }
    }

    // TODO Favoritenliste neu laden (Aktualisieren)
    fun loadFav(){
        favoritesList.value
    }

    // mit diesen Zeilen geben wir der "RadioClass" Bescheid, dass sie erkennen soll
    // das wir VOR der aktuellen "position" und NACH der aktuellen "position" eine weitere "position" haben.
    // Und wir ermöglichen es somit, die Ausführung der Previous und Next Wiedergabe.
    fun setPrevAndNext(radioStations: List<RadioClass>) {

        if (!radioStations.isNullOrEmpty()) {

            for (position in radioStations.indices) {
                val currentStation = radioStations[position]

                if (position < radioStations.size - 1) {
                    // Damit die Liste nicht "out of Bounds" geht
                    // geben wir der "position" +1 und der "radioStation" -1
                    // somit kann mann vor schalten ohne Absturz.
                    val nextStation = radioStations[position + 1]
                    currentStation.nextStation = nextStation.stationuuid
                }
                // Das selbe für zurück schalten.
                if (position > 0) {
                    val previousRadio = radioStations[position - 1]
                    currentStation.previousStation = previousRadio.stationuuid
                }
            }
        }
    }

}

