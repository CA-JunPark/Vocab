package personal.jp.vocabapp.tts

expect class TTSManager {

    fun speak(text: String)

    fun dispose()
}