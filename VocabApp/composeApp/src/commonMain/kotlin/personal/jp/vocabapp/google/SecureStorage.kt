package personal.jp.vocabapp.google

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

expect class SecureStorage(dataStore: DataStore<Preferences>, context: Any? = null) {
    suspend fun saveToken(key: String, token: String,)
    suspend fun getToken(key: String): String?
    suspend fun deleteToken(key: String)

}

fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )

expect fun createDataStorage(context: Any? = null): DataStore<Preferences>