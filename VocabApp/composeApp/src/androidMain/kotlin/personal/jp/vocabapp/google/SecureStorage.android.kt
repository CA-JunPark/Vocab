package personal.jp.vocabapp.google

import java.util.prefs.Preferences

actual class SecureStorage actual constructor(pref: Preferences) {
    actual fun saveToken(key: String, token: String) {
    }

    actual fun getToken(key: String): String? {
        TODO("Not yet implemented")
    }

    actual fun deleteToken(key: String) {
    }
}