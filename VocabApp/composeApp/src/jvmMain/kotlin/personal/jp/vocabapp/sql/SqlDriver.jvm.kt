package personal.jp.vocabapp.sql

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import db.WordDatabase
import java.io.File

class JVMDriverFactory: DriverFactory{
    override fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite:word.db")
        if (!File("word.db").exists()) {
            WordDatabase.Schema.create(driver)
        }
        return driver
    }
}

actual fun getDriverFactory(context: Any): DriverFactory {
    return JVMDriverFactory()
}