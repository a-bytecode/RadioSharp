package com.example.radiosharp.model

import com.squareup.moshi.Json

data class RadioClass(

    @Json(name = "country")
    val country: String,

    @Json(name = "name")
    val name: String,

    @Json(name = "url")
    val url: String,

    @Json(name = "favicon")
    val favicon: String,

    @Json(name = "tags")
    val tags: String

    )