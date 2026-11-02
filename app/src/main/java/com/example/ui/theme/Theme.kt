package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SifaCyanLight,
    onPrimary = Color(0xFF003258),
    primaryContainer = SifaCyanDark,
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = SifaAccent,
    onSecondary = Color(0xFF003258),
    secondaryContainer = Color(0xFF00497B),
    onSecondaryContainer = Color(0xFFCBE6FF),
    tertiary = SifaWarmOrange,
    background = SifaDarkBackground,
    surface = SifaDarkSurface,
    surfaceVariant = SifaDarkSurfaceVariant,
    onBackground = Color(0xFFEDF5FD),
    onSurface = Color(0xFFEDF5FD)
)

private val LightColorScheme = lightColorScheme(
    primary = SifaCyan,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD0EDFF),
    onPrimaryContainer = Color(0xFF001E36),
    secondary = SifaCyanDark,
    onSecondary = Color.White,
    secondaryContainer = SifaLightSurfaceVariant,
    onSecondaryContainer = Color(0xFF001E36),
    tertiary = SifaWarmOrange,
    background = SifaLightBackground,
    surface = SifaLightSurface,
    surfaceVariant = SifaLightSurfaceVariant,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A)
)

@Composable
fun WelfareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
