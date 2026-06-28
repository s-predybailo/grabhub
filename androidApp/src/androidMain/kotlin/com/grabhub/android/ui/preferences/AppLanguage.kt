package com.grabhub.android.ui.preferences

enum class AppLanguage(val storageKey: String, val localeTag: String?) {
    SYSTEM("system", null),
    RU("ru", "ru"),
    EN("en", "en"),
    ;

    companion object {
        fun fromStorageKey(key: String?): AppLanguage =
            entries.firstOrNull { it.storageKey == key } ?: RU
    }
}
