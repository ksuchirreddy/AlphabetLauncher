package com.novafocus.alphabetlauncher.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Clean large clock and date header component.
 */
@Composable
fun ClockHeader(
    timeString: String,
    dateString: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = timeString.ifEmpty { "12:51" },
            fontSize = 68.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = (-1).sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = dateString.ifEmpty { "Thu 24 Sept" },
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xCCFFFFFF)
        )
    }
}
