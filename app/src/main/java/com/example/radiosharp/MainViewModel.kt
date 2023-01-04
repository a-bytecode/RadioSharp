package com.example.radiosharp

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.radiosharp.remote.RadioApiService
import com.example.radiosharp.remote.Repository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {




    private var api = RadioApiService.UserApi

    val repository = Repository(api)
    val test = repository.test


    fun getConnect(format:String,term:String) {

            viewModelScope.launch {
                try {
                repository.getConnection(format,term)

            } catch (e:Exception){
                    Log.d("MainViewModel","$e")
        }

        }

    }
}