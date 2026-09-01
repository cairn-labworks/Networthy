package com.cairnlabworks.mywealth.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.min

/** One wedge of a [DonutChart]. */
data class PieSlice(
    val label: String,
    val value: Double,
    val color: Color,
)

/**
 * A dependency-free donut chart. Tapping a wedge reports its index through
 * [onSelect] (tapping the selected wedge again, or the empty hole, clears the
 * selection by reporting null). The center [content] slot renders text inside
 * the donut hole.
 */
@Composable
fun DonutChart(
    slices: List<PieSlice>,
    selectedIndex: Int?,
    onSelect: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    diameter: Dp = 220.dp,
    thicknessRatio: Float = 0.42f,
    content: @Composable () -> Unit,
) {
    val total = slices.sumOf { it.value }.toFloat()

    Box(modifier = modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .size(diameter)
                .pointerInput(slices, total) {
                    detectTapGestures { tap ->
                        if (total <= 0f) return@detectTapGestures
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val dx = tap.x - center.x
                        val dy = tap.y - center.y
                        val dist = hypot(dx, dy)
                        val outer = min(size.width, size.height) / 2f
                        val inner = outer * (1f - thicknessRatio)
                        if (dist < inner || dist > outer) {
                            onSelect(null)
                            return@detectTapGestures
                        }
                        // Angle clockwise from 12 o'clock, matching the arc drawing.
                        var theta = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                        theta = (theta + 90f + 360f) % 360f
                        var acc = 0f
                        for (i in slices.indices) {
                            val sweep = slices[i].value.toFloat() / total * 360f
                            if (theta >= acc && theta < acc + sweep) {
                                onSelect(if (selectedIndex == i) null else i)
                                return@detectTapGestures
                            }
                            acc += sweep
                        }
                    }
                },
        ) {
            if (total <= 0f) return@Canvas
            val outer = min(size.width, size.height) / 2f
            val stroke = outer * thicknessRatio
            val radius = outer - stroke / 2f
            val arcSize = Size(radius * 2f, radius * 2f)
            val topLeft = Offset(size.width / 2f - radius, size.height / 2f - radius)

            var startAngle = -90f
            slices.forEachIndexed { index, slice ->
                val sweep = slice.value.toFloat() / total * 360f
                val dim = selectedIndex != null && selectedIndex != index
                drawArc(
                    color = if (dim) slice.color.copy(alpha = 0.30f) else slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweep - GAP_DEGREES,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke),
                )
                startAngle += sweep
            }
        }
        content()
    }
}

private const val GAP_DEGREES = 1.5f
