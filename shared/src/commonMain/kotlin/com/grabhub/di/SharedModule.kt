package com.grabhub.di

import com.grabhub.data.SearchRepository
import com.grabhub.engine.SearchEngine
import com.grabhub.network.createHttpClient
import com.grabhub.providers.SearchProvider
import com.grabhub.providers.printables.PrintablesProvider
import com.grabhub.providers.thingiverse.ThingiverseProvider
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.dsl.module

fun sharedModule(thingiverseAccessToken: String = ""): Module = module {
    single<HttpClient> { createHttpClient() }
    single { PrintablesProvider(get()) }
    single { ThingiverseProvider(get(), thingiverseAccessToken) }
    single<List<SearchProvider>> {
        listOf(get<PrintablesProvider>(), get<ThingiverseProvider>())
    }
    single { SearchEngine(get()) }
    single { SearchRepository(get()) }
}
