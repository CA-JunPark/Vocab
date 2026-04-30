package personal.jp.vocabapp.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import personal.jp.vocabapp.sql.WordService
import java.util.concurrent.TimeUnit

class VocabUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val wordService: WordService by inject()

    override suspend fun doWork(): Result {
        return try {
            val activeWords = wordService.getActiveAllWords()
            if (activeWords.isEmpty()) return Result.success()

            val manager = GlanceAppWidgetManager(applicationContext)
            val glanceIds = manager.getGlanceIds(VocabWidget::class.java)
            val currentTime = System.currentTimeMillis()
            val oneHourInMillis = TimeUnit.HOURS.toMillis(1)

            glanceIds.forEach { glanceId ->
                updateAppWidgetState(applicationContext, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                    val lastUpdate = prefs[lastUpdateKey] ?: 0L
                    // if widget is not updated in last 1 hour, go update
                    if (currentTime - lastUpdate < oneHourInMillis) {
                        return@updateAppWidgetState prefs
                    }

                    val mutablePrefs = prefs.toMutablePreferences()

                    val savedShuffledNames = prefs[shuffledIdsKey]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
                    val currentIndex = prefs[currentIndexKey] ?: 0
                    val currentNames = activeWords.map { it.name }

                    val needsReshuffle = savedShuffledNames.isEmpty() ||
                            currentIndex >= savedShuffledNames.size ||
                            !savedShuffledNames.all { name -> currentNames.contains(name) }

                    val targetShuffledNames = if (needsReshuffle) currentNames.shuffled() else savedShuffledNames
                    val nextIndex = if (needsReshuffle) 0 else currentIndex

                    val targetName = targetShuffledNames[nextIndex]
                    val randomWord = activeWords.find { it.name == targetName } ?: activeWords.random()
                    val firstMeaning = randomWord.meaningKr.split("\n")
                        .firstOrNull { it.isNotBlank() }?.trim()
                        ?: "뜻 없음"

                    mutablePrefs[wordKey] = randomWord.name
                    mutablePrefs[meaningKey] = firstMeaning
                    mutablePrefs[shuffledIdsKey] = targetShuffledNames.joinToString(",")
                    mutablePrefs[currentIndexKey] = nextIndex + 1
                    mutablePrefs[lastUpdateKey] = currentTime

                    mutablePrefs
                }
                VocabWidget().update(applicationContext, glanceId)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}