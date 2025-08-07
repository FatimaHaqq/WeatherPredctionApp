from fastapi import FastAPI, Query
from fastapi.middleware.cors import CORSMiddleware
from predict import predict_tflite

app = FastAPI()

# Allow Android app to access backend
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Change to only your Android IP or domain for production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/")
def root():
    return {"message": "Backend is running"}
@app.get("/predict")
def predict(city: str = Query(...)):
    try:
        predictions = predict_tflite(city)
        return {"city": city, "predicted_temperatures_celsius": predictions}
    except Exception as e:
        return {"error": str(e)}
