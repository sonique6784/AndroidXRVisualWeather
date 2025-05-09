package fr.sonique.mygeminiapplication

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response

interface WeatherApiService {
    @GET("v1/forecast")
    suspend fun getWeatherData( //daily=weather_code,temperature_2m_min,temperature_2m_max&current=weather_code,temperature_2m
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "weather_code,temperature_2m",
        @Query("daily") daily: String = "weather_code,temperature_2m_min,temperature_2m_max"
    ): Response<WeatherResponse>
}