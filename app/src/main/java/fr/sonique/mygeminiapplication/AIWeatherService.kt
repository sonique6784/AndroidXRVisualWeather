package fr.sonique.mygeminiapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.vertexai.type.PublicPreviewAPI
import com.google.firebase.vertexai.type.Schema
import com.google.firebase.vertexai.type.generationConfig
import com.google.firebase.vertexai.vertexAI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class WeatherLocation(val city: String, val latitude: Double, val longitude: Double)

class AIWeatherService {

    private val jsonSchema = Schema.obj(
        mapOf(
            "city" to Schema.string(),
            "latitude" to Schema.double(),
            "longitude" to Schema.double()
        )
    )

    private val jsonModel = Firebase.vertexAI.generativeModel(
        modelName = "gemini-2.0-flash-001",
        // generate JSON data
        generationConfig = generationConfig {
            responseMimeType = "application/json"
            responseSchema = jsonSchema
        }
    )

    @OptIn(PublicPreviewAPI::class)
    private val generativeImageModel = Firebase.vertexAI.imagenModel(
        modelName = "imagen-3.0-fast-generate-001"
        //modelName = "imagen-3.0-generate-002"
    )

    @OptIn(PublicPreviewAPI::class)
    suspend fun sendBitmapPrompt(cityName: String, weatherLike: String): Bitmap? {
        val prompt = """Imagine an image of $cityName with a $weatherLike weather, in the style of 8bit pixel art."""

        val response = generativeImageModel.generateImages(prompt)
        if (response.images.isNotEmpty()) {
            // Convert ByteArray to Bitmap
            val bitmap = BitmapFactory.decodeByteArray(response.images.first().data, 0, response.images.first().data.size)

            return bitmap
        }
        return null
    }


    @Throws(Exception::class)
    suspend fun sendJSONPrompt(voiceTranscription: String): WeatherLocation? {
        Log.d("voiceTranscription", voiceTranscription)

        val prompt = """You are a very talended reader that can find keywords in a sentence. You know geographie and the main cities in the world.
            From the text provided, extract the name of the city and only the city name and get the latitude and longitude and return in JSON format provided.
            - the text is: "$voiceTranscription"
            - JSON format as follow:
            { "city": "Paris", "latitude": 48.8588254, "longitude": 2.2644627 }
            { "city": "New York", "latitude": 40.6970238, "longitude": -74.1446547 }
            { "city": "Tokyo", "latitude": 35.502031, "longitude":138.4479874 }
            { "city": "London", "latitude": 51.5285257, "longitude": -0.2667453 }
        """.trimIndent()

        jsonModel.generateContent(prompt).text?.let { outputContent ->
            Log.d("outputContent", outputContent)

            // convert outputContent JSON String to JSON object
            val json = org.json.JSONObject(outputContent)
            if(json.has("city") && json.has("latitude") && json.has("longitude")) {
                val city = json.get("city").toString()
                val lat = json.get("latitude").toString().toDouble()
                val long = json.get("longitude").toString().toDouble()

                return WeatherLocation(city, lat, long)
            }
        }

        return null
    }
}