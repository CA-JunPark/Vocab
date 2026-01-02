package personal.jp.vocabapp.google

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import personal.jp.vocabapp.Secrets

actual fun authClient(secureStorage: SecureStorage): HttpClient {
    return HttpClient(CIO){
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                }
            )
        }
        install(Auth) {
            bearer {
                loadTokens {
                    // fetch tokens from secureStorage
                    BearerTokens(secureStorage.getToken(ACCESS_TOKEN),
                        secureStorage.getToken(REFRESH_TOKEN))
                }
                refreshTokens {
                    println("Refreshing tokens...")
                    // This block runs automatically when a 401 Unauthorized is received
                    val response = client.post("https://oauth2.googleapis.com/token") {
                        // Refresh calls do not need the Bearer token themselves
                        markAsRefreshTokenRequest()
                        setBody(FormDataContent(Parameters.build {
                            append("grant_type", "refresh_token")
                            append("refresh_token", oldTokens?.refreshToken ?: "")
                            append("client_id", Secrets.WEB_CLIENT_ID)
                            append("client_secret", Secrets.WEB_CLIENT_SECRET)
                        }))
                    }
                    if (response.status.value == 200) {
                        val newToken: TokenResponse = response.body()
                        secureStorage.saveToken(ACCESS_TOKEN, newToken.accessToken)
                        if (newToken.refreshToken != null) {
                            // save new one if it exists
                            secureStorage.saveToken(REFRESH_TOKEN, newToken.refreshToken)
                        }
                        return@refreshTokens BearerTokens(
                            accessToken = newToken.accessToken,
                            refreshToken = newToken.refreshToken ?: oldTokens?.refreshToken!!
                        )
                    } else {
                        secureStorage.deleteToken(ACCESS_TOKEN)
                        secureStorage.deleteToken(REFRESH_TOKEN)
                        return@refreshTokens null
                    }
                }
            }
        }
    }
}