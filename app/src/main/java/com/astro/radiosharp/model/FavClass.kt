package com.astro.radiosharp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity
data class FavClass(

    @PrimaryKey
    @Json(name = "stationuuid")
    override val stationuuid: String,

    @Json(name = "country")
    override val country: String,

    @Json(name = "name")
    override val name: String,

    @Json(name = "url")
    override val radioUrl: String,

    @Json(name = "favicon")
    override val favicon: String,

    @Json(name = "tags")
    override val tags: String,

    override var nextStation : String = "",

    override var previousStation : String = "",


    ): IRadio // FavClass implementiert das Interface von "IRadio"