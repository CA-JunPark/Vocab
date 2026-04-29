package personal.jp.vocabapp.google

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.plugin
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import personal.jp.vocabapp.Secrets
import personal.jp.vocabapp.getPlatform

private val refreshClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}

fun authClient(secureStorage: SecureStorage): HttpClient {
    return HttpClient(CIO) {
//        install(Logging) {
//            logger = object : io.ktor.client.plugins.logging.Logger {
//                override fun log(message: String) {
//                    Logger.d { "HTTP Client: $message" }
//                }
//            }
//            level = LogLevel.ALL
//        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        install(Auth) {
            bearer {
                cacheTokens = false
                loadTokens {
                    // use idToken since backend checks email data
                    val idToken = secureStorage.getToken(ID_TOKEN)
                    val refresh = secureStorage.getToken(REFRESH_TOKEN)

                    if (idToken.isEmpty()) null
                    else BearerTokens(idToken, refresh)
                }

                refreshTokens {
                    Logger.d { "401 Unauthorized detected. Attempting to refresh ID Token..." }
                    val isJvm = getPlatform().name.contains("JVM")
                    Logger.d { "is JVM ${getPlatform().name}" }
                    val clientId = if (isJvm) Secrets.WEB_CLIENT_ID else Secrets.ANDROID_CLIENT_ID
                    try {
                        val response = refreshClient.post("https://oauth2.googleapis.com/token") {
                            setBody(FormDataContent(Parameters.build {
                                append("grant_type", "refresh_token")
                                append("refresh_token", oldTokens?.refreshToken ?: "")
                                append("client_id", clientId)

                                if (isJvm) {
                                    append("client_secret", Secrets.WEB_CLIENT_SECRET)
                                }
                            }))
                        }

                        if (response.status.value == 200) {
                            val tokenData: TokenResponse = response.body()
                            val newIdToken = tokenData.idToken ?: ""
                            val newRefreshToken = tokenData.refreshToken ?: oldTokens?.refreshToken ?: ""

                            if (newIdToken.isNotEmpty()) {
                                secureStorage.saveToken(ID_TOKEN, newIdToken)
                                Logger.i { "ID Token refreshed successfully." }
                            }
                            BearerTokens(newIdToken, newRefreshToken)
                        } else {
                            Logger.e { "Refresh failed: ${response.status}" }
                            null
                        }
                    } catch (e: Exception) {
                        Logger.e { "Refresh Exception: ${e.message}" }
                        null
                    }
                }
            }
        }
    }
}
@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: Int,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("scope") val scope: String,
    @SerialName("token_type") val tokenType: String,
    @SerialName("id_token") val idToken: String? = null
)