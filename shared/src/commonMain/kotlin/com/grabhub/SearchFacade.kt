package com.grabhub

import com.grabhub.cache.createDefaultDatabaseDriverFactory
import com.grabhub.data.SearchRepository
import com.grabhub.di.sharedModule
import com.grabhub.domain.SearchQuery
import com.grabhub.domain.SearchResult
import kotlinx.coroutines.runBlocking
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

/**
 * Thin facade for Swift/iOS to call shared search logic without dealing with coroutines directly.
 */
class SearchFacade(
    thingiverseAccessToken: String = "",
) {
    private val koin = KoinHolder.get(thingiverseAccessToken)

    fun searchBlocking(query: String): SearchResult = runBlocking {
        koin.get<SearchRepository>().search(SearchQuery(text = query))
    }

    companion object {
        fun reset() {
            KoinHolder.reset()
        }
    }
}

private object KoinHolder {
    @Volatile
    private var started = false

    fun get(thingiverseAccessToken: String) = synchronized(this) {
        if (!started) {
            startKoin { modules(sharedModule(createDefaultDatabaseDriverFactory(), thingiverseAccessToken)) }
            started = true
        }
        GlobalContext.get()
    }

    fun reset() = synchronized(this) {
        if (started) {
            stopKoin()
            started = false
        }
    }
}
