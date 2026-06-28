package com.grabhub.android.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.grabhub.android.ui.theme.GrabHubShapes
import com.grabhub.android.ui.theme.GrabHubTypography
import com.grabhub.android.ui.theme.Indigo500
import com.grabhub.android.ui.theme.Indigo600
import com.grabhub.android.ui.theme.Indigo700
import com.grabhub.android.ui.theme.Slate100
import com.grabhub.android.ui.theme.Slate50
import com.grabhub.android.ui.theme.Slate800
import com.grabhub.android.ui.theme.Slate900
import com.grabhub.android.ui.theme.Slate950
import com.grabhub.android.ui.theme.Violet400

private val LightColors = lightColorScheme(
    primary = Indigo600,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Indigo700,
    secondary = Color(0xFF7C3AED),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE9FE),
    onSecondaryContainer = Color(0xFF5B21B6),
    tertiary = Color(0xFF0891B2),
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate800,
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    surfaceContainerLow = Color(0xFFF8FAFC),
    surfaceContainer = Color(0xFFF1F5F9),
    surfaceContainerHigh = Color(0xFFE2E8F0),
)

private val DarkColors = darkColorScheme(
    primary = Violet400,
    onPrimary = Slate950,
    primaryContainer = Color(0xFF3730A3),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFFC4B5FD),
    onSecondary = Slate950,
    secondaryContainer = Color(0xFF4C1D95),
    onSecondaryContainer = Color(0xFFEDE9FE),
    tertiary = Color(0xFF67E8F9),
    background = Slate950,
    onBackground = Color(0xFFF8FAFC),
    surface = Slate900,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Slate800,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF334155),
    surfaceContainerLow = Color(0xFF1E293B),
    surfaceContainer = Color(0xFF334155),
    surfaceContainerHigh = Color(0xFF475569),
)

@Composable
fun GrabHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = GrabHubTypography,
        shapes = GrabHubShapes,
        content = content,
    )
}
