package personal.jp.vocabapp.sql

import app.cash.sqldelight.db.SqlDriver

interface DriverFactory{
    fun createDriver(): SqlDriver
}

expect fun getDriverFactory(context: Any = Any()): DriverFactory