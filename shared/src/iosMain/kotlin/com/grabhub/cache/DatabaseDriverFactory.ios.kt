package com.grabhub.cache

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver =
        NativeSqliteDriver(GrabHubDatabase.Schema, "grabhub.db")
}

actual fun createDefaultDatabaseDriverFactory(): DatabaseDriverFactory = DatabaseDriverFactory()
