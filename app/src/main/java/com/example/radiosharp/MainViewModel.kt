package com.example.radiosharp

import android.animation.ObjectAnimator
import android.app.Application
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.radiosharp.model.RadioClass
import com.example.radiosharp.remote.RadioApiService
import com.example.radiosharp.remote.Repository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {


    private var api = RadioApiService.UserApi

    val repository = Repository(api)

    val loadTheRadio = repository.loadRadio


//    private var _radioList = MutableLiveData<List<RadioClass>>()
//    val radioList : LiveData<List<RadioClass>>
//    get() = _radioList

//    val test = repository.test
//    fun getConnect(format:String,term:String) {
//
//            viewModelScope.launch {
//                try {
//                repository.getConnection(format,term)
//
//            } catch (e:Exception){
//                    Log.d("MainViewModel","$e")
//        }
//
//        }

        fun searchRadio(format: String, term: String) {

               viewModelScope.launch {
                   try {
                   repository.getConnection(format,term)
               } catch (e:Exception) {
                       Log.d("MainViewModel","$e")
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

}
