package com.example.radiosharp.remote

import com.example.radiosharp.model.RadioClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path


const val BASE_URL = "https://radio-browser.p.rapidapi.com/"

val API_TOKEN = "d841fadc6emshcff481dd4ef3e2cp148a97jsnd6be3c3c51b1"


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

    @GET("{format}/stations/byname/{searchterm}")

    suspend fun getServerResponse(@Path("format") format:String,@Path("searchterm") term:String):List<RadioClass>

    object UserApi {
        val retrofitService: RadioApiService by lazy { retrofit.create(RadioApiService::class.java) }
    }

}