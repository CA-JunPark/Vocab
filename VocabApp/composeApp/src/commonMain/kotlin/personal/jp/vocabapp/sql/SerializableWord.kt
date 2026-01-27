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

fun toSerializable(words:List<Word>): List<SerializableWord> {
    return words.map{
        SerializableWord(
            name = it.name,
            meaningKr = it.meaningKr,
            example = it.example,
            antonymEn = it.antonymEn,
            tags = it.tags,
            createdTime = it.createdTime,
            modifiedTime = it.modifiedTime,
            isDeleted = it.isDeleted,
            syncedTime = it.syncedTime,
            note = it.note
        )
    }
}

fun fromSerializable(words:List<SerializableWord>): List<Word> {
    return words.map {
        Word(
            name = it.name,
            meaningKr = it.meaningKr,
            example = it.example,
            antonymEn = it.antonymEn,
            tags = it.tags,
            createdTime = it.createdTime,
            modifiedTime = it.modifiedTime,
            isDeleted = it.isDeleted,
            syncedTime = it.syncedTime,
            note = it.note
        )
    }
}

fun createWord(name: String, meaningKr: String,
               example: String? = "", antonymEn: String? = "", tags: String? = "", note: String? = ""): Word{
    return Word(
        name = name,
        meaningKr = meaningKr,
        // default values
        example = example,
        antonymEn = antonymEn,
        tags = tags,
        createdTime = "",
        modifiedTime = "",
        isDeleted = false,
        syncedTime = null,
        note = note
    )
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


suspend fun sync(client: HttpClient, wordService: WordService, keyDataManager: KeyDataManager){
    try{
        val lastSyncedTime = keyDataManager.getLastSync()

        val unsyncedWords = toSerializable(wordService.getUnsyncedWords(lastSyncedTime))
        Logger.d { "Unsynced Words: $unsyncedWords" }
        // post request
        val response = client.post("${Secrets.LOCAL}/sync") {
            contentType(ContentType.Application.Json)
            setBody(SyncRequest(
                lastSyncTime = lastSyncedTime,
                localChanges = unsyncedWords
            ))
        }

        val syncResult = response.body<SyncResponse>()

        // update local
        val words = fromSerializable(syncResult.wordsToUpdate)
        for (word in words){
            wordService.upsertWord(word)
        }

        // update last synced time
        for (word in syncResult.wordsToUpdate){
            wordService.setSync(word.name)
        }
        keyDataManager.saveLastSync(syncResult.serverTime)
    } catch (e: Exception){
        Logger.e { "${e.message}" }
    }

}

