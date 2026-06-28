package com.grabhub.android.di

import com.grabhub.android.ui.detail.DetailViewModel
import com.grabhub.android.ui.feed.FeedViewModel
import com.grabhub.android.ui.home.HomeViewModel
import com.grabhub.android.ui.preferences.UiPreferences
import com.grabhub.android.ui.favorites.FavoritesViewModel
import com.grabhub.android.ui.history.HistoryViewModel
import com.grabhub.android.ui.search.SearchViewModel
import com.grabhub.android.ui.settings.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val androidModule = module {
    single { UiPreferences(androidContext()) }
    viewModel { SearchViewModel(get(), get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { FeedViewModel(get(), get()) }
    viewModel { DetailViewModel(get(), get()) }
    viewModel { FavoritesViewModel(get()) }
    viewModel { HistoryViewModel(get()) }
    viewModel { (thingiverseConfigured: Boolean, versionName: String) ->
        SettingsViewModel(get(), thingiverseConfigured, versionName)
    }
}
