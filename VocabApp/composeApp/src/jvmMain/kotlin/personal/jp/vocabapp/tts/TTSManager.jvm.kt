package personal.jp.vocabapp.tts

import co.touchlab.kermit.Logger

actual class TTSManager(context: Any?) {

    actual fun speak(text: String) {
        try {
            val safeText = text.replace("'", "''")

            val script = "Add-Type -AssemblyName System.Speech; " +
                    "\$speak = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                    "\$speak.Speak('$safeText')"

            ProcessBuilder("powershell", "-WindowStyle", "Hidden", "-Command", script).start()

            Logger.d("Speak $text")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun dispose() {

    }
}