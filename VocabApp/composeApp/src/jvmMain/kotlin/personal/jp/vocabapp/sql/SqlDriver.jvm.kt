package personal.jp.vocabapp.sql

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import db.WordDatabase
import java.io.File

class JVMDriverFactory : DriverFactory {
    override fun createDriver(): SqlDriver {
        val databaseFile = File("word.db")
        val driver = JdbcSqliteDriver("jdbc:sqlite:word.db")

        if (!databaseFile.exists()) {
            WordDatabase.Schema.create(driver)
        }

        driver.execute(null, "PRAGMA foreign_keys = ON;", 0)

        return driver
    }
}
actual fun getDriverFactory(context: Any): DriverFactory {
    return JVMDriverFactory()
}