package com.example.radiosharp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity
data class FavClass(

    @PrimaryKey
    @Json(name = "stationuuid")
    val stationuuid: String
    )