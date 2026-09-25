package com.novafocus.alphabetlauncher

import com.novafocus.alphabetlauncher.data.AppInfo
import com.novafocus.alphabetlauncher.data.AppRepository
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for app name letter normalization and grouping logic.
 */
class AppGroupingTest {

    @Test
    fun testNormalLetterExtraction() {
        assertEquals('G', AppRepository.getNormalizedInitialChar("Gmail"))
        assertEquals('G', AppRepository.getNormalizedInitialChar("gpay"))
        assertEquals('W', AppRepository.getNormalizedInitialChar("WhatsApp"))
        assertEquals('C', AppRepository.getNormalizedInitialChar("Chrome"))
    }

    @Test
    fun testDigitAndSymbolFallbackToHash() {
        assertEquals('#', AppRepository.getNormalizedInitialChar("1Password"))
        assertEquals('#', AppRepository.getNormalizedInitialChar("360 Security"))
        assertEquals('#', AppRepository.getNormalizedInitialChar("@NovaLauncher"))
        assertEquals('#', AppRepository.getNormalizedInitialChar(""))
    }

    @Test
    fun testAppGroupingByLetter() {
        val testApps = listOf(
            AppInfo("com.gmail", "Gmail", null, 'G'),
            AppInfo("com.gpay", "GPay", null, 'G'),
            AppInfo("com.whatsapp", "WhatsApp", null, 'W'),
            AppInfo("com.1pass", "1Password", null, '#')
        )

        val groupedMap = testApps.groupBy { it.initialChar }

        assertEquals(2, groupedMap['G']?.size)
        assertEquals(1, groupedMap['W']?.size)
        assertEquals(1, groupedMap['#']?.size)
        assertEquals(null, groupedMap['Z'])
    }
}
