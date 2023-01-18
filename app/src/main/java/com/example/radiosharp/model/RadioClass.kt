package com.example.radiosharp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity
data class RadioClass(

    @PrimaryKey
    @Json(name = "stationuuid")
    val stationuuid: String,

    @Json(name = "country")
    val country: String,

    @Json(name = "name")
    val name: String,

    @Json(name = "url")
    val radioUrl: String,

    @Json(name = "favicon")
    val favicon: String,

    @Json(name = "tags")
    val tags: String,

    var nextStation : String = "",

    var previousStation : String = ""

    )