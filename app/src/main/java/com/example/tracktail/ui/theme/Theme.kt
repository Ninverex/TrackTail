package com.example.tracktail.ui.theme

import android.app.Activity
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

// Kolory zielone - jasny motyw
private val Green40 = Color(0xFF2E7D32)
private val GreenGrey40 = Color(0xFF558B2F)
private val LightGreen40 = Color(0xFF66BB6A)

// Kolory zielone - ciemny motyw
private val Green80 = Color(0xFF81C784)
private val GreenGrey80 = Color(0xFF9CCC65)
private val LightGreen80 = Color(0xFFA5D6A7)

private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    secondary = GreenGrey80,
    tertiary = LightGreen80,
    background = Color(0xFF1B1B1B),
    surface = Color(0xFF2C2C2C),
    onPrimary = Color(0xFF003300),
    onSecondary = Color(0xFF1B3700),
    onTertiary = Color(0xFF002200),
    onBackground = Color(0xFFE5E5E5),
    onSurface = Color(0xFFE5E5E5)
)

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    secondary = GreenGrey40,
    tertiary = LightGreen40,
    background = Color(0xFFFAFAFA),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1B),
    onSurface = Color(0xFF1C1B1B)
)

@Composable
fun TrackTailTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
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