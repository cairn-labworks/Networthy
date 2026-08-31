package com.cairnlabworks.mywealth.ui.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

/**
 * A non-lazy vertical list whose items can be reordered by long-pressing the
 * supplied drag handle and dragging up or down. Reordering happens live and the
 * final order is reported through [onReordered] when the gesture ends.
 *
 * The [dragHandle] modifier passed to [itemContent] must be applied to whichever
 * element should start the drag (e.g. the whole row or a header). This keeps the
 * reorder scope self-contained, so items can only move within this list.
 */
@Composable
fun <T> DragReorderColumn(
    items: List<T>,
    keyOf: (T) -> Any,
    onReordered: (List<T>) -> Unit,
    modifier: Modifier = Modifier,
    verticalSpacing: Dp = 0.dp,
    itemContent: @Composable (item: T, isDragging: Boolean, dragHandle: Modifier) -> Unit,
) {
    val spacingPx = with(LocalDensity.current) { verticalSpacing.roundToPx() }
    val haptics = LocalHapticFeedback.current

    // Live, reorderable copy that re-syncs whenever the source list changes.
    var order by remember(items) { mutableStateOf(items) }
    val heights = remember { mutableStateMapOf<Any, Int>() }
    var draggingKey by remember { mutableStateOf<Any?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(verticalSpacing)) {
        order.forEach { item ->
            val key = keyOf(item)
            val isDragging = draggingKey == key
            key(key) {
                val handle = Modifier.pointerInput(key) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            draggingKey = key
                            dragOffset = 0f
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragOffset += dragAmount.y
                            val cur = order.indexOfFirst { keyOf(it) == draggingKey }
                            if (cur >= 0) {
                                if (dragOffset > 0 && cur < order.lastIndex) {
                                    val nextH = heights[keyOf(order[cur + 1])] ?: 0
                                    if (dragOffset > (nextH + spacingPx) / 2f) {
                                        order = order.toMutableList().apply { add(cur + 1, removeAt(cur)) }
                                        dragOffset -= (nextH + spacingPx)
                                    }
                                } else if (dragOffset < 0 && cur > 0) {
                                    val prevH = heights[keyOf(order[cur - 1])] ?: 0
                                    if (-dragOffset > (prevH + spacingPx) / 2f) {
                                        order = order.toMutableList().apply { add(cur - 1, removeAt(cur)) }
                                        dragOffset += (prevH + spacingPx)
                                    }
                                }
                            }
                        },
                        onDragEnd = {
                            draggingKey = null
                            dragOffset = 0f
                            onReordered(order)
                        },
                        onDragCancel = {
                            draggingKey = null
                            dragOffset = 0f
                            onReordered(order)
                        },
                    )
                }

                Box(
                    modifier = Modifier
                        .zIndex(if (isDragging) 1f else 0f)
                        .graphicsLayer { translationY = if (isDragging) dragOffset else 0f }
                        .onSizeChanged { heights[key] = it.height },
                ) {
                    itemContent(item, isDragging, handle)
                }
            }
        }
    }
}
