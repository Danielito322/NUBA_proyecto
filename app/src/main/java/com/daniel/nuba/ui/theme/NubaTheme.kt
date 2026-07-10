package com.daniel.nuba.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val NubaBg = Color(0xFF07101E)
val NubaPanel = Color(0xAA111D31)
val NubaPanelStrong = Color(0xE0142238)
val NubaText = Color(0xFFF4F7FB)
val NubaMuted = Color(0xFF9DAECC)
val NubaLine = Color(0x3346D9FF)
val NubaCyan = Color(0xFF46D9FF)
val NubaViolet = Color(0xFF8A7CFF)
val NubaPink = Color(0xFFFF9CCF)
val NubaAmber = Color(0xFFFFC36D)
val NubaGreen = Color(0xFF66F2B4)

private val Scheme = darkColorScheme(
    primary = NubaViolet,
    secondary = NubaCyan,
    tertiary = NubaPink,
    background = NubaBg,
    surface = NubaPanelStrong,
    onPrimary = Color.White,
    onSecondary = Color(0xFF03111C),
    onBackground = NubaText,
    onSurface = NubaText
)

@Composable
fun NubaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Scheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
