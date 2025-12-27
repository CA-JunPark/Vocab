package personal.jp.vocabapp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import app.cash.sqldelight.db.SqlDriver
import co.touchlab.kermit.Logger
import db.Word
import db.WordDatabase
import org.koin.core.Koin
import org.koin.core.context.startKoin
import personal.jp.vocabapp.di.WordRepo
import personal.jp.vocabapp.di.WordServiceImpl
import personal.jp.vocabapp.di.appModule
import personal.jp.vocabapp.sql.getDriverFactory

fun main() = application {
    val koinApp = startKoin{
        modules(appModule(getDriverFactory()))
    }

    val service: WordServiceImpl = koinApp.koin.get()

    val data = testDB(service)

    Window(
        onCloseRequest = ::exitApplication,
        title = "VocabApp",
    ) {
        App(data=data)
    }
}

//TODO Remove later
fun testDB(service: WordServiceImpl): List<Word>{


    val word:Word = Word("Potato", "감자", "test", "test",
        "test", "null", "null", false)

    Logger.d { "This is a debug log" }
    service.deleteAllWords()
    if(!service.addWord(word)){
        println("Insert failed")
    }

    val allWords = service.getAllWords()
    return allWords
}