package com.grabhub.android.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.grabhub.android.R
import com.grabhub.domain.SourceType

@Composable
fun SourceType.label(): String = stringResource(
    when (this) {
        SourceType.PRINTABLES -> R.string.source_printables
        SourceType.THINGIVERSE -> R.string.source_thingiverse
        SourceType.MAKERWORLD -> R.string.source_makerworld
        SourceType.CREALITY_CLOUD -> R.string.source_creality
    },
)
