package com.novafocus.alphabetlauncher.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.novafocus.alphabetlauncher.data.AppRepository
import com.novafocus.alphabetlauncher.data.FavoritesManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel managing launcher state, touch events, live time/date ticker, and search.
 */
class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val favoritesManager = FavoritesManager(application)
    private val appRepository = AppRepository(application, favoritesManager)

    private val _uiState = MutableStateFlow(LauncherUiState())
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    // Shared flow to emit haptic feedback events to activity
    private val _hapticEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val hapticEvents: SharedFlow<Unit> = _hapticEvents.asSharedFlow()

    private var timeTickerJob: Job? = null

    init {
        loadApps()
        startTimeTicker()
    }

    /**
     * Loads installed apps and initializes favorites and letter counts.
     */
    fun loadApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val apps = appRepository.loadInstalledApps()
            val favorites = appRepository.getFavoriteApps()
            val counts = apps.groupingBy { it.initialChar }.eachCount()

            _uiState.update {
                it.copy(
                    allApps = apps,
                    favoriteApps = favorites,
                    appCountByLetter = counts,
                    isLoading = false
                )
            }
        }
    }

    /**
     * Starts continuous background ticker updating time and date strings every second.
     */
    private fun startTimeTicker() {
        timeTickerJob?.cancel()
        timeTickerJob = viewModelScope.launch {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dateFormat = SimpleDateFormat("EEE d MMM", Locale.getDefault())

            while (true) {
                val now = Date()
                val newTime = timeFormat.format(now)
                val newDate = dateFormat.format(now)

                _uiState.update {
                    if (it.timeString != newTime || it.dateString != newDate) {
                        it.copy(timeString = newTime, dateString = newDate)
                    } else {
                        it
                    }
                }
                delay(1000)
            }
        }
    }

    /**
     * Called when touch starts on the alphabet bar.
     */
    fun onDragStart(touchY: Float, letter: Char) {
        val apps = appRepository.getAppsForLetter(letter)
        val currentLetter = _uiState.value.selectedLetter

        if (currentLetter != letter) {
            _hapticEvents.tryEmit(Unit)
        }

        _uiState.update {
            it.copy(
                isDragging = true,
                touchY = touchY,
                selectedLetter = letter,
                filteredApps = apps
            )
        }
    }

    /**
     * Called as finger moves along the alphabet bar.
     */
    fun onDragMove(touchY: Float, letter: Char) {
        val currentLetter = _uiState.value.selectedLetter
        val letterChanged = currentLetter != letter

        if (letterChanged) {
            _hapticEvents.tryEmit(Unit)
            val apps = appRepository.getAppsForLetter(letter)
            _uiState.update {
                it.copy(
                    touchY = touchY,
                    selectedLetter = letter,
                    filteredApps = apps
                )
            }
        } else {
            _uiState.update { it.copy(touchY = touchY) }
        }
    }

    /**
     * Called when finger is lifted off the alphabet bar.
     */
    fun onDragEnd() {
        _uiState.update {
            it.copy(
                isDragging = false,
                touchY = null,
                selectedLetter = null,
                filteredApps = emptyList()
            )
        }
    }

    /**
     * Updates search query and performs instant app filtering.
     */
    fun onSearchQueryChanged(query: String) {
        val results = appRepository.searchApps(query)
        _uiState.update {
            it.copy(
                searchQuery = query,
                searchResults = results
            )
        }
    }

    /**
     * Opens or closes full-screen search overlay.
     */
    fun setSearchActive(active: Boolean) {
        _uiState.update {
            it.copy(
                isSearchActive = active,
                searchQuery = if (!active) "" else it.searchQuery,
                searchResults = if (!active) emptyList() else it.searchResults
            )
        }
    }

    /**
     * Toggles favourite status of an application.
     */
    fun toggleFavorite(packageName: String) {
        viewModelScope.launch {
            favoritesManager.toggleFavorite(packageName)
            val apps = appRepository.loadInstalledApps()
            val favorites = appRepository.getFavoriteApps()
            _uiState.update {
                it.copy(
                    allApps = apps,
                    favoriteApps = favorites
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timeTickerJob?.cancel()
    }
}
