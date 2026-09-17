package io.edenx.androidpark.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Purple500,
    onPrimary = Color.White,
    secondary = Teal200,
    onSecondary = Ink,
    tertiary = Accent,
    background = Color.White,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Mist,
    onSurfaceVariant = Slate,
    outline = Grey,
)

private val DarkColors = darkColorScheme(
    primary = Purple700,
    onPrimary = OnSurface,
    secondary = Teal200,
    onSecondary = Ink,
    tertiary = Accent,
    background = Ink,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = Slate,
    onSurfaceVariant = Mist,
    outline = Grey,
)

/**
 * The Compose counterpart of @style/Theme.FingerChallenge. The two coexist:
 * XML samples keep the MaterialComponents theme, Compose samples wrap their
 * content in this one.
 *
 * Note MenuActivity forces AppCompatDelegate.MODE_NIGHT_NO, so today
 * isSystemInDarkTheme() always reports false and DarkColors is unreachable.
 * That predates this change; values-night/themes.xml is dead for the same
 * reason.
 */
@Composable
fun AndroidParkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AndroidParkTypography,
        content = content,
    )
}
