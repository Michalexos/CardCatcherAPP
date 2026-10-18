package com.cardcatcher.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Značkové farby CardCatcher
val Navy = Color(0xFF00284C)        // pozadie aplikácie
val NavyDeep = Color(0xFF001B36)    // spodné menu, vstupy, prepínače
val Surface1 = Color(0xFF0A3963)    // karty
val Surface2 = Color(0xFF14507F)    // zvýraznené plochy, okraje
val Mint = Color(0xFFCAFCE1)        // akcenty, výbery
val Pink = Color(0xFFF19AB7)        // len hlavné tlačidlá a FAB
val TextMain = Color(0xFFEAFFF4)
val TextMuted = Color(0xFF9DC4D6)
val Profit = Color(0xFF6BE3A2)
val Loss = Color(0xFFFF8FA8)

// Alias, aby starší kód, ktorý ešte používa Purple, nepadol (už je to modrá, nie fialová)
val Purple = Surface1

private val DarkColors: ColorScheme = darkColorScheme(
    primary = Pink,
    onPrimary = Navy,
    secondary = Mint,
    onSecondary = Navy,
    tertiary = Mint,
    background = Navy,
    onBackground = TextMain,
    surface = Surface1,
    onSurface = TextMain,
    surfaceVariant = Surface2,
    onSurfaceVariant = TextMuted,
    outline = Mint.copy(alpha = 0.4f),
    error = Loss
)

private val CardCatcherTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 34.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 26.sp,
        letterSpacing = (-0.3).sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
)

@Composable
fun CardCatcherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = CardCatcherTypography,
        content = content
    )
}