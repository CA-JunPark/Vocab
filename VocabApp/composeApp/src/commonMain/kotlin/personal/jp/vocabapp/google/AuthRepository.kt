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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import personal.jp.vocabapp.Platform
import personal.jp.vocabapp.Secrets

interface LoginHandler {
    /**
     * Starts the browser flow.
     * @param onCodeReceived callback when the auth code is captured.
     */
    fun login(onCodeReceived: (String) -> Unit)
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

    init {
        println("!!! AuthRepository: Initializing and collecting flow...")
        // Listen for codes arriving from either JVM or Android
        authFlowManager.authCode
            .onEach { code ->
                println("!!! AuthRepository: Flow received code: $code")
                exchangeCodeForToken(code)
            }
            .launchIn(scope)
    }

    fun startLogin(){
        loginHandler.login { code ->
            // This callback is used primarily by JVM (Netty)
            authFlowManager.onCodeReceived(code)
        }
    }

    private suspend fun exchangeCodeForToken(code: String) {
        if (platform.name.contains("JVM")){
            exchangeCodeForTokenJVM(code)
        }
        else{
            exchangeCodeForTokenAndroid(code)

        }
    }

    private suspend fun exchangeCodeForTokenJVM(code: String) {
        try {
            val response: TokenResponse = httpClient.post("https://oauth2.googleapis.com/token") {
                setBody(FormDataContent(Parameters.build {
                    append("grant_type", "authorization_code")
                    append("code", code)
                    append("client_id", Secrets.WEB_CLIENT_ID)
                    append("client_secret", Secrets.WEB_CLIENT_SECRET) // Needed for JVM
                    append("redirect_uri", "http://localhost:8080/callback")
                }))
            }.body()

            saveTokens(response)
        } catch (e: Exception) {
            println("Exchange failed: ${e.message}")
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
            println("Exchange failed: ${e.message}")
        }
    }

    private suspend fun saveTokens(response: TokenResponse) {
        // store in secure storage
        secureStorage.saveToken("access_token", response.accessToken)
        response.refreshToken?.let {
            secureStorage.saveToken("refresh_token", it)
        }
    }
}