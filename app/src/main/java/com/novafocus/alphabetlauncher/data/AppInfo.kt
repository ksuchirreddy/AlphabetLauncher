package com.novafocus.alphabetlauncher.data

import android.graphics.drawable.Drawable

/**
 * Represents an installed launchable application.
 *
 * @param packageName Unique Android package name (e.g. "com.whatsapp")
 * @param appName User-visible app title (e.g. "WhatsApp")
 * @param icon App launcher icon drawable
 * @param initialChar Normalized uppercase initial character ('A'-'Z' or '#')
 * @param isFavorite Whether the app is pinned to the resting home screen favourites list
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val initialChar: Char,
    val isFavorite: Boolean = false
)
