package personal.jp.vocabapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import personal.jp.vocabapp.di.apiModule
import personal.jp.vocabapp.di.authModule
import personal.jp.vocabapp.di.platformModule
import personal.jp.vocabapp.di.wordModule
import personal.jp.vocabapp.google.AuthFlowManager
import personal.jp.vocabapp.screens.Screen
import personal.jp.vocabapp.sql.getDriverFactory
import personal.jp.vocabapp.widget.VocabUpdateWorker
import java.util.concurrent.TimeUnit

// Android
class MainActivity : ComponentActivity() {
    private val authFlowManager: AuthFlowManager by inject()
    private var currentScreen by mutableStateOf<Screen>(Screen.Home)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (GlobalContext.getOrNull() == null) {
            startKoin{
                androidContext(this@MainActivity)
                modules(wordModule(getDriverFactory(this@MainActivity)), apiModule(),
                    platformModule, authModule)
            }
        }

        setupAutoUpdateTask()

        handleIntent(intent)

        setContent {
            App(
                currentScreen = currentScreen,
                onScreenChange = { currentScreen = it },
                onExit = { finish() }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // when the app is already running in the background,
        // update to current new intent
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val data: Uri? = intent?.data
        if (data != null && data.scheme == "personal.jp.vocabapp") {
            val code = data.getQueryParameter("code")
            code?.let { authFlowManager.onCodeReceived(it) }
            return
        }

        if (intent?.action == "ACTION_OPEN_WORD_DETAIL") {
            val wordName = intent.getStringExtra("WORD_NAME")
            if (!wordName.isNullOrBlank() && wordName != "Tap '→'") {
                currentScreen = Screen.WordDetail(wordName)
            }
        }
    }

    private fun setupAutoUpdateTask() {
        val workRequest = PeriodicWorkRequestBuilder<VocabUpdateWorker>(
            1, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "VocabAutoUpdate",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}