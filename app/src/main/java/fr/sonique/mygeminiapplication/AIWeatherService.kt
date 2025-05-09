package fr.sonique.mygeminiapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig


// https://console.cloud.google.com/iam-admin/quotas?walkthrough_id=bigquery--bigquery_quota_request&start_index=2&inv=1&invt=Abz8kQ&project=gen-lang-client-0832029529#step_index=1
// https://console.cloud.google.com/iam-admin/quotas?walkthrough_id=bigquery--bigquery_quota_request&start_index=2&inv=1&invt=Abz8kQ&project=gen-lang-client-0832029529&pageState=(%22allQuotasTable%22:(%22f%22:%22%255B%257B_22k_22_3A_22Current%2520usage%2520percentage_22_2C_22t_22_3A5_2C_22v_22_3A_22%257B_5C_22v_5C_22_3A_5C_2290%2525_5C_22_2C_5C_22o_5C_22_3A_5C_22%253E_5C_22%257D_22_2C_22i_22_3A_22currentPercent_22%257D%255D%22,%22p%22:0))#step_index=1
data class WeatherLocation(val city: String, val latitude: Double, val longitude: Double)

class AIWeatherService {

    private val jsonSchema = Schema.obj(
        mapOf(
            "city" to Schema.string(),
            "latitude" to Schema.double(),
            "longitude" to Schema.double()
        )
    )

    private val jsonModel = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel("gemini-2.0-flash-lite-001",
                generationConfig = generationConfig {
                    responseMimeType = "application/json"
                    responseSchema = jsonSchema
                }
        )
//    private val jsonModel = Firebase.vertexAI.generativeModel(
//        modelName = "gemini-2.0-flash-001",
//        // generate JSON data
//        generationConfig = generationConfig {
//            responseMimeType = "application/json"
//            responseSchema = jsonSchema
//        }
//    )

    @OptIn(PublicPreviewAPI::class)
    private val generativeImageModel = Firebase.ai(backend = GenerativeBackend.vertexAI())
        .imagenModel(
        modelName = "imagen-3.0-fast-generate-001"
    )

    @OptIn(PublicPreviewAPI::class)
    suspend fun sendBitmapPrompt(cityName: String, weatherLike: String, time: String): Bitmap? {
        val prompt = """Imagine an image of $cityName in the $time with a $weatherLike weather, in the style of 8bit pixel art."""

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