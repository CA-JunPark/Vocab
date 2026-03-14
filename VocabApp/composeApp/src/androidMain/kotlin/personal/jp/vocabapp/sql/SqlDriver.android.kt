package personal.jp.vocabapp.sql

import app.cash.sqldelight.db.SqlDriver
import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import db.WordDatabase


class AndroidDriverFactory(private val context: Context) : DriverFactory {
    override fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = WordDatabase.Schema,
            context = context,
            name = "word.db",
            callback = object : AndroidSqliteDriver.Callback(WordDatabase.Schema) {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    db.execSQL("PRAGMA foreign_keys = ON;")
                }
            }
        )
    }
}

actual fun getDriverFactory(context: Any): DriverFactory {
    return AndroidDriverFactory(context as Context)
}