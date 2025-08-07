import os
import numpy as np
import pandas as pd
import requests
import tensorflow as tf
from sklearn.preprocessing import MinMaxScaler
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import LSTM, Dense

# 🌍 Get coordinates for a city
def get_coordinates(city_name):
    geo_url = f"https://geocoding-api.open-meteo.com/v1/search?name={city_name}&count=1"
    response = requests.get(geo_url)
    data = response.json()
    if "results" not in data or len(data["results"]) == 0:
        raise ValueError(f"City '{city_name}' not found.")
    return data["results"][0]["latitude"], data["results"][0]["longitude"]

# ⛅ Fetch historical weather data from Open-Meteo
def fetch_weather_data(city):
    lat, lon = get_coordinates(city)
    url = (
        "https://archive-api.open-meteo.com/v1/archive?"
        f"latitude={lat}&longitude={lon}"
        f"&start_date=2025-04-01&end_date=2025-06-22"
        f"&hourly=temperature_2m,pressure_msl,relativehumidity_2m,windspeed_10m"
        f"&timezone=auto"
    )
    response = requests.get(url)
    if response.status_code != 200:
        raise Exception(f"API Error: {response.status_code}")
    data = response.json()
    df = pd.DataFrame({
        'time': pd.to_datetime(data['hourly']['time']),
        'temperature': data['hourly']['temperature_2m'],
        'pressure': data['hourly']['pressure_msl'],
        'humidity': data['hourly']['relativehumidity_2m'],
        'windspeed': data['hourly']['windspeed_10m']
    })
    daily_data = df.resample('D', on='time').mean().reset_index()
    daily_data.dropna(inplace=True)
    return daily_data

# 🧠 Create LSTM sequences
def create_sequences(data, input_days=7, output_days=5):
    X, y = [], []
    for i in range(len(data) - input_days - output_days + 1):
        X.append(data[i:i+input_days, :-1])  # all features except target
        y.append(data[i+input_days:i+input_days+output_days, -1])  # temperature only
    return np.array(X), np.array(y)

# 🧠 Train LSTM model
def train_model(X, y):
    model = Sequential([
        LSTM(64, input_shape=(7, 3)),
        Dense(32, activation='relu'),
        Dense(5)
    ])
    model.compile(optimizer='adam', loss='mse')
    model.fit(X, y, epochs=30, batch_size=16, validation_split=0.2)
    return model

# 🔁 Convert Keras model to TFLite
def convert_to_tflite(model, filename):
    os.makedirs(os.path.dirname(filename), exist_ok=True)
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    converter.target_spec.supported_ops = [
        tf.lite.OpsSet.TFLITE_BUILTINS,
        tf.lite.OpsSet.SELECT_TF_OPS
    ]
    tflite_model = converter.convert()
    with open(filename, 'wb') as f:
        f.write(tflite_model)
    print(f"✅ TFLite model saved as: {filename}")

# 🚀 Full pipeline for training and saving model for any city
def train_and_save_model(city):
    print(f"🔧 Training model for city: {city}")
    df = fetch_weather_data(city)
    features = ['humidity', 'pressure', 'windspeed', 'temperature']
    scaler = MinMaxScaler()
    scaled = scaler.fit_transform(df[features])
    X, y = create_sequences(scaled)
    model = train_model(X, y)
    filename = f"model/{city.lower()}_weather_lstm_model.tflite"
    convert_to_tflite(model, filename)
    return filename
def preprocess_input(city):
    df = fetch_weather_data(city)
    features = ['humidity', 'pressure', 'windspeed', 'temperature']
    scaler = MinMaxScaler()
    scaled_data = scaler.fit_transform(df[features])
    input_seq = scaled_data[-7:, :-1].reshape(1, 7, 3)
    return input_seq, scaler

def predict_tflite(city, model_path="model/weather_lstm_model.tflite"):
    input_seq, scaler = preprocess_input(city)
    interpreter = tf.lite.Interpreter(model_path=model_path)
    interpreter.allocate_tensors()
    input_index = interpreter.get_input_details()[0]['index']
    output_index = interpreter.get_output_details()[0]['index']
    interpreter.set_tensor(input_index, input_seq.astype(np.float32))
    interpreter.invoke()
    output = interpreter.get_tensor(output_index)[0]

    dummy = np.zeros((5, 4))
    dummy[:, -1] = output.flatten()
    predicted_temps = scaler.inverse_transform(dummy)[:, -1]
    return predicted_temps.tolist()