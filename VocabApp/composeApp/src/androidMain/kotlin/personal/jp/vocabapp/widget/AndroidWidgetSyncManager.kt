package personal.jp.vocabapp.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import db.Word
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import personal.jp.vocabapp.sql.WordService

class AndroidWidgetSyncManager(private val context: Context) : WidgetSyncManager, KoinComponent {
    private val wordService: WordService by inject()

    override fun syncWord(word: Word?) {
        CoroutineScope(Dispatchers.IO).launch {
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(VocabWidget::class.java)

            glanceIds.forEach { id ->
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
                    val mutablePrefs = prefs.toMutablePreferences()
                    if (word != null) {
                        if (prefs[wordKey] == word.name) {
                            val firstMeaning = word.meaningKr.split("\n")
                                .firstOrNull { it.isNotBlank() }?.trim() ?: "뜻 없음"
                            mutablePrefs[meaningKey] = firstMeaning
                        }
                    } else {
                        val activeWords = wordService.getActiveAllWords()
                        if (activeWords.isNotEmpty()) {
                            val nextWord = activeWords.random()
                            val firstMeaning = nextWord.meaningKr.split("\n")
                                .firstOrNull { it.isNotBlank() }?.trim() ?: "뜻 없음"

                            mutablePrefs[wordKey] = nextWord.name
                            mutablePrefs[meaningKey] = firstMeaning
                        } else {
                            mutablePrefs[wordKey] = "Tap '+'"
                            mutablePrefs[meaningKey] = "to start"
                        }
                    }
                    mutablePrefs
                }
                VocabWidget().update(context, id)
            }
        }
    }
}