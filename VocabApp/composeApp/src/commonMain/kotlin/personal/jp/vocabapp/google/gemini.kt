package personal.jp.vocabapp.google
import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import personal.jp.vocabapp.Secrets

@Serializable
data class GeminiResponse(
    val name: String,
    val meaningKr: List<String>,
    val example: List<String>,
    val antonymEn: List<String>,
    val tags: List<String>,
)


suspend fun enrichWordByGemini(client: HttpClient, word: String): GeminiResponse?{
    return try {
        val response = client.get("${Secrets.LOCAL}/gemini") {
            url {
                parameters.append("word", word)
            }
        }

        if (response.status.isSuccess()) {
            response.body<GeminiResponse>()
        } else {
            Logger.e("Server Error: ${response.status.value} - ${response.status.description}")
            null
        }

    } catch (e: Exception) {
        Logger.e("Error: ${e.message}")
        null
    }
}