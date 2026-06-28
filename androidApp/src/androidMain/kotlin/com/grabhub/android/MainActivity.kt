package com.grabhub.android

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.grabhub.android.locale.LocaleHelper
import com.grabhub.android.ui.preferences.UiPreferences

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val languageTag = UiPreferences(newBase).getAppLanguage().localeTag
        super.attachBaseContext(LocaleHelper.wrap(newBase, languageTag))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GrabHubNavHost()
        }
    }
}
