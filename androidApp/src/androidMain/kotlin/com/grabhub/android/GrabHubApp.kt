package com.grabhub.android

import android.app.Application
import com.grabhub.android.di.androidModule
import com.grabhub.di.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class GrabHubApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@GrabHubApp)
            modules(
                sharedModule(thingiverseAccessToken = BuildConfig.THINGIVERSE_ACCESS_TOKEN),
                androidModule,
            )
        }
    }
}
