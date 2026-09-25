package com.novafocus.alphabetlauncher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.novafocus.alphabetlauncher.MainActivity

/**
 * BroadcastReceiver triggered when an app package is added, removed, or changed.
 */
class PackageChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if (action == Intent.ACTION_PACKAGE_ADDED ||
            action == Intent.ACTION_PACKAGE_REMOVED ||
            action == Intent.ACTION_PACKAGE_CHANGED
        ) {
            MainActivity.currentViewModel?.loadApps()
        }
    }
}
