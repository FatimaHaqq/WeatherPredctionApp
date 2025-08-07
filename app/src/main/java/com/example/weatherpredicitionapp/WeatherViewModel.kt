package com.example.weatherpredicitionapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val _forecastData = MutableStateFlow<List<DailyForecast>>(emptyList())
    val forecastData: StateFlow<List<DailyForecast>> = _forecastData

    private val _weatherData = MutableStateFlow<WeatherResponse?>(null)
    val weatherData: StateFlow<WeatherResponse?> = _weatherData

    private val weatherApi = WeatherAPI.create()
    private val geoApi = GeoAPI.create()


    fun fetchWeather(city: String, apiKey: String) {
        viewModelScope.launch {
            try {
                // 🔍 Added this log
                println("🌍 Fetching weather for city: $city")

                // Step 1: Get coordinates using Geocoding API
                 val locations = geoApi.getCoordinatesByLocation(city, apiKey = apiKey)

                if (locations.isNotEmpty()) {
                    val lat = locations[0].lat
                    val lon = locations[0].lon

                    //  Step 2: Get weather by coordinates
                    val response = weatherApi.getCurrentWeatherByCoordinates(lat, lon, apiKey)
                    _weatherData.value = response

                    println("✅ Current weather: ${response.main.temp}°C, ${response.weather[0].description}")

                    // Step 3: Fetch forecast using coordinates
                    fetchFiveDayForecastByCoordinates(lat, lon, apiKey)

                } else {
                    println("⚠️ No location found for $city")
                }


                // 🔍 Added this log
                // println("✅ Current weather: ${response.main.temp}°C, ${response.weather[0].description}")

                // 🔍 Now fetch forecast (moved outside to show more clearly)
                //fetchFiveDayForecastByCoordinates(lat,lon, apiKey)


            } catch (e: Exception) {
                // 🔍 More helpful log
                println("🔥 Error fetching weather: ${e.message}")
            }
        }
    }

    fun fetchCurrentWeatherByCoordinates(lat: Double, lon: Double, apiKey: String) {
        viewModelScope.launch {
            try {
                println("🌦️ Fetching current weather for coordinates: ($lat, $lon)")

                val response = weatherApi.getCurrentWeatherByCoordinates(lat, lon, apiKey)
                _weatherData.value = response

                println("✅ Current weather: ${response.main.temp}°C, ${response.weather[0].description}")

            } catch (e: Exception) {
                println("🔥 Error fetching current weather by coordinates: ${e.message}")
            }
        }
    }



    fun fetchFiveDayForecastByCoordinates(lat: Double, lon: Double, apiKey: String) {
        viewModelScope.launch {
            try {
                println("📅 Fetching 5-day forecast for coordinates: ($lat, $lon)")

                val forecastResponse = weatherApi.getFiveDayForecastByCoordinates(lat, lon, apiKey)

                val dailyForecasts = forecastResponse.list.filter {
                    it.dt_txt.contains("12:00:00")
                }

                val simplifiedForecast = dailyForecasts.map {
                    DailyForecast(
                        dt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            .parse(it.dt_txt)?.time?.div(1000) ?: 0L,
                        temp = Temp(it.main.temp),
                        weather = it.weather
                    )
                }

                _forecastData.value = simplifiedForecast

            } catch (e: Exception) {
                println("🔥 Error fetching forecast by coordinates: ${e.message}")
            }
        }
    }
}




