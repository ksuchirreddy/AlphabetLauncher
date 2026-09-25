package com.novafocus.alphabetlauncher

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.novafocus.alphabetlauncher.data.AppInfo
import com.novafocus.alphabetlauncher.ui.LauncherViewModel
import com.novafocus.alphabetlauncher.ui.components.AlphabetSideBar
import com.novafocus.alphabetlauncher.ui.components.ClockHeader
import com.novafocus.alphabetlauncher.ui.components.FavoritesList
import com.novafocus.alphabetlauncher.ui.components.FilteredAppList
import com.novafocus.alphabetlauncher.ui.components.LetterBubble
import com.novafocus.alphabetlauncher.ui.components.SearchOverlay
import com.novafocus.alphabetlauncher.ui.theme.AlphabetLauncherTheme
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentViewModel = viewModel

        // Enable edge-to-edge layout
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AlphabetLauncherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    LauncherScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (currentViewModel == viewModel) {
            currentViewModel = null
        }
    }

    companion object {
        var currentViewModel: LauncherViewModel? = null
    }
}

@Composable
fun LauncherScreen(viewModel: LauncherViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Trigger haptic vibration tick whenever letter changes
    LaunchedEffect(Unit) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        viewModel.hapticEvents.collectLatest {
            if (vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(12)
                }
            }
        }
    }

    val launchApp: (AppInfo) -> Unit = { app ->
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
            if (intent != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Cannot open ${app.appName}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to launch ${app.appName}", Toast.LENGTH_SHORT).show()
        }
    }

    val toggleFavorite: (AppInfo) -> Unit = { app ->
        viewModel.toggleFavorite(app.packageName)
        val msg = if (app.isFavorite) "Removed from favorites" else "Added to favorites"
        Toast.makeText(context, "${app.appName}: $msg", Toast.LENGTH_SHORT).show()
    }

    BackHandler(enabled = uiState.isSearchActive || uiState.isDragging) {
        if (uiState.isSearchActive) {
            viewModel.setSearchActive(false)
        } else if (uiState.isDragging) {
            viewModel.onDragEnd()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    // Swipe up on home screen opens search
                    if (dragAmount < -25f && !uiState.isDragging && !uiState.isSearchActive) {
                        viewModel.setSearchActive(true)
                    }
                }
            }
    ) {
        // Main Left Content: Animated transition between Home View (Clock+Favorites) and Filtered App List
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(start = 28.dp, top = 24.dp, end = 72.dp, bottom = 24.dp)
        ) {
            AnimatedContent(
                targetState = uiState.isDragging,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "MainContentTransition"
            ) { dragging ->
                if (dragging) {
                    FilteredAppList(
                        letter = uiState.selectedLetter,
                        apps = uiState.filteredApps,
                        onAppClick = launchApp,
                        onAppLongClick = toggleFavorite
                    )
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        ClockHeader(
                            timeString = uiState.timeString,
                            dateString = uiState.dateString
                        )
                        Spacer(modifier = Modifier.height(36.dp))
                        FavoritesList(
                            favorites = uiState.favoriteApps,
                            onAppClick = launchApp,
                            onAppLongClick = toggleFavorite
                        )
                    }
                }
            }
        }

        // Floating Letter Bubble next to finger touch position on curve
        LetterBubble(
            letter = uiState.selectedLetter,
            touchY = uiState.touchY,
            isDragging = uiState.isDragging
        )

        // Vertical A-Z SideBar on right edge
        AlphabetSideBar(
            isDragging = uiState.isDragging,
            touchY = uiState.touchY,
            appCountByLetter = uiState.appCountByLetter,
            onDragStart = { touchY, letter ->
                viewModel.onDragStart(touchY, letter)
            },
            onDragMove = { touchY, letter ->
                viewModel.onDragMove(touchY, letter)
            },
            onDragEnd = {
                viewModel.onDragEnd()
            },
            modifier = Modifier.align(Alignment.CenterEnd)
        )

        // Full Screen Search Overlay (opened via swipe up)
        SearchOverlay(
            isSearchActive = uiState.isSearchActive,
            searchQuery = uiState.searchQuery,
            searchResults = uiState.searchResults,
            onQueryChange = { viewModel.onSearchQueryChanged(it) },
            onCloseSearch = { viewModel.setSearchActive(false) },
            onAppClick = { app ->
                viewModel.setSearchActive(false)
                launchApp(app)
            }
        )
    }
}
