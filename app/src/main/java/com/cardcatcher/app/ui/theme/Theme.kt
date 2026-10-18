package com.cardcatcher.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Navy = Color(0xFF00284C)
val Mint = Color(0xFFCAFCE1)
val Pink = Color(0xFFF19AB7)
val Purple = Color(0xFF461959)
val Profit = Color(0xFF74D89A)
val Loss = Color(0xFFFF7FA4)

private val DarkColors: ColorScheme = darkColorScheme(
    primary = Pink,
    secondary = Mint,
    tertiary = Purple,
    background = Navy,
    surface = Purple,
    onPrimary = Purple,
    onSecondary = Purple,
    onBackground = Mint,
    onSurface = Mint,
    error = Loss
)

private val CardCatcherTypography = androidx.compose.material3.Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 34.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 26.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
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
    )
)

@Composable
fun CardCatcherTheme(content: @Composable () -> Unit) {
    isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = DarkColors,
        typography = CardCatcherTypography,
        content = content
    )
}
