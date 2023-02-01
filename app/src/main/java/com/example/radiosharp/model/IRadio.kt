package com.example.radiosharp.model

// IRadio ist das Interface das benötigt wird um "FavClass" und "RadioClass" miteinander zu verbinden.
// Man Erbt quasi von diesem Interface.
interface IRadio {
    val stationuuid: String
    val country: String
    val name: String
    val radioUrl: String
    val favicon: String
    val tags: String
    var nextStation : String
    var previousStation : String
}