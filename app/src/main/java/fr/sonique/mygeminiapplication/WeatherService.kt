package fr.sonique.mygeminiapplication

import android.util.Log
import retrofit2.Response

class WeatherService {
    @Throws(Exception::class)
    suspend fun getWeather(weatherLocation: WeatherLocation): WeatherResponse? {
        val response: Response<WeatherResponse> =
            RetrofitInstance.api.getWeatherData(
                weatherLocation.latitude,
                weatherLocation.longitude)

        if (response.isSuccessful) {
            val weatherResponse: WeatherResponse? = response.body()
            Log.d("WeatherResponse", weatherResponse.toString())
            return weatherResponse
        } else {
            Log.e("WeatherResponse", "Error: ${response.message()}")
            throw Exception(response.message())
        }
    }
}