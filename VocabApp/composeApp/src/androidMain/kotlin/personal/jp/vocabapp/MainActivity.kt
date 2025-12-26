package personal.jp.vocabapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.cash.sqldelight.db.SqlDriver
import db.WordDatabase
import personal.jp.vocabapp.sql.DriverFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

//        val driver = DriverFactory(this).createDriver()

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
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