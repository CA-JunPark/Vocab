package personal.jp.vocabapp.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.text.FontWeight
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import personal.jp.vocabapp.MainActivity
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.currentState
import androidx.glance.state.PreferencesGlanceStateDefinition
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import personal.jp.vocabapp.sql.WordService
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.datastore.preferences.core.intPreferencesKey


class AddWordActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent(context, QuickAddActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
    }
}

val wordKey = stringPreferencesKey("widget_word")
val meaningKey = stringPreferencesKey("widget_meaning")

val shuffledIdsKey = stringPreferencesKey("shuffled_word_names")
val currentIndexKey = intPreferencesKey("current_shuffle_index")


class NextWordActionCallback : ActionCallback, KoinComponent {
    private val wordService: WordService by inject()

    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val activeWords = wordService.getActiveAllWords()
        if (activeWords.isEmpty()) return

        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            val mutablePrefs = prefs.toMutablePreferences()

            val savedShuffledNames = prefs[shuffledIdsKey]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
            val currentIndex = prefs[currentIndexKey] ?: 0

            val currentNames = activeWords.map { it.name }
            // Reshuffle conditions
            val needsReshuffle = savedShuffledNames.isEmpty() || // mising word
                    currentIndex >= savedShuffledNames.size || // seen all word once
                    !savedShuffledNames.all { name -> currentNames.contains(name) } // when data changed

            val targetShuffledNames: List<String>
            val nextIndex: Int

            if (needsReshuffle) {
                targetShuffledNames = currentNames.shuffled()
                nextIndex = 0
            } else {
                targetShuffledNames = savedShuffledNames
                nextIndex = currentIndex
            }

            val targetName = targetShuffledNames[nextIndex]
            val randomWord = activeWords.find { it.name == targetName } ?: activeWords.random()

            val fullMeaning = randomWord.meaningKr ?: ""
            val firstMeaning = fullMeaning.split(Regex("\n"))
                .firstOrNull { it.isNotBlank() }?.trim() ?: "뜻 없음"

            mutablePrefs[wordKey] = randomWord.name
            mutablePrefs[meaningKey] = firstMeaning
            mutablePrefs[shuffledIdsKey] = targetShuffledNames.joinToString(",")
            mutablePrefs[currentIndexKey] = nextIndex + 1

            mutablePrefs
        }

        VocabWidget().update(context, glanceId)
    }
}


class VocabWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<androidx.datastore.preferences.core.Preferences>()
            val word = prefs[wordKey] ?: "Tap '+'"
            val meaning = prefs[meaningKey] ?: "to start"
            VocabWidgetContent(word, meaning)
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun VocabWidgetContent(word: String, meaning: String) {
        val backgroundColor = Color(0xFF1B202D)
        val primaryTextColor = Color.White
        val buttonBackgroundColor = Color(0xFF2B313E)
        val secondaryTextColor = Color(0xFF9BA1B0)

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(8.dp)
                .clickable(actionStartActivity<MainActivity>()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // word
            Spacer(modifier = GlanceModifier.height(4.dp))

            Text(
                text = word,
                maxLines = 1,
                style = TextStyle(
                    color = ColorProvider(primaryTextColor),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            // scrollable meaning section
            LazyColumn(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        text = meaning,
                        style = TextStyle(
                            color = ColorProvider(secondaryTextColor),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(4.dp))

            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // [+]
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = GlanceModifier
                        .size(width = 52.dp, height = 40.dp)
                        .background(ColorProvider(buttonBackgroundColor))
                        .cornerRadius(20.dp)
                        .clickable(actionRunCallback<AddWordActionCallback>())
                ) {
                    Text(
                        text = "+",
                        modifier = GlanceModifier.padding(top = 1.dp),
                        style = TextStyle(
                            color = ColorProvider(secondaryTextColor),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.width(12.dp))

                // [→]
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = GlanceModifier
                        .size(width = 52.dp, height = 40.dp)
                        .background(ColorProvider(buttonBackgroundColor))
                        .cornerRadius(20.dp)
                        .clickable(actionRunCallback<NextWordActionCallback>())
                ) {
                    Text(
                        text = "→",
                        modifier = GlanceModifier.padding(top = 1.dp),
                        style = TextStyle(
                            color = ColorProvider(secondaryTextColor),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }
    }
}