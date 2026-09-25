package com.novafocus.alphabetlauncher.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Repository responsible for querying PackageManager for installed apps,
 * caching them by initial letter, and providing favorites list.
 */
class AppRepository(
    private val context: Context,
    private val favoritesManager: FavoritesManager = FavoritesManager(context)
) {
    private var cachedAppsByLetter: Map<Char, List<AppInfo>> = emptyMap()
    private var cachedAllAppsList: List<AppInfo> = emptyList()

    /**
     * Loads launchable apps from PackageManager on Dispatchers.IO.
     * Caches result in memory to guarantee 60fps touch performance.
     */
    suspend fun loadInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = pm.queryIntentActivities(intent, 0)
        val selfPackageName = context.packageName
        val favoritePackages = favoritesManager.getFavoritePackageNames()

        val apps = resolveInfos
            .filter { it.activityInfo.packageName != selfPackageName }
            .map { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                val label = resolveInfo.loadLabel(pm)?.toString()?.trim() ?: packageName
                val icon = try {
                    resolveInfo.loadIcon(pm)
                } catch (e: Exception) {
                    pm.defaultActivityIcon
                }
                val initialChar = getNormalizedInitialChar(label)
                val isFav = favoritePackages.contains(packageName)

                AppInfo(
                    packageName = packageName,
                    appName = label,
                    icon = icon,
                    initialChar = initialChar,
                    isFavorite = isFav
                )
            }
            .sortedBy { it.appName.lowercase(Locale.getDefault()) }

        cachedAllAppsList = apps
        cachedAppsByLetter = apps.groupBy { it.initialChar }

        return@withContext apps
    }

    /**
     * Returns cached map of apps grouped by letter ('A'-'Z' or '#').
     */
    fun getAppsByLetterMap(): Map<Char, List<AppInfo>> = cachedAppsByLetter

    /**
     * Gets installed apps starting with the target letter (case-insensitive).
     */
    fun getAppsForLetter(letter: Char): List<AppInfo> {
        val targetChar = letter.uppercaseChar()
        return cachedAppsByLetter[targetChar] ?: emptyList()
    }

    /**
     * Gets current list of favorite apps (up to 7 apps).
     * If user favorites are empty or match fewer than 5 apps, populates with first available installed apps.
     */
    fun getFavoriteApps(): List<AppInfo> {
        val favoritePackages = favoritesManager.getFavoritePackageNames()
        val favorites = cachedAllAppsList.filter { favoritePackages.contains(it.packageName) }

        if (favorites.size >= 5) {
            return favorites.take(7)
        }

        // Fallback: take first 6-7 installed apps if favorites match few apps
        val combined = (favorites + cachedAllAppsList).distinctBy { it.packageName }
        return combined.take(7)
    }

    /**
     * Performs instant fuzzy search over installed apps.
     */
    fun searchApps(query: String): List<AppInfo> {
        val q = query.trim().lowercase(Locale.getDefault())
        if (q.isEmpty()) return emptyList()
        return cachedAllAppsList.filter {
            it.appName.lowercase(Locale.getDefault()).contains(q) ||
                    it.packageName.lowercase(Locale.getDefault()).contains(q)
        }
    }

    /**
     * Normalizes an app name to its uppercase starting character ('A'-'Z'), or '#' for numbers/symbols.
     */
    companion object {
        fun getNormalizedInitialChar(appName: String): Char {
            if (appName.isEmpty()) return '#'
            val first = appName.trim().first().uppercaseChar()
            return if (first in 'A'..'Z') first else '#'
        }
    }
}
