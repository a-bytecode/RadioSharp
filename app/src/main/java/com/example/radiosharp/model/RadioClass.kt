package com.example.radiosharp.model

import android.net.Uri
import com.squareup.moshi.Json

data class RadioClass(

    @Json(name = "stationuuid")
    val stationuuid: String,

    @Json(name = "country")
    val country: String,

    @Json(name = "name")
    val name: String,

    @Json(name = "url")
    val playRadio: String,

    @Json(name = "favicon")
    val favicon: String,

    @Json(name = "tags")
    val tags: String

    )