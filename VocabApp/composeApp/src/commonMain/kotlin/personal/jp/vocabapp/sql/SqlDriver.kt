package personal.jp.vocabapp.sql

import app.cash.sqldelight.db.SqlDriver
import db.WordDatabase

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DriverFactory): WordDatabase {
    val driver = driverFactory.createDriver()
    val database = WordDatabase(driver)

    return database
}