package com.grabhub.android.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.grabhub.android.ui.theme.SourceCreality
import com.grabhub.android.ui.theme.SourceMakerWorld
import com.grabhub.android.ui.theme.SourcePrintables
import com.grabhub.android.ui.theme.SourceThingiverse
import com.grabhub.domain.SourceType

@Composable
fun SourceBadge(
    source: SourceType,
    modifier: Modifier = Modifier,
) {
    val label = source.label()
    val tint = sourceColor(source)
    Surface(
        modifier = modifier,
        color = tint.copy(alpha = 0.18f),
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = tint.copy(alpha = 0.95f),
            maxLines = 1,
        )
    }
}

private fun sourceColor(source: SourceType): Color = when (source) {
    SourceType.PRINTABLES -> SourcePrintables
    SourceType.THINGIVERSE -> SourceThingiverse
    SourceType.MAKERWORLD -> SourceMakerWorld
    SourceType.CREALITY_CLOUD -> SourceCreality
}
