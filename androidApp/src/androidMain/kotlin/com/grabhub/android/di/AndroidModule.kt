package com.grabhub.android.di

import com.grabhub.android.ui.search.SearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val androidModule = module {
    viewModel { SearchViewModel(get()) }
}
