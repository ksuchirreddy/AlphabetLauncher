package com.novafocus.alphabetlauncher.ui

import com.novafocus.alphabetlauncher.data.AppInfo

/**
 * Immutable state representing the home screen launcher UI.
 */
data class LauncherUiState(
    val isDragging: Boolean = false,
    val touchY: Float? = null,
    val selectedLetter: Char? = null,
    val filteredApps: List<AppInfo> = emptyList(),
    val favoriteApps: List<AppInfo> = emptyList(),
    val allApps: List<AppInfo> = emptyList(),
    val appCountByLetter: Map<Char, Int> = emptyMap(),
    val timeString: String = "",
    val dateString: String = "",
    val isSearchActive: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<AppInfo> = emptyList(),
    val isLoading: Boolean = true
)
