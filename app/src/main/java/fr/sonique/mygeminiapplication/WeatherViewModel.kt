package fr.sonique.mygeminiapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// https://cloud.google.com/vertex-ai/generative-ai/docs/learn/model-versions

class WeatherViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var tapCount: Int = 0
    private val aiWeatherService = AIWeatherService()
    private val weatherService = WeatherService()

//    private val generativeTextModel = GenerativeModel(
//        modelName = "gemini-1.5-flash",
//        apiKey = BuildConfig.apiKey,
//    )

    /**
     * enable debug mode
     */
    fun registerTapOnImage() {
        tapCount++
        if (tapCount > 5) {
            // show Prompt + Go Button
            _uiState.value = UiState.DebugMode
        }
    }

    fun requestWeather(prompt: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.value = UiState.Loading
                updateText(prompt)
                // trigger Gemini and ask to find what is the weather like in the city
                aiWeatherService.sendJSONPrompt(prompt)?.let { weatherLocation ->
                    val weatherResponse = weatherService.getWeather(weatherLocation)
                    val weatherLike = weatherResponse?.current?.getWeatherName() ?: "sunny"
                    val threeDModel = weatherResponse?.current?.getSimpleWeatherName() ?: WeatherFor3dModel.SUNNY
                    updateText("The weather in ${weatherLocation.city} is $weatherLike", threeDModel)
                    aiWeatherService.sendBitmapPrompt(
                        cityName = weatherLocation.city,
                        weatherLike = weatherLike
                    )?.let { bitmap ->
                        _uiState.value =
                            UiState.SaveBitmap(bitmap, weatherLocation.city + " " + weatherLike)

                        _uiState.value =
                            UiState.SuccessBitmap(bitmap, weatherLocation.city + " " + weatherLike)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "")
            }
        }
    }

    fun updateText(text: String, model: WeatherFor3dModel? = null) {
        _uiState.value = UiState.Success(text, model)
    }
}