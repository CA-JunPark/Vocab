package personal.jp.vocabapp.sql

import app.cash.sqldelight.db.SqlDriver
import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import db.WordDatabase


actual class DriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(WordDatabase.Schema, context, "word.db")
    }
}