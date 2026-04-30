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
) {
    fun toDomainModels(): Pair<db.Word, List<db.Tag>> {
        val word = db.Word(
            name = this.name,
            meaningKr = this.meaningKr.joinToString("\n"),
            example = this.example.joinToString("\n"),
            antonymEn = this.antonymEn.joinToString("\n"),
            createdTime = "",
            modifiedTime = "",
            isDeleted = false,
            syncedTime = null,
            note = null
        )

        // Tags Colors are assigned automatically in WordService
        val tagList = this.tags.map { tagName ->
            db.Tag(tagName = tagName.trim(), color = "")
        }

        return Pair(word, tagList)
    }
}


suspend fun enrichWordByGemini(client: HttpClient, word: String): GeminiResponse? {
    return try {
        val response = client.get("${Secrets.BACKEND_API}/gemini") {
            url {
                parameters.append("word", word)
            }
        }

        if (response.status.isSuccess()) {
            val body = response.body<GeminiResponse>()
            Logger.d("Gemini successfully enriched: ${body.name}")
            body
        } else {
            Logger.e("Gemini Server Error: ${response.status.value}")
            null
        }
    } catch (e: Exception) {
        Logger.e("Gemini Request Failed: ${e.message}")
        null
    }
}