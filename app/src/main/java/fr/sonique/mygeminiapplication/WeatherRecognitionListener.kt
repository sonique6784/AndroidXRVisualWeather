package fr.sonique.mygeminiapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class WeatherRecognitionListener(val context: Context, val selectedLanguage: String,
                                 val onResultCallback: (text:String) -> Unit): RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null

    private fun getRecogniserIntent(): Intent {
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
            selectedLanguage
        )
        recognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            selectedLanguage
        )
        recognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, RESULTS_LIMIT)

        return recognizerIntent
    }

    fun startListening() {
        if(speechRecognizer == null) {
            resetSpeechRecognizer()
        }
        speechRecognizer?.startListening(getRecogniserIntent())
        //binding.progressBar1.visibility = View.VISIBLE
    }

    fun resetSpeechRecognizer() {
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        val isAvailable = SpeechRecognizer.isRecognitionAvailable(context)
        Log.d(
            "Speech",
            "isRecognitionAvailable: " + isAvailable
        )
        if (isAvailable) {
            speechRecognizer?.setRecognitionListener(this)
        }
    }

    fun onStopListening() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    override fun onBeginningOfSpeech() {
        Log.d("speech", "onBeginningOfSpeech")
//            binding.progressBar1.isIndeterminate = false
//            binding.progressBar1.max = 10
    }

    override fun onBufferReceived(buffer: ByteArray) {
        Log.d("speech", "onBufferReceived: $buffer")
    }

    override fun onEndOfSpeech() {
        Log.d("speech", "onEndOfSpeech")
        //binding.progressBar1.isIndeterminate = true
        speechRecognizer?.stopListening()
    }

    override fun onResults(results: Bundle) {
        Log.d("speech", "onResults")
        val matches: ArrayList<String>? = results
            .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        var text = ""
        matches?.let { it ->
             text += """
     $it
    
     """.trimIndent()
        }
        Log.d("speech", "TEXT: $text")

        if(text.isNotBlank()) {
            onResultCallback(text)
        }
        if (IS_CONTINUES_LISTEN) {
            startListening()
        } else {
            onStopListening()
        }
    }

    override fun onError(errorCode: Int) {
        val errorMessage = getErrorText(errorCode)
        Log.d("speech", "FAILED $errorMessage")
        //binding.tvError.text = errorMessage

        // reset voice recogniser
        resetSpeechRecognizer()
        startListening()
    }

    override fun onEvent(arg0: Int, arg1: Bundle) {
        Log.d("speech", "onEvent")
    }

    override fun onPartialResults(arg0: Bundle) {
        Log.d("speech", "onPartialResults")
    }

    override fun onReadyForSpeech(arg0: Bundle) {
        Log.d("speech", "onReadyForSpeech")
    }

    override fun onRmsChanged(rmsdB: Float) {
        //binding.progressBar1.progress = rmsdB.toInt()
    }

    fun getErrorText(errorCode: Int): String {
        val message: String = when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED -> "Language Not supported"
            SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> "Language Unavailable"
            else -> "Didn't understand, please try again."
        }
        return message
    }
}