package com.grabhub.di

import com.grabhub.cache.DatabaseDriverFactory
import com.grabhub.cache.GrabHubDatabase
import com.grabhub.cache.LocalCache
import com.grabhub.data.DetailRepository
import com.grabhub.data.SearchRepository
import com.grabhub.engine.SearchEngine
import com.grabhub.network.createHttpClient
import com.grabhub.providers.DetailProvider
import com.grabhub.providers.SearchProvider
import com.grabhub.providers.creality.CrealityCloudProvider
import com.grabhub.providers.makerworld.MakerWorldProvider
import com.grabhub.providers.printables.PrintablesProvider
import com.grabhub.providers.thingiverse.ThingiverseProvider
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.dsl.module

fun sharedModule(
    databaseDriverFactory: DatabaseDriverFactory,
    thingiverseAccessToken: String = "",
): Module = module {
    single<HttpClient> { createHttpClient() }
    single { GrabHubDatabase(databaseDriverFactory.createDriver()) }
    single { LocalCache(get()) }

    single { PrintablesProvider(get()) }
    single { ThingiverseProvider(get(), thingiverseAccessToken) }
    single { MakerWorldProvider(get()) }
    single { CrealityCloudProvider(get()) }

    single<List<SearchProvider>> {
        listOf(
            get<PrintablesProvider>(),
            get<ThingiverseProvider>(),
            get<MakerWorldProvider>(),
            get<CrealityCloudProvider>(),
        )
    }
    single<List<DetailProvider>> {
        listOf(
            get<PrintablesProvider>(),
            get<ThingiverseProvider>(),
            get<MakerWorldProvider>(),
            get<CrealityCloudProvider>(),
        )
    }

    single { SearchEngine(get()) }
    single { SearchRepository(get(), get()) }
    single { DetailRepository(get(), get()) }
}
