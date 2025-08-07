package com.example.weatherpredicitionapp

data class WeatherResponse(
    val name: String,
    val coord: Coord,
    val main: Main,
    val weather: List<Weather>
)
data class WeatherItem(
    val dt_txt: String,              // "2025-06-20 12:00:00"
    val main: Main,
    val weather: List<Weather>
)

data class Main(
    val temp: Double,
    val humidity: Int
)

data class Weather(
    val description: String,
    val icon: String
)
data class Coord(val lat: Double, val lon: Double)