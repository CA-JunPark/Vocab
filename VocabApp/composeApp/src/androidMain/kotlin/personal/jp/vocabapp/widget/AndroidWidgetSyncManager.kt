package personal.jp.vocabapp.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import db.Word
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import personal.jp.vocabapp.widget.WidgetSyncManager

class AndroidWidgetSyncManager(private val context: Context) : WidgetSyncManager {
    override fun syncWord(word: Word?) {
        CoroutineScope(Dispatchers.IO).launch {
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(VocabWidget::class.java)

            glanceIds.forEach { id ->
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
                    val mutablePrefs = prefs.toMutablePreferences()
                    if (prefs[wordKey] == word?.name) {
                        val firstMeaning = word?.meaningKr?.split("\n")
                            ?.firstOrNull { it.isNotBlank() }?.trim() ?: "뜻 없음"
                        mutablePrefs[meaningKey] = firstMeaning
                    }
                    mutablePrefs
                }
                VocabWidget().update(context, id)
            }
        }
    }
}