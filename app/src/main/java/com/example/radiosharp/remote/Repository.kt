package com.example.radiosharp.remote

import android.service.autofill.UserData
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.radiosharp.model.RadioClass

class Repository(private val api: RadioApiService.UserApi) {


    private var _test = MutableLiveData<List<RadioClass>>()
    val test: MutableLiveData<List<RadioClass>>
    get() = _test


   suspend fun getConnection(format:String,term:String){

       test.value = api.retrofitService.getServerResponse(format,term)
   }
}

