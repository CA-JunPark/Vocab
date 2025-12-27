package personal.jp.vocabapp.sql

import app.cash.sqldelight.db.SqlDriver
import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import db.WordDatabase


class AndroidDriverFactory(private val context: Context): DriverFactory{
    override fun createDriver(): SqlDriver = AndroidSqliteDriver(WordDatabase.Schema, context, "word.db")
}

actual fun getDriverFactory(context: Any): DriverFactory {
    return AndroidDriverFactory(context as Context)
}