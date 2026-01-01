package personal.jp.vocabapp.google

import java.util.prefs.Preferences

expect class SecureStorage() {
    fun saveToken(key: String, token: String,)
    fun getToken(key: String): String?
    fun deleteToken(key: String)

}
