package personal.jp.vocabapp.sql
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateTime
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlinx.io.IOException

class KeyDataManager(private val dataStore: DataStore<Preferences>) {
    // Save the time
    suspend fun saveLastSync(serverTime:String? = null) {
        if (serverTime != null){
            dataStore.edit { settings ->
                settings[stringPreferencesKey("lastSyncedTime")] = serverTime
            }
        }
        else{
            dataStore.edit { settings ->
                settings[stringPreferencesKey("lastSyncedTime")] = getSqlTimestamp()
            }
        }
    }

    // Retrieve the time (default to 0 if not found)
    suspend fun getLastSync(): String {
        val token = dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[stringPreferencesKey("lastSyncedTime")]
            }
            .first() ?: return ""

        return token
    }

    fun getSqlTimestamp(): String {
        val now = Clock.System.now()
        // use TimeZone.UTC for consistency
        val dateTime = now.toLocalDateTime(TimeZone.UTC)

        return "%04d-%02d-%02d %02d:%02d:%02d".format(
            dateTime.year,
            dateTime.month.number,
            dateTime.day,
            dateTime.hour,
            dateTime.minute,
            dateTime.second
        )
    }
}

