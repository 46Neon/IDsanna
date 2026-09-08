package com.idsanna.android

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

class ObservationStore {
    @Volatile private var latestObservation: AccessibilityObservation? = null

    fun publish(observation: AccessibilityObservation) { latestObservation = observation }
    fun latest(): AccessibilityObservation? = latestObservation
}

data class AccessibilityObservation(
    val observationId: String,
    val timestamp: Long,
    val packageName: String,
    val className: String?,
    val nodes: List<ObservedNode>
)

data class ObservedNode(
    val text: String?,
    val contentDescription: String?,
    val className: String?,
    val clickable: Boolean,
    val editable: Boolean,
    val enabled: Boolean,
    val bounds: Rect
)

object AccessibilityTreeReader {
    fun read(root: AccessibilityNodeInfo?, packageName: String?, className: String?, maxNodes: Int = 250): AccessibilityObservation {
        val nodes = mutableListOf<ObservedNode>()
        fun visit(node: AccessibilityNodeInfo) {
            if (nodes.size >= maxNodes) return
            val bounds = Rect().also { node.getBoundsInScreen(it) }
            nodes.add(ObservedNode(node.text?.toString(), node.contentDescription?.toString(), node.className?.toString(), node.isClickable, node.isEditable, node.isEnabled, bounds))
            for (index in 0 until node.childCount) node.getChild(index)?.let { visit(it) }
        }
        root?.let { visit(it) }
        return AccessibilityObservation("obs-${System.currentTimeMillis()}", System.currentTimeMillis(), packageName.orEmpty(), className, nodes)
    }
}
