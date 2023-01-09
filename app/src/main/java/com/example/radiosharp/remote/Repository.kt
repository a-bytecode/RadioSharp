package com.example.radiosharp.remote

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.radiosharp.model.RadioClass

class Repository(private val api: RadioApiService.UserApi) {


    private var _loadRadio = MutableLiveData<List<RadioClass>>()
    val loadRadio : MutableLiveData<List<RadioClass>>
    get() = _loadRadio

    private var _favoritesList = MutableLiveData<MutableList<RadioClass>>(mutableListOf())
    val favoritesList : LiveData<MutableList<RadioClass>>
    get() = _favoritesList


   suspend fun getConnection(format:String,term:String){

       loadRadio.value = api.retrofitService.getServerResponse(format,term)
   }


    fun addFavorites(radioStation:RadioClass){
        if (favoritesList.value != null){
            val favList : MutableList<RadioClass> = favoritesList.value!!
            favList.add(radioStation)
            _favoritesList.value = favList
            Log.d("removeFavorite","${radioStation.name}")
        }

    }

    fun removeFavorite(radioStation: RadioClass){
        //TODO MutableLiveData triggern
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

