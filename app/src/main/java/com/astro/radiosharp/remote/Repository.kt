package com.astro.radiosharp.remote

import com.astro.radiosharp.ApiStatus
import com.astro.radiosharp.local.RadioDatabase
import com.astro.radiosharp.model.FavClass
import com.astro.radiosharp.model.RadioClass
import com.astro.radiosharp.MainViewModel
import kotlinx.coroutines.delay


class Repository(private val api: RadioApiService.UserApi, private val database: RadioDatabase) {

//================================================================================================//
// -------------- Repository: "ApiCall, ApiStatus & Favoriten Funktionen" --------------
//================================================================================================//

    //Hier erstellen wir Variablen damit wir aus der Repository aus, auf die
    // "Query" von der "RoomDatabase" zugreifen können.
    val dB = database.radioDatabaseDao
    val getAllDatabase = dB.getAll()

    //hier holen wir die Query für die "search" funktion in den Favoriten,
    // um sie dann in der Mutable Liste "FavClass" anwenden zu können.
    suspend fun searchFavByName(name : String):MutableList<FavClass>{
        return dB.getAllFavByName(name)
    }

    //Mit "getAllFav" initialisieren o. aktualisieren wir die Liste
    // und holen alles was aus den Favoriten ist heraus.
    suspend fun getAllFav():MutableList<FavClass>{
        return dB.getAllFav()
    }

    //Mit dieser Funktion starten wir den Api Call
    // und setzten unseren ApiStatus aus dem "ViewModel" aus.
    suspend fun getConnection(format: String, term: String, viewModel: MainViewModel) {
        val response = RadioApiService.UserApi.retrofitService.getServerResponse(format, term)
        dB.deleteAll()
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

    // In der Add und remove Favorites Funktion,
    // suchen wir die id der Station heraus
    // und geben diese im Parameter "favorites" weiter.
    // um Favoriten löschen oder hinzufügen zu können.
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

}


//===============================================================================================================
// ----- eine weitere Methode zur Lösung für die Funktion "skip & privious" aus der Repository aus -----
//===============================================================================================================

// mit diesen Zeilen geben wir der "RadioClass" Bescheid, dass sie erkennen soll
// das wir VOR der aktuellen "position" und NACH der aktuellen "position" eine weitere "position" haben.
// Und wir ermöglichen es somit, die Ausführung der Previous und Next Wiedergabe.

//fun setPrevAndNext(radioStations: List<RadioClass>) {
//
//    if (!radioStations.isNullOrEmpty()) {
//
//        for (position in radioStations.indices) {
//            val currentStation = radioStations[position]
//
//            if (position < radioStations.size - 1) {
//                // Damit die Liste nicht "out of Bounds" geht
//                // geben wir der "position" +1 und der "radioStation" -1
//                // somit kann mann vor schalten ohne Absturz.
//                val nextStation = radioStations[position + 1]
//                currentStation.nextStation = nextStation.stationuuid
//            }
//            // Das selbe für zurück schalten.
//            if (position > 0) {
//                val previousRadio = radioStations[position - 1]
//                currentStation.previousStation = previousRadio.stationuuid
//            }
//        }
//    }
//}
//
//fun setFavPrevAndNext(radioStations: List<FavClass>) {
//
//    if (!radioStations.isNullOrEmpty()) {
//
//        for (position in radioStations.indices) {
//            val currentStation = radioStations[position]
//
//            if (position < radioStations.size - 1) {
//                // Damit die Liste nicht "out of Bounds" geht
//                // geben wir der "position" +1 und der "radioStation" -1
//                // somit kann mann vor schalten ohne Absturz.
//                val nextStation = radioStations[position + 1]
//                currentStation.nextStation = nextStation.stationuuid
//            }
//            // Das selbe für zurück schalten.
//            if (position > 0) {
//                val previousRadio = radioStations[position - 1]
//                currentStation.previousStation = previousRadio.stationuuid
//            }
//        }
//    }
//}

