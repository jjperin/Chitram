package com.chitram.tv.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Ink = Color(0xFF0B101B)
val Panel = Color(0xFF151C2A)
val PanelLight = Color(0xFF202B3C)
val Cream = Color(0xFFF5F0E8)
val Muted = Color(0xFFABB5C4)
val Gold = Color(0xFFD9AC70)

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
