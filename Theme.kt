package com.calmaceleste.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Paleta celeste suave. Para cambiar la identidad visual de toda la app, edita solo estos colores.
private val LightColors = lightColorScheme(
    primary = Color(0xFF8ACDF0),
    onPrimary = Color(0xFF0B3954),
    primaryContainer = Color(0xFFD7EEFB),
    onPrimaryContainer = Color(0xFF0B3954),
    secondary = Color(0xFFB7E3F7),
    onSecondary = Color(0xFF0B3954),
    secondaryContainer = Color(0xFFE3F4FC),
    onSecondaryContainer = Color(0xFF0B3954),
    tertiary = Color(0xFFA9D8CF),
    onTertiary = Color(0xFF0B3954),
    background = Color(0xFFEAF6FD),
    onBackground = Color(0xFF12384F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF12384F),
    surfaceVariant = Color(0xFFDDEFFA),
    onSurfaceVariant = Color(0xFF3F6177),
    outline = Color(0xFF9CC6DE),
    error = Color(0xFFD9726E)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8ACDF0),
    onPrimary = Color(0xFF06263A),
    primaryContainer = Color(0xFF1D4A68),
    onPrimaryContainer = Color(0xFFD7EEFB),
    secondary = Color(0xFF6FB6DC),
    onSecondary = Color(0xFF06263A),
    secondaryContainer = Color(0xFF173F5C),
    onSecondaryContainer = Color(0xFFD7EEFB),
    tertiary = Color(0xFF8FC9BE),
    onTertiary = Color(0xFF06263A),
    background = Color(0xFF0B2336),
    onBackground = Color(0xFFE4F3FC),
    surface = Color(0xFF12324A),
    onSurface = Color(0xFFE4F3FC),
    surfaceVariant = Color(0xFF1B4560),
    onSurfaceVariant = Color(0xFFB5D6E8),
    outline = Color(0xFF5F8CA6),
    error = Color(0xFFF09A96)
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp)
)

private val base = Typography()
private val AppTypography = base.copy(
    headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
    headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = base.bodyLarge.copy(fontSize = 17.sp, lineHeight = 26.sp),
    bodyMedium = base.bodyMedium.copy(fontSize = 15.sp, lineHeight = 22.sp)
)

@Composable
fun CalmaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
