package personal.jp.vocabapp.google

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

actual class SecureStorage actual constructor(private val dataStore: DataStore<Preferences>) {
    actual suspend fun saveToken(key: String, token: String) {
    }

    actual suspend fun getToken(key: String): String? {

        return null
    }

    actual suspend fun deleteToken(key: String) {
    }
}

actual fun createDataStorage(context: Any?): DataStore<Preferences> {
    val androidContext = context as Context
    return createDataStore(
        producePath = { context.filesDir.resolve("vocab.preferences_pb").absolutePath }
    )
}