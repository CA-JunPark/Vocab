package personal.jp.vocabapp.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import co.touchlab.kermit.Logger
import db.Tag
import db.Word
import io.ktor.client.HttpClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import personal.jp.vocabapp.google.enrichWordByGemini
import personal.jp.vocabapp.sql.WordService

class AiFillWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val client: HttpClient by inject()
    private val wordService: WordService by inject()
    private val widgetSyncManager: WidgetSyncManager by inject()

    override suspend fun doWork(): Result {
        val wordName = inputData.getString("WORD_NAME") ?: return Result.failure()

        if (runAttemptCount > 3) {
            Logger.e("Background AI Fill aborted: Exceeded max attempts (4) for '$wordName'")
            return Result.failure()
        }

        return try {
            Logger.d("Enriching in background $wordName (Attempt: ${runAttemptCount + 1})")
            val result = enrichWordByGemini(client, wordName)

            if (result != null) {
                val meaningText = result.meaningKr.joinToString("\n")
                val exampleText = result.example.joinToString("\n")
                val antonymText = result.antonymEn.joinToString("\n")

                val existingWord = wordService.getActiveWordOrNull(wordName)

                val updatedWord = Word(
                    name = wordName.trim(),
                    meaningKr = meaningText,
                    example = exampleText,
                    antonymEn = antonymText,
                    note = existingWord?.note ?: "",
                    createdTime = existingWord?.createdTime ?: "",
                    modifiedTime = "",
                    isDeleted = false,
                    syncedTime = null
                )

                val tags = result.tags.map { Tag(tagName = it, color = "") }

                val success = wordService.upsertWord(updatedWord, tags)

                if (success) {
                    widgetSyncManager.syncWord(updatedWord)
                    Logger.d("Background add $wordName success")
                    Result.success()
                } else {
                    Logger.d("Database update failed for $wordName, retrying...")
                    Result.retry()
                }
            } else {
                Logger.d("Gemini result is null for $wordName, retrying...")
                Result.retry()
            }
        } catch (e: Exception) {
            Logger.e("Exception in AiFillWorker: ${e.message}")
            Result.retry()
        }
    }
}