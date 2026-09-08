package com.idsanna.android

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class IdsannaAccessibilityService : AccessibilityService() {
    companion object { val observations = ObservationStore() }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val root = rootInActiveWindow
        val observation = AccessibilityTreeReader.read(root, event.packageName?.toString(), event.className?.toString())
        observations.publish(observation)
    }

    override fun onInterrupt() {}
}
