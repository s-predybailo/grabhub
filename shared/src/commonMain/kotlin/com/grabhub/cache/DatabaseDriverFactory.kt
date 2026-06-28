package com.grabhub.cache

import app.cash.sqldelight.db.SqlDriver

expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

expect fun createDefaultDatabaseDriverFactory(): DatabaseDriverFactory
