package com.cairnlabworks.mywealth.ui.statistics

import androidx.compose.ui.graphics.Color

/**
 * A qualitative palette used to colour pie/donut wedges. Colours are chosen to
 * stay legible in both light and dark themes and to be reasonably distinct.
 */
object ChartColors {
    private val palette = listOf(
        Color(0xFF00897B), // teal
        Color(0xFF3949AB), // indigo
        Color(0xFFF9A825), // amber
        Color(0xFF8E24AA), // purple
        Color(0xFF43A047), // green
        Color(0xFFE53935), // red
        Color(0xFF1E88E5), // blue
        Color(0xFFF4511E), // deep orange
        Color(0xFF6D4C41), // brown
        Color(0xFF00ACC1), // cyan
        Color(0xFF7CB342), // light green
        Color(0xFFD81B60), // pink
    )

    fun at(index: Int): Color = palette[index % palette.size]
}
