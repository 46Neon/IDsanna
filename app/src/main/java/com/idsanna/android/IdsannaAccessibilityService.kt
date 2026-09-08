package com.idsanna.android

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class IdsannaAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) { /* Observación controlada: las acciones se implementarán con aprobación y verificación. */ }
    override fun onInterrupt() {}
}
