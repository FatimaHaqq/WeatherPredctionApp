package com.example.weatherpredicitionapp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface GeoAPI {
    @GET("direct")
    suspend fun getCoordinatesByLocation(
        @Query("q") location: String,
        @Query("limit") limit: Int = 1,
        @Query("appid") apiKey: String
    ): List<GeoLocation>

    companion object {
        private const val GEO_BASE_URL = "https://api.openweathermap.org/geo/1.0/"

        fun create(): GeoAPI {
            return Retrofit.Builder()
                .baseUrl(GEO_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GeoAPI::class.java)
        }
    }
}
