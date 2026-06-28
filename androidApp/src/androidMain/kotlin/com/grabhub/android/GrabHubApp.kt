package com.grabhub.android

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.grabhub.android.BuildConfig
import com.grabhub.android.di.androidModule
import com.grabhub.cache.DatabaseDriverFactory
import com.grabhub.di.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class GrabHubApp : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()
        SingletonImageLoader.setSafe { context -> newImageLoader(context) }
        startKoin {
            androidContext(this@GrabHubApp)
            modules(
                sharedModule(
                    databaseDriverFactory = DatabaseDriverFactory(this@GrabHubApp),
                    thingiverseAccessToken = BuildConfig.THINGIVERSE_ACCESS_TOKEN,
                ),
                androidModule,
            )
        }
    }

    override fun newImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory())
            }
            .crossfade(true)
            .build()
    }
}
