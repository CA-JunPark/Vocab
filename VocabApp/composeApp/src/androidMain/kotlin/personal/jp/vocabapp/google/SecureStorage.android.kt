package personal.jp.vocabapp.google

import java.util.prefs.Preferences

actual class SecureStorage actual constructor() {
    actual fun saveToken(key: String, token: String) {
    }

    actual fun getToken(key: String): String? {

        return null
    }

    actual fun deleteToken(key: String) {
    }
}