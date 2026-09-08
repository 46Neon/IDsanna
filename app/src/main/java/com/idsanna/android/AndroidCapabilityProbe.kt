package com.idsanna.android

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings

class AndroidCapabilityProbe(private val context: Context) {
    private val packageManager = context.packageManager

    fun probe(packageName: String): AppCapabilityReport {
        val appInfo = runCatching { packageManager.getApplicationInfo(packageName, 0) }.getOrNull()
        if (appInfo == null) return AppCapabilityReport(packageName, AppState.UNKNOWN, false, false, false, listOf("package_not_visible_or_not_installed"))
        val launchable = packageManager.getLaunchIntentForPackage(packageName) != null
        val accessibilityEnabled = isAccessibilityServiceEnabled()
        val label = packageManager.getApplicationLabel(appInfo).toString()
        val issues = mutableListOf<String>()
        if (!launchable) issues.add("no_launch_intent")
        if (!accessibilityEnabled) issues.add("accessibility_service_disabled")
        val state = when {
            !launchable -> AppState.BLOCKED
            !accessibilityEnabled -> AppState.LAUNCHABLE
            else -> AppState.ACCESSIBILITY_CANDIDATE
        }
        return AppCapabilityReport(packageName, state, true, launchable, accessibilityEnabled, issues, label)
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val enabled = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        return enabled.contains(context.packageName)
    }
}

data class AppCapabilityReport(
    val packageName: String,
    val state: AppState,
    val detected: Boolean,
    val launchable: Boolean,
    val accessibilityEnabled: Boolean,
    val issues: List<String>,
    val label: String = ""
)

enum class AppState { UNKNOWN, LAUNCHABLE, ACCESSIBILITY_CANDIDATE, TESTED, PARTIAL, BLOCKED }
