package com.example.radiosharp.remote

import com.example.radiosharp.ApiStatus
import com.example.radiosharp.local.RadioDatabase
import com.example.radiosharp.model.FavClass
import com.example.radiosharp.model.RadioClass
import com.example.radiosharp.MainViewModel
import kotlinx.coroutines.delay


class Repository(private val api: RadioApiService.UserApi, private val database: RadioDatabase) {


    val dB = database.radioDatabaseDao
    val getAllDatabase = dB.getAll()
//    val getAllFavByName = { name:String -> dB.getAllFavByName(name) }

    suspend fun searchFavByName(name : String):MutableList<FavClass>{
        return dB.getAllFavByName(name)
    }

    suspend fun getAllFav():MutableList<FavClass>{
        return dB.getAllFav()
    }

    suspend fun getConnection(format: String, term: String, viewModel: MainViewModel) {
        val response = api.retrofitService.getServerResponse(format, term)
        dB.deleteAll()
        setPrevAndNext(response)
        val results = response as MutableList<RadioClass>
        dB.insert(results)


        if (results.size > 0) {
            viewModel.setApiStatus(ApiStatus.FOUND_RESULTS)
        } else {
            viewModel.setApiStatus(ApiStatus.FOUND_NO_RESULTS)
            if (viewModel.apiStatus.value!! == ApiStatus.FOUND_NO_RESULTS) {
                delay(6000)
                viewModel.resetApiStatus()
            }
        }
    }

    // Die Funktion add und remove, bereitgestellt und sie ungleich "null" gesetzt
    // damit bei der Aktivierung der funktion die App nicht abstürzt
    suspend fun addFavorites(favorite: FavClass) {

        val radioStation = getAllDatabase.value!!.find {
            it.stationuuid == favorite.stationuuid
        }
        if (radioStation != null) {
            dB.insertFav(favorite)
        }
    }

    suspend fun removeFavorite(favorite: FavClass) {
        val radioStation = getAllDatabase.value!!.find {
            it.stationuuid == favorite.stationuuid
        }
        if (radioStation != null) {
            dB.deleteFav(favorite)
        }
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

    fun setFavPrevAndNext(radioStations: List<FavClass>) {

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

