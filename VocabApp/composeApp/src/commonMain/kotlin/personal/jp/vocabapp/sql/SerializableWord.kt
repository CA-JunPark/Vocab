package personal.jp.vocabapp.sql

import androidx.compose.runtime.Composable
import db.Word
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject
import personal.jp.vocabapp.Data
import personal.jp.vocabapp.Secrets

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
    val synced: Boolean,
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
            synced = it.synced,
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
        synced = false,
        note = note
    )
}

@Serializable
data class SyncedWordsList(
    // response data for sync
    val syncedWords: List<String>
)
suspend fun sync(client: HttpClient, wordService: WordService, keyDataManager: KeyDataManager){
    try{
        // get synced=false Words
        val words = toSerializable(wordService.selectUnsyncedWord())
        // post words to server
        val response = client.post("${Secrets.LOCAL}/sync") {
            contentType(ContentType.Application.Json)
            // Ktor automatically serializes the list because of ContentNegotiation
            setBody(words)
        }
        // get result List<String> of Words.names that are successfully synced
        val syncedWordsList : SyncedWordsList = response.body<SyncedWordsList>()
        // set synced = True for words in syncedWordList
        syncedWordsList.syncedWords.forEach{
            wordService.setSync(it)
        }
        // update lastSyncedTime
        keyDataManager.saveLastSync()
    } catch (e: Exception){
        println("${e.message}")
    }

}

