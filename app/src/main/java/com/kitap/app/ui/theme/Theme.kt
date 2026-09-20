package com.kitap.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Bölüm 6 standardı yarıçaplar:
// Küçük öğeler (rozetler): 8-10dp, Düğme ve form: 12dp, Kart: 18dp, Tam yuvarlak: 999dp
val KitapShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(18.dp),
)

private val LightColors = lightColorScheme(
    primary = Vurgu, onPrimary = Color.White,
    primaryContainer = VurguSoft, onPrimaryContainer = VurguGuclu,
    secondary = Adacayi, onSecondary = Kahve,
    secondaryContainer = AdacayiSoft, onSecondaryContainer = AdacayiMurekkep,
    background = Krem, onBackground = Kahve,
    surface = KremYuzey, onSurface = Kahve,
    surfaceVariant = KremYuzey2, onSurfaceVariant = KahveSoluk,
    outline = KahveSoluk, outlineVariant = KremCizgi,
    error = Color(0xFFB3261E), onError = Color.White,
    surfaceContainerLowest = KremYuzey, surfaceContainerLow = KremYuzey,
    surfaceContainer = KremYuzey2, surfaceContainerHigh = KremYuzey2, surfaceContainerHighest = KremYuzey2,
)

private val DarkColors = darkColorScheme(
    primary = KoyuVurgu, onPrimary = Color.White,
    primaryContainer = KoyuVurguSoft, onPrimaryContainer = KoyuVurguGuclu,
    secondary = Adacayi, onSecondary = Kahve,
    secondaryContainer = KoyuAdacayiSoft, onSecondaryContainer = KoyuAdacayiMurekkep,
    background = KoyuZemin, onBackground = KoyuMurekkep,
    surface = KoyuYuzey, onSurface = KoyuMurekkep,
    surfaceVariant = KoyuYuzey2, onSurfaceVariant = KoyuSoluk,
    outline = KoyuKenar, outlineVariant = KoyuCizgi,
    error = Color(0xFFF2B8B5), onError = KoyuZemin,
    surfaceContainerLowest = KoyuZemin, surfaceContainerLow = KoyuYuzey,
    surfaceContainer = KoyuYuzey2, surfaceContainerHigh = KoyuYuzey2, surfaceContainerHighest = KoyuYuzey2,
)

private val AdminColors = darkColorScheme(
    primary = KoyuVurgu, onPrimary = Color.White,
    primaryContainer = KoyuVurguSoft, onPrimaryContainer = KoyuVurguGuclu,
    secondary = Adacayi, onSecondary = Kahve,
    background = EspressoZemin, onBackground = KoyuMurekkep,
    surface = EspressoYuzey, onSurface = KoyuMurekkep,
    surfaceVariant = EspressoYuzey, onSurfaceVariant = KoyuSoluk,
    outline = KoyuKenar, outlineVariant = KoyuCizgi,
    error = Color(0xFFF2B8B5), onError = EspressoZemin,
    surfaceContainerLowest = EspressoZemin, surfaceContainerLow = EspressoYuzey,
    surfaceContainer = EspressoYuzey, surfaceContainerHigh = EspressoYuzey, surfaceContainerHighest = EspressoYuzey,
)

@Composable
fun KitapTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = KitapTypography,
        shapes = KitapShapes,
        content = content,
    )
}

@Composable
fun AdminTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AdminColors,
        typography = KitapTypography,
        shapes = KitapShapes,
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background, content = content)
    }
}
