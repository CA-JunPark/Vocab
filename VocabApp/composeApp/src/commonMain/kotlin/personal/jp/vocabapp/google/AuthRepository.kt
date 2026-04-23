package personal.jp.vocabapp.google

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Parameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import personal.jp.vocabapp.Platform
import personal.jp.vocabapp.Secrets
import co.touchlab.kermit.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import personal.jp.vocabapp.Screens.UserProfile
import io.ktor.util.decodeBase64String
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

interface LoginHandler {
    /**
     * Starts the browser flow.
     * @param onCodeReceived callback when the auth code is captured.
     */
    fun login(onCodeReceived: (String) -> Unit)
    fun stop()
}

class AuthFlowManager {
    private val _authCode = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val authCode = _authCode.asSharedFlow()

    fun onCodeReceived(code: String) {
        _authCode.tryEmit(code)
    }
}

class AuthRepository(
    private val loginHandler: LoginHandler,
    private val authFlowManager: AuthFlowManager,
    private val httpClient: HttpClient,
    private val platform: Platform,
    private val secureStorage: SecureStorage
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val _isLoginInProgress = MutableStateFlow(false)
    val isLoginInProgress = _isLoginInProgress.asStateFlow()

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser = _currentUser.asStateFlow()

    init {
        scope.launch {
            if (isLoggedIn()) {
                _currentUser.value = getCurrentUser()
            }
        }
        Logger.d("!!! AuthRepository: Initializing and collecting flow...")
        // Listen for codes arriving from either JVM or Android
        authFlowManager.authCode
            .onEach { code ->
                Logger.d("!!! AuthRepository: Flow received code: $code")
                exchangeCodeForToken(code)
                _currentUser.value = getCurrentUser()
                _isLoginInProgress.value = false // when login is done
                scope.launch {
                    delay(1000) // 1 second is plenty for Netty to finish
                    try {
                        loginHandler.stop()
                    } catch (e: Exception) {
                        // Swallow the specific Netty error if it still happens
                        Logger.d("Server stop warning: ${e.message}")
                    }

                }
            }
            .launchIn(scope)
    }

    suspend fun startLogin(){
        _isLoginInProgress.value = true
        clearTokens()
        loginHandler.login { code ->
            // This callback is used primarily by JVM (Netty)
            authFlowManager.onCodeReceived(code)
        }
    }

    private suspend fun exchangeCodeForToken(code: String) {
        try {
            if (platform.name.contains("JVM")){
                exchangeCodeForTokenJVM(code)
            } else {
                exchangeCodeForTokenAndroid(code)
            }
        } catch (e: Exception) {
            Logger.d("Exchange failed: ${e.message}")
            _isLoginInProgress.value = false // when login is failed
        }
    }

    private suspend fun exchangeCodeForTokenJVM(code: String) {
        try {
            val response: TokenResponse = httpClient.post("https://oauth2.googleapis.com/token") {
                setBody(FormDataContent(Parameters.build {
                    append("grant_type", "authorization_code")
                    append("code", code)
                    append("client_id", Secrets.WEB_CLIENT_ID)
                    append("client_secret", Secrets.WEB_CLIENT_SECRET)
                    append("redirect_uri", "http://localhost:8080/callback")
                }))
            }.body()

            saveTokens(response)
        } catch (e: Exception) {
            Logger.d("Exchange failed: ${e.message}")
        }
    }
    private suspend fun exchangeCodeForTokenAndroid(code: String) {
        try {
            val response: TokenResponse = httpClient.post("https://oauth2.googleapis.com/token") {
                setBody(FormDataContent(Parameters.build {
                    append("grant_type", "authorization_code")
                    append("code", code)
                    append("client_id", Secrets.ANDROID_CLIENT_ID)
                    append("redirect_uri", "personal.jp.vocabapp:/oauth2redirect")
                }))
            }.body()

            saveTokens(response)
        } catch (e: Exception) {
            Logger.d("Exchange failed: ${e.message}")
        }
    }

    private suspend fun saveTokens(response: TokenResponse) {
        // store in secure storage
        secureStorage.saveToken(ACCESS_TOKEN, response.accessToken)
        response.refreshToken?.let {
            secureStorage.saveToken(REFRESH_TOKEN, it)
        }
        secureStorage.saveToken(ID_TOKEN, response.idToken ?: "")
    }

    private suspend fun clearTokens(){
        secureStorage.deleteToken(ID_TOKEN)
        secureStorage.deleteToken(ACCESS_TOKEN)
        secureStorage.deleteToken(REFRESH_TOKEN)
    }

    suspend fun isLoggedIn(): Boolean {
        return secureStorage.getToken(ACCESS_TOKEN) != null
    }

    suspend fun getCurrentUser(): UserProfile? {
        val idToken = secureStorage.getToken(ID_TOKEN) ?: return null

        return try {
            // JWT parts (0: Header, 1: Payload, 2: Signature)
            val parts = idToken.split(".")
            if (parts.size < 2) return null

            val payloadJson = parts[1].decodeBase64Url()
            val decoded = Json { ignoreUnknownKeys = true }.decodeFromString<GoogleIdTokenPayload>(payloadJson)
//            Logger.d("Decoded payload: $decoded")

            UserProfile(
                name = decoded.name ?: "User",
                email = decoded.email ?: "No Email",
                profileImageUrl = decoded.picture
            )
        } catch (e: Exception) {
            Logger.e("ID Token decoding failed: ${e.message}")
            null
        }
    }
    private fun String.decodeBase64Url(): String {
        // Convert Base64Url specific characters (- and _) to standard Base64 (+ and /)
        var base64 = this.replace("-", "+").replace("_", "/")

        // Add padding (=) to ensure the Base64 string length is a multiple of 4
        while (base64.length % 4 != 0) {
            base64 += "="
        }
        return base64.decodeBase64String() // Uses io.ktor.util.decodeBase64String
    }

    suspend fun cancelLogin() {
        _isLoginInProgress.value = false
        try {
            loginHandler.stop()
        } catch (e: Exception) {
            Logger.d("Login stop warning: ${e.message}")
        }
    }

    suspend fun logout() {
        clearTokens()
        _currentUser.value = null
    }
}

@Serializable
private data class GoogleIdTokenPayload(
    val name: String? = null,
    val email: String? = null,
    val picture: String? = null
)