package personal.jp.vocabapp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import app.cash.sqldelight.db.SqlDriver
import db.WordDatabase
import personal.jp.vocabapp.sql.DriverFactory

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "VocabApp",
    ) {
        //val driver = DriverFactory().createDriver()
        App()
    }
}

//TODO Remove later
fun testDB(driver: SqlDriver){
    val database = WordDatabase(driver)
    val dbQuery = database.wordDatabaseQueries

    dbQuery.insertWord("Potato", "감자", "test", "test",
        "test", "test", "test", false)

    val allWords = dbQuery.selectAllWordsInfo().executeAsList()
    println("Start")
    println(allWords)
    println("End")

}