package personal.jp.vocabapp.google

import io.ktor.client.HttpClient
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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

private val refreshClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}

fun authClient(secureStorage: SecureStorage): HttpClient {
    return HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        install(Auth) {
            bearer {
                loadTokens {
                    val idToken = secureStorage.getToken(ID_TOKEN)
                    val refresh = secureStorage.getToken(REFRESH_TOKEN)
                    if (idToken.isEmpty() || refresh.isEmpty()) null
                    else BearerTokens(idToken, refresh)
                }

                refreshTokens {
                    try {
                        val response = refreshClient.post("https://oauth2.googleapis.com/token") {
                            setBody(FormDataContent(Parameters.build {
                                append("grant_type", "refresh_token")
                                append("refresh_token", oldTokens?.refreshToken ?: "")
                                append("client_id", Secrets.WEB_CLIENT_ID)
                                append("client_secret", Secrets.WEB_CLIENT_SECRET)
                            }))
                        }

                        if (response.status.value == 200) {
                            val newToken: TokenResponse = response.body()
                            // oldToken.accessToken is ID token
                            val id = newToken.idToken ?: oldTokens?.accessToken ?: ""
                            val access = newToken.accessToken
                            val refresh = newToken.refreshToken ?: oldTokens?.refreshToken

                            // save to storage
                            newToken.idToken?.let {
                                secureStorage.saveToken(ID_TOKEN, it)
                            }
                            secureStorage.saveToken(ACCESS_TOKEN, access)
                            newToken.refreshToken?.let{
                                secureStorage.saveToken(REFRESH_TOKEN, it)
                            }

                            if (id.isNotEmpty() && refresh != null) {
                                BearerTokens(id, refresh)
                            } else {
                                null
                            }
                        } else {
                            // if refresh fails, delete tokens
                            secureStorage.deleteToken(ACCESS_TOKEN)
                            secureStorage.deleteToken(REFRESH_TOKEN)
                            secureStorage.deleteToken(ID_TOKEN)
                            null
                        }
                    } catch (e: Exception) {
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