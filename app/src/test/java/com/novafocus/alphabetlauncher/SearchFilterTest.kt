package com.novafocus.alphabetlauncher

import com.novafocus.alphabetlauncher.data.AppInfo
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

/**
 * Unit tests for app search filtering logic.
 */
class SearchFilterTest {

    @Test
    fun testSearchFiltering() {
        val testApps = listOf(
            AppInfo("com.whatsapp", "WhatsApp", null, 'W'),
            AppInfo("com.google.android.youtube", "YouTube", null, 'Y'),
            AppInfo("com.google.android.gm", "Gmail", null, 'G'),
            AppInfo("com.openai.chatgpt", "ChatGPT", null, 'C')
        )

        fun search(query: String): List<AppInfo> {
            val q = query.lowercase(Locale.getDefault())
            return testApps.filter {
                it.appName.lowercase(Locale.getDefault()).contains(q) ||
                        it.packageName.lowercase(Locale.getDefault()).contains(q)
            }
        }

        val chatResults = search("chat")
        assertEquals(1, chatResults.size)
        assertEquals("ChatGPT", chatResults.first().appName)

        val tubeResults = search("tube")
        assertEquals(1, tubeResults.size)
        assertEquals("YouTube", tubeResults.first().appName)

        val emptyResults = search("nonexistentapp123")
        assertEquals(0, emptyResults.size)
    }
}
