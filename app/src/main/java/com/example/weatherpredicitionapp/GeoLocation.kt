package com.example.weatherpredicitionapp

data class GeoLocation(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String,
    val state: String?
)
