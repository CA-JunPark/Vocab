package personal.jp.vocabapp.google

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.crypto.tink.Aead
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import android.util.Base64
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import java.security.GeneralSecurityException
import com.google.crypto.tink.RegistryConfiguration
import java.io.IOException

actual class SecureStorage actual constructor(
    private val dataStore: DataStore<Preferences>,
    private val context: Any?
) {
    private val androidContext = context as Context
    private val aead: Aead

    init {
        try {
            AeadConfig.register()

            val masterKeyUri = "android-keystore://master_key"
            val keysetManager = AndroidKeysetManager.Builder()
                .withSharedPref(androidContext, "vocab_keyset", "vocab_tink_prefs")
                .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
                .withMasterKeyUri(masterKeyUri)
                .build()

            aead = keysetManager.keysetHandle.getPrimitive(
                RegistryConfiguration.get(),
                Aead::class.java
            )
        } catch (e: GeneralSecurityException) {
            throw RuntimeException("Failed to initialize Tink", e)
        } catch (e: IOException) {
            throw RuntimeException("Failed to read/write keyset", e)
        }
    }
    actual suspend fun saveToken(key: String, token: String) {
        try {
            // Encrypt with Tink (Master key is in Keystore)
            val encryptedBytes = aead.encrypt(token.encodeToByteArray(), null)
            val base64Token = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)

            // Save to DataStore
            dataStore.edit { settings ->
                settings[stringPreferencesKey(key)] = base64Token
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual suspend fun getToken(key: String): String {
        try {
            // Get Base64 from DataStore
            val base64Token = dataStore.data.map { it[stringPreferencesKey(key)] }.first() ?: return ""

            // Decrypt with Tink
            val encryptedBytes = Base64.decode(base64Token, Base64.NO_WRAP)
            val decryptedBytes = aead.decrypt(encryptedBytes, null)

            return decryptedBytes.decodeToString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    actual suspend fun deleteToken(key: String) {
        dataStore.edit { it.remove(stringPreferencesKey(key)) }
    }
}

actual fun createDataStorage(context: Any?): DataStore<Preferences> {
    val androidContext = context as Context
    return createDataStore(
        producePath = { context.filesDir.resolve("vocab.preferences_pb").absolutePath }
    )
}