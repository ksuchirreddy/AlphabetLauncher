package com.novafocus.alphabetlauncher.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * Enlarged circular floating bubble that appears to the left of the finger on the curve.
 */
@Composable
fun LetterBubble(
    letter: Char?,
    touchY: Float?,
    isDragging: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isDragging && letter != null && touchY != null,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        if (letter != null && touchY != null) {
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (-120).dp.roundToPx(),
                            y = (touchY - 32.dp.toPx()).roundToInt()
                        )
                    }
                    .size(64.dp)
                    .shadow(elevation = 12.dp, shape = CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF2A2A2A),
                                Color(0xFF141414)
                            )
                        ),
                        shape = CircleShape
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF00E5FF),
                                Color(0x3300E5FF)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter.toString(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
