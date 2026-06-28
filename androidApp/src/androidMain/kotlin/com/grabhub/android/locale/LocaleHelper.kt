package com.grabhub.android.locale

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {

    fun wrap(context: Context, languageTag: String?): Context {
        if (languageTag.isNullOrBlank()) return context
        val locale = Locale.forLanguageTag(languageTag)
        Locale.setDefault(locale)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }
}
