package personal.jp.vocabapp.sql

import db.Word
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import personal.jp.vocabapp.Secrets
import co.touchlab.kermit.Logger
import db.Tag
import io.ktor.client.statement.bodyAsText
import personal.jp.vocabapp.viewmodels.WordWithTags

@Serializable
data class SerializableWord(
    val name: String,
    val meaningKr: String,
    val example: String?,
    val antonymEn: String?,
    val tags: String?,
    val createdTime: String,
    val modifiedTime: String,
    val isDeleted: Boolean,
    val syncedTime: String?,
    val note: String? = ""
)

fun toSerializable(word: Word, tags: List<Tag>): SerializableWord {
    return SerializableWord(
        name = word.name,
        meaningKr = word.meaningKr,
        example = word.example,
        antonymEn = word.antonymEn,
        createdTime = word.createdTime,
        modifiedTime = word.modifiedTime,
        isDeleted = word.isDeleted,
        syncedTime = word.syncedTime,
        note = word.note,
        tags = tags.joinToString(",") { it.tagName }// get only names
    )
}


fun fromSerializable(sWord: SerializableWord): Pair<Word, List<db.Tag>> {
    val word = Word(
        name = sWord.name,
        meaningKr = sWord.meaningKr,
        example = sWord.example,
        antonymEn = sWord.antonymEn,
        createdTime = sWord.createdTime,
        modifiedTime = sWord.modifiedTime,
        isDeleted = sWord.isDeleted,
        syncedTime = sWord.syncedTime,
        note = sWord.note
    )
    // put default color for tags
    val tags = sWord.tags?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotBlank() }
        ?.map { Tag(it, "#808080") } // give default color
        ?: emptyList()
    return Pair(word, tags)
}

@Serializable
data class SyncRequest(
    val lastSyncTime: String,
    val localChanges: List<SerializableWord> // These are the words where syncedTime IS NULL
)

@Serializable
data class SyncResponse(
    val wordsToUpdate: List<SerializableWord>,
    val serverTime: String
)


suspend fun sync(client: HttpClient, wordService: WordService, keyDataManager: KeyDataManager) {
    try {
        val lastSyncedTime = keyDataManager.getLastSync() ?: "1970-01-01 00:00:00"
        Logger.d {"lastSyncedTime: $lastSyncedTime"}
        val unsyncedWordsRaw = wordService.getUnsyncedWords(lastSyncedTime)

        val localChanges = unsyncedWordsRaw.map { word ->
            val tags = wordService.getTagsForWord(word.name)
            toSerializable(word, tags)
        }

        val response = client.post("${Secrets.BACKEND_API}/sync") {
            contentType(ContentType.Application.Json)
            setBody(SyncRequest(lastSyncTime = lastSyncedTime, localChanges = localChanges))
        }
        Logger.d { "HTTP Status: ${response.status}" }

        val responseText = response.bodyAsText()
        println("RAW SERVER RESPONSE: $responseText")

        val syncResult = response.body<SyncResponse>()

        Logger.d {"Sync Count: ${syncResult.wordsToUpdate.size}"}
        for (sWord in syncResult.wordsToUpdate) {
            println("Updating word: ${sWord.name}")
            val (word, tags) = fromSerializable(sWord)
            // assign colors in upserWord
            wordService.upsertWord(word, tags)
            wordService.setSync(word.name)
        }

        Logger.d {"serverTime: ${syncResult.serverTime}"}
        keyDataManager.saveLastSync(syncResult.serverTime)
    } catch (e: Exception) {
        Logger.e { "Sync Failed: ${e.message}" }
        e.printStackTrace()
    }
}