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
import personal.jp.vocabapp.Secrets

actual fun authClient(): HttpClient {
    return HttpClient(CIO){
        install(ContentNegotiation) { json() }
        install(Auth) {
            bearer {
                loadTokens {
                    // Fetch tokens from local storage (Settings/SQLDelight)
                    BearerTokens("accessToken", "refreshToken")
                }
                refreshTokens {
                    // This block runs automatically when a 401 Unauthorized is received
                    val response = client.post("https://oauth2.googleapis.com/token") {
                        // Refresh calls do not need the Bearer token themselves
                        markAsRefreshTokenRequest()
                        setBody(FormDataContent(Parameters.build {
                            append("grant_type", "refresh_token")
                            append("refresh_token", oldTokens?.refreshToken ?: "")
                            append("client_id", Secrets.WEB_CLIENT_ID)
                        }))
                    }
                    if (response.status.value == 200) {
                        val newToken: TokenResponse = response.body()
                        // TODO Save the new tokens to local storage
                        BearerTokens(
                            accessToken = newToken.accessToken,
                            refreshToken = newToken.refreshToken ?: oldTokens?.refreshToken!!
                        )
                    } else {
                        null // Forces user to re-login
                    }
                }
            }
        }
    }
}