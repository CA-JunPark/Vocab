package personal.jp.vocabapp

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import org.koin.core.context.startKoin
import personal.jp.vocabapp.di.apiModule
import personal.jp.vocabapp.di.authModule
import personal.jp.vocabapp.di.platformModule
import personal.jp.vocabapp.di.wordModule
import personal.jp.vocabapp.sql.getDriverFactory
import java.util.prefs.Preferences
import kotlin.system.exitProcess

fun main() = application {
    // load window state
    val prefs = Preferences.userRoot().node("personal/jp/vocabapp")
    val width = prefs.getDouble("window_width", 450.0).dp
    val height = prefs.getDouble("window_height", 900.0).dp
    val x = prefs.getDouble("window_x", 100.0).dp
    val y = prefs.getDouble("window_y", 100.0).dp

    val windowState = rememberWindowState(
        position = WindowPosition(x, y),
        size = DpSize(width, height)
    )

    // save window state
    val saveWindowState = {
        val pos = windowState.position
        val size = windowState.size

        if (pos is WindowPosition.Absolute) {
            prefs.putDouble("window_x", pos.x.value.toDouble())
            prefs.putDouble("window_y", pos.y.value.toDouble())
        }
        prefs.putDouble("window_width", size.width.value.toDouble())
        prefs.putDouble("window_height", size.height.value.toDouble())
    }

    startKoin {
        modules(
            wordModule(getDriverFactory()),
            apiModule(),
            platformModule,
            authModule
        )
    }

    Window(
        onCloseRequest = {
            saveWindowState()
            exitApplication()
        },
        state = windowState,
        title = "VocabApp",
        resizable = true
    ) {
        App(onExit = {
            saveWindowState()
            exitProcess(0)
        })
    }
}