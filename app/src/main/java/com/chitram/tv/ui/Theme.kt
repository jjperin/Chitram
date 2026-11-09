package com.chitram.tv.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Ink = Color(0xFF090B0F)
val Panel = Color(0xFF14171D)
val PanelLight = Color(0xFF252A32)
val Cream = Color(0xFFF7F5F1)
val Muted = Color(0xFF9DA3AD)
val Gold = Color(0xFFE4B675)

@Composable
fun ChitramTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Gold, onPrimary = Ink, background = Ink, onBackground = Cream,
            surface = Panel, onSurface = Cream, secondary = Muted
        ),
        content = content
    )
}
