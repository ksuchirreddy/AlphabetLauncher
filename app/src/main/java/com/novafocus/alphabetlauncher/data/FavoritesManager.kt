package com.novafocus.alphabetlauncher.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages persisted user favorite app choices using SharedPreferences.
 */
class FavoritesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getFavoritePackageNames(): Set<String> {
        val saved = prefs.getStringSet(KEY_FAVORITES, null)
        if (saved != null && saved.isNotEmpty()) {
            return saved
        }
        // Default popular favorite packages fallback
        return DEFAULT_FAVORITES
    }

    fun toggleFavorite(packageName: String): Set<String> {
        val current = getFavoritePackageNames().toMutableSet()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        prefs.edit().putStringSet(KEY_FAVORITES, current).apply()
        return current
    }

    fun addFavorite(packageName: String): Set<String> {
        val current = getFavoritePackageNames().toMutableSet()
        current.add(packageName)
        prefs.edit().putStringSet(KEY_FAVORITES, current).apply()
        return current
    }

    fun removeFavorite(packageName: String): Set<String> {
        val current = getFavoritePackageNames().toMutableSet()
        current.remove(packageName)
        prefs.edit().putStringSet(KEY_FAVORITES, current).apply()
        return current
    }

    companion object {
        private const val PREFS_NAME = "alphabet_launcher_prefs"
        private const val KEY_FAVORITES = "favorite_packages"

        private val DEFAULT_FAVORITES = setOf(
            "com.whatsapp",
            "com.android.chrome",
            "com.google.android.youtube",
            "com.google.android.gm",
            "com.google.android.calculator",
            "com.android.camera",
            "com.openai.chatgpt"
        )
    }
}
