package com.example.radiosharp.remote

import android.service.autofill.UserData
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.radiosharp.model.RadioClass

class Repository(private val api: RadioApiService.UserApi) {


    private var _loadRadio = MutableLiveData<List<RadioClass>>()
    val loadRadio : MutableLiveData<List<RadioClass>>
    get() = _loadRadio


   suspend fun getConnection(format:String,term:String){

       loadRadio.value = api.retrofitService.getServerResponse(format,term)
   }
}

