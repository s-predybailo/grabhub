package com.grabhub.cache

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        GrabHubDatabase.Schema.create(driver)
        return driver
    }
}

actual fun createDefaultDatabaseDriverFactory(): DatabaseDriverFactory = DatabaseDriverFactory()
