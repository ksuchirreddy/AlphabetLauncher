package com.novafocus.alphabetlauncher

import com.novafocus.alphabetlauncher.ui.components.ALPHABET_ITEMS
import com.novafocus.alphabetlauncher.ui.components.calculateLetterDisplacement
import com.novafocus.alphabetlauncher.ui.components.getItemForTouchY
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the mathematical curve displacement calculation and touch mapping logic.
 */
class AlphabetCurveMathTest {

    @Test
    fun testMaxDisplacementAtTouchPoint() {
        val touchY = 400f
        val letterY = 400f
        val displacement = calculateLetterDisplacement(
            letterY = letterY,
            touchY = touchY,
            influenceRadiusPx = 300f,
            maxDisplacementPx = 150f
        )
        // At exact touch point (distance = 0), displacement should equal maxDisplacement
        assertEquals(150f, displacement, 0.001f)
    }

    @Test
    fun testZeroDisplacementOutsideInfluenceRadius() {
        val touchY = 100f
        val letterY = 500f // distance 400f > influenceRadius 300f
        val displacement = calculateLetterDisplacement(
            letterY = letterY,
            touchY = touchY,
            influenceRadiusPx = 300f,
            maxDisplacementPx = 150f
        )
        // Outside radius, displacement must be strictly 0
        assertEquals(0f, displacement, 0.001f)
    }

    @Test
    fun testSymmetricDisplacement() {
        val touchY = 300f
        val radius = 200f
        val maxShift = 100f

        val displacementAbove = calculateLetterDisplacement(
            letterY = touchY - 50f,
            touchY = touchY,
            influenceRadiusPx = radius,
            maxDisplacementPx = maxShift
        )
        val displacementBelow = calculateLetterDisplacement(
            letterY = touchY + 50f,
            touchY = touchY,
            influenceRadiusPx = radius,
            maxDisplacementPx = maxShift
        )

        // Curve must be perfectly symmetric above and below the touch point
        assertEquals(displacementAbove, displacementBelow, 0.001f)
        assertTrue("Displacement must be positive", displacementAbove > 0f)
        assertTrue("Displacement must be less than maxShift", displacementAbove < maxShift)
    }

    @Test
    fun testTouchYToItemMapping() {
        val totalHeight = 1000f
        val firstItem = getItemForTouchY(touchY = 0f, totalHeight = totalHeight)
        val lastItem = getItemForTouchY(touchY = 1000f, totalHeight = totalHeight)
        val starItem = ALPHABET_ITEMS.first()
        val dotItem = ALPHABET_ITEMS.last()

        assertEquals(starItem, firstItem)
        assertEquals(dotItem, lastItem)
    }
}
