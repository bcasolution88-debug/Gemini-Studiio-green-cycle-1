package com.example.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.example.data.repository.FirestoreManager

class ThemeManager(
    initialDark: Boolean = true,
    private val context: Context? = null
) {
    var isDarkMode by mutableStateOf(
        context?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            ?.getBoolean("is_dark_mode", initialDark) ?: initialDark
    )
        private set

    fun toggleDarkMode() {
        val newMode = !isDarkMode
        isDarkMode = newMode
        savePreference(newMode)
    }

    fun updateDarkMode(dark: Boolean) {
        isDarkMode = dark
        savePreference(dark)
    }

    private fun savePreference(dark: Boolean) {
        context?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            ?.edit()
            ?.putBoolean("is_dark_mode", dark)
            ?.apply()

        // Also sync theme preference to Firestore user profile
        FirestoreManager.saveUserProfilePreference("usr_default_88", dark)
    }
}

val LocalThemeManager = compositionLocalOf { ThemeManager() }

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = TertiaryGreen,
    onTertiary = OnTertiaryGreen,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant
)

@Composable
fun GreenCycleTheme(
    themeManager: ThemeManager = LocalThemeManager.current,
    content: @Composable () -> Unit
) {
    val darkTheme = themeManager.isDarkMode

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalThemeManager provides themeManager) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
