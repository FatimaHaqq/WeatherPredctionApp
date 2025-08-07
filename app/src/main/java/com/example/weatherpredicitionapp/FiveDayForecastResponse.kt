package com.example.weatherpredicitionapp

data class FiveDayForecastResponse(
    val list: List<ForecastItem>
)

data class ForecastItem(
    val dt_txt: String, // "2025-06-25 12:00:00"
    val main: Main,
    val weather: List<Weather>
)

data class Forecast5DayResponse(
    val list: List<WeatherItem>
)

data class DailyForecast(
    val dt: Long,
    val temp: Temp,
    val weather: List<Weather>
)

data class Temp(
    val day: Double
)






