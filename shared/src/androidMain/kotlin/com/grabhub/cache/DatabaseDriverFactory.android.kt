package com.grabhub.cache

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DatabaseDriverFactory(
    private val context: Context,
) {
    actual fun createDriver(): SqlDriver =
        AndroidSqliteDriver(GrabHubDatabase.Schema, context, "grabhub.db")
}

actual fun createDefaultDatabaseDriverFactory(): DatabaseDriverFactory {
    error("Use DatabaseDriverFactory(context) via Koin on Android")
}
