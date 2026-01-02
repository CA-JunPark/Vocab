package personal.jp.vocabapp.google

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sun.jna.platform.win32.Crypt32
import com.sun.jna.platform.win32.WinCrypt
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import java.util.Base64

actual fun createDataStorage(context: Any?): DataStore<Preferences> = createDataStore(
    // Portable .exe application Expected
    producePath = {
        val portableDir = System.getProperty("user.dir")
        File(portableDir, "data/vocab.preferences_pb").absolutePath
    }
)

actual class SecureStorage actual constructor(private val dataStore: DataStore<Preferences>) {
    actual suspend fun saveToken(key: String, token: String) {
        try {
            // 1. Encrypt with DPAPI
            val dataIn = WinCrypt.DATA_BLOB(token.toByteArray(Charsets.UTF_8))
            val dataOut = WinCrypt.DATA_BLOB()
            val success = Crypt32.INSTANCE.CryptProtectData(dataIn, key, null, null, null, 0, dataOut)

            if (success) {
                val encryptedBytes = dataOut.pbData.getByteArray(0, dataOut.cbData)
                val base64Token = Base64.getEncoder().encodeToString(encryptedBytes)

                // 2. Save to DataStore
                dataStore.edit { settings ->
                    settings[stringPreferencesKey(key)] = base64Token
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual suspend fun getToken(key: String): String? {
        try {
            // Get Base64 string from DataStore
            val base64Token = dataStore.data.map { it[stringPreferencesKey(key)] }.first() ?: return null

            // Decode and Decrypt with DPAPI
            val encryptedBytes = Base64.getDecoder().decode(base64Token)
            val dataIn = WinCrypt.DATA_BLOB(encryptedBytes)
            val dataOut = WinCrypt.DATA_BLOB()
            val success = Crypt32.INSTANCE.CryptUnprotectData(dataIn, null, null, null, null, 0, dataOut)

            // Convert decrypted bytes to String
            if (success) {
                return String(dataOut.pbData.getByteArray(0, dataOut.cbData), Charsets.UTF_8)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    actual suspend fun deleteToken(key: String) {
        dataStore.edit { it.remove(stringPreferencesKey(key)) }
    }
}