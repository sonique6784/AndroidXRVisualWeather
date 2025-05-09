package fr.sonique.mygeminiapplication

import android.graphics.Bitmap

/**
 * A sealed hierarchy describing the state of the text generation.
 */
sealed interface UiState {

    /**
     * Empty state when the screen is first shown
     */
    object Initial : UiState

    /**
     * Still loading
     */
    object Loading : UiState

    /**
     * Image has been generated
     */
    data class SuccessBitmap(val outputText: String, val outputBitmap: Bitmap? = null, val name: String? = null, val type: WeatherFor3dModel? = null, val temperature: Double? = null) : UiState

    data class SaveBitmap(val outputBitmap: Bitmap, val name: String) : UiState

    /**
     * There was an error generating text
     */
    data class Error(val errorMessage: String) : UiState


    /**
     * There was an error generating text
     */
    object DebugMode : UiState
}