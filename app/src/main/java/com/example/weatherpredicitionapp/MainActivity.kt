package com.example.weatherpredicitionapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherpredicitionapp.ui.theme.BlueJC
import com.example.weatherpredicitionapp.ui.theme.DarkBlueJC
import com.example.weatherpredicitionapp.ui.theme.WeatherPredicitionAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherPredicitionAppTheme {
                WeatherScreen()
            }
        }
    }
}

@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel()) {
    val weatherData by viewModel.weatherData.collectAsState()
    val forecast by viewModel.forecastData.collectAsState()
    var city by remember { mutableStateOf("") }
    val apiKey = "8c2370e241986f0db70baad9213534f1"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(id = R.drawable.background), // ✅ Make sure this image exists!
                contentScale = ContentScale.FillBounds
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(70.dp))

                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(30.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        unfocusedIndicatorColor = BlueJC,
                        focusedIndicatorColor = BlueJC,
                        focusedLabelColor = DarkBlueJC
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.fetchWeather(city, apiKey)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueJC)
                ) {
                    Text(text = "Check Weather")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
            weatherData?.let {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        WeatherCard("City", it.name, Icons.Default.Place)
                        WeatherCard("Temperature", "${it.main.temp}°C", Icons.Default.Info)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        WeatherCard("Humidity", "${it.main.humidity}%", Icons.Default.Warning)
                        WeatherCard("Description", it.weather[0].description, Icons.Default.Info)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "5-Day Forecast",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = DarkBlueJC,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(forecast) { day ->
                    ForecastItem(day)
                }
            }
        }
    }
}
@Composable
fun WeatherCard(label: String, value: String, icon: ImageVector) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .size(150.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DarkBlueJC,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = label, fontSize = 14.sp, color = DarkBlueJC)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = BlueJC
                )
            }
        }
    }
}

@Composable
fun ForecastItem(day: DailyForecast) {
    val date = java.text.SimpleDateFormat("EEE, dd MMM")
        .format(java.util.Date(day.dt * 1000))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text  = date, color = DarkBlueJC)
            Text(text = "${day.temp.day}°C", color = BlueJC, fontWeight = FontWeight.Bold)
            Text(text = day.weather.firstOrNull()?.description ?: "Unknown", color = BlueJC)
        }
    }
}
