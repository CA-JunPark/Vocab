package personal.jp.vocabapp.google

import com.sun.jna.platform.win32.Crypt32
import com.sun.jna.platform.win32.WinCrypt
import java.util.Base64
import java.util.prefs.Preferences





actual class SecureStorage actual constructor(pref: Preferences) {
    val pref = pref
    actual fun saveToken(key: String, token: String) {
        try {
            // Encrypt the string using Windows DPAPI
            val dataIn = WinCrypt.DATA_BLOB(token.toByteArray(Charsets.UTF_8))
            val dataOut = WinCrypt.DATA_BLOB()

            val success = Crypt32.INSTANCE.CryptProtectData(
                dataIn, "UserToken", null, null, null, 0, dataOut
            )

            if (success) {
                // Convert encrypted bytes to Base64 String
                val encryptedBytes = dataOut.pbData.getByteArray(0, dataOut.cbData)
                val base64Token = Base64.getEncoder().encodeToString(encryptedBytes)

                // Save to Windows Registry
                pref.put(key, base64Token)
                pref.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun getToken(key: String): String? {
        try {
            // 1. Get Base64 string from Registry
            val base64Token = pref.get(key, null) ?: return null

            // 2. Decode Base64 to encrypted bytes
            val encryptedBytes = Base64.getDecoder().decode(base64Token)
            val dataIn = WinCrypt.DATA_BLOB(encryptedBytes)
            val dataOut = WinCrypt.DATA_BLOB()

            // 3. Decrypt using Windows DPAPI
            val success = Crypt32.INSTANCE.CryptUnprotectData(
                dataIn, null, null, null, null, 0, dataOut
            )

            if (success) {
                return String(dataOut.pbData.getByteArray(0, dataOut.cbData), Charsets.UTF_8)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    actual fun deleteToken(key: String) {
        pref.remove(key)
        pref.flush()
    }
}