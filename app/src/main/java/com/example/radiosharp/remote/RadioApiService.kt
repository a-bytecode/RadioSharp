package com.example.radiosharp.remote

import com.example.radiosharp.BuildConfig
import com.example.radiosharp.model.RadioClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


const val BASE_URL = "https://at1.api.radio-browser.info/"

val API_TOKEN = BuildConfig.API_TOKEN

private val client:OkHttpClient = OkHttpClient.Builder()
    .addInterceptor { chain ->
        val newRequest: Request = chain.request().newBuilder()
            .addHeader("X-RapidAPI-Key", API_TOKEN)
            .addHeader("X-RapidAPI-Host", "radio-browser.p.rapidapi.com")
            .build()
        chain.proceed(newRequest) }.build()

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory())
        .build()

private val retrofit = Retrofit.Builder()
    .client(client)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface RadioApiService{

    @GET("{format}/stations/search")

    suspend fun getServerResponse(@Path("format") format:String,@Query("name") term:String):List<RadioClass>

    object UserApi {
        val retrofitService: RadioApiService by lazy { retrofit.create(RadioApiService::class.java) }
    }

}