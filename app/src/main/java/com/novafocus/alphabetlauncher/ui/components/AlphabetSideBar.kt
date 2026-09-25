package com.novafocus.alphabetlauncher.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * List of items displayed in the vertical right sidebar.
 * Top: Star '★', Middle: 'A'-'Z', Bottom: Dot '•'
 */
val ALPHABET_ITEMS = listOf('★') + ('A'..'Z').toList() + listOf('•')

/**
 * Calculates horizontal displacement for a letter at [letterY] given touch at [touchY].
 * Uses a Cosine-bell falloff function with influence radius [influenceRadiusPx]
 * and maximum horizontal shift [maxDisplacementPx].
 */
fun calculateLetterDisplacement(
    letterY: Float,
    touchY: Float,
    influenceRadiusPx: Float = 360f,
    maxDisplacementPx: Float = 160f
): Float {
    val distance = abs(letterY - touchY)
    if (distance >= influenceRadiusPx) return 0f

    val normalizedDistance = distance / influenceRadiusPx // 0..1
    // Cosine-squared bell curve falloff formula: cos^2( (pi / 2) * u )
    val factor = cos((Math.PI / 2.0) * normalizedDistance).toFloat()
    val bellShape = factor * factor

    return maxDisplacementPx * bellShape
}

/**
 * Maps vertical touch Y coordinate to the corresponding Char in [items].
 */
fun getItemForTouchY(
    touchY: Float,
    totalHeight: Float,
    items: List<Char> = ALPHABET_ITEMS
): Char {
    if (totalHeight <= 0f || items.isEmpty()) return items.first()
    val clampedY = touchY.coerceIn(0f, totalHeight)
    val itemHeight = totalHeight / items.size
    val index = (clampedY / itemHeight).toInt().coerceIn(0, items.lastIndex)
    return items[index]
}

@Composable
fun AlphabetSideBar(
    isDragging: Boolean,
    touchY: Float?,
    appCountByLetter: Map<Char, Int>,
    onDragStart: (touchY: Float, letter: Char) -> Unit,
    onDragMove: (touchY: Float, letter: Char) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    var componentHeight by remember { mutableStateOf(0f) }
    
    // Animatable curve strength multiplier (1.0 during drag, springs to 0.0 on release)
    val curveAnimatable = remember { Animatable(0f) }

    LaunchedEffect(isDragging) {
        if (isDragging) {
            curveAnimatable.snapTo(1f)
        } else {
            // Spring animation on release: slight bounce / overshoot back to straight line
            curveAnimatable.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.55f, // Slightly springy overshoot
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(64.dp)
            .onGloballyPositioned { coordinates ->
                componentHeight = coordinates.size.height.toFloat()
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val letter = getItemForTouchY(offset.y, componentHeight)
                        onDragStart(offset.y, letter)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val letter = getItemForTouchY(change.position.y, componentHeight)
                        onDragMove(change.position.y, letter)
                    },
                    onDragEnd = {
                        onDragEnd()
                    },
                    onDragCancel = {
                        onDragEnd()
                    }
                )
            },
        contentAlignment = Alignment.CenterEnd
    ) {
        if (componentHeight > 0f) {
            val itemHeight = componentHeight / ALPHABET_ITEMS.size

            ALPHABET_ITEMS.forEachIndexed { index, char ->
                val letterY = (index + 0.5f) * itemHeight

                // Base displacement based on distance to touch point
                val rawDisplacement = if (touchY != null) {
                    calculateLetterDisplacement(letterY, touchY)
                } else 0f

                // Apply spring progress multiplier (shifts left by negative X)
                val finalDisplacement = -rawDisplacement * curveAnimatable.value

                // Dimming logic for letters with zero apps
                val count = appCountByLetter[char] ?: 0
                val isSpecial = char == '★' || char == '•'
                val alpha = when {
                    isSpecial -> 1.0f
                    count > 0 -> 1.0f
                    else -> 0.35f // Dimmed empty letter indicator
                }

                Text(
                    text = char.toString(),
                    fontSize = 13.sp,
                    fontWeight = if (count > 0 || isSpecial) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (char == '★') Color(0xFFFFD700) else Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset {
                            IntOffset(
                                x = finalDisplacement.roundToInt(),
                                y = (index * itemHeight + (itemHeight - 20f) / 2f).roundToInt()
                            )
                        }
                        .alpha(alpha)
                )
            }
        }
    }
}
