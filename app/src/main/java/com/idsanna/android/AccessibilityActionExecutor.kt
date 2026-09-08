package com.idsanna.android

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo

class AccessibilityActionExecutor(private val service: AccessibilityService) {
    fun clickText(text: String): ActionResult {
        val node = findNode { it.text?.toString() == text && it.isClickable }
            ?: return ActionResult(false, "click_target_not_found")
        return ActionResult(node.performAction(AccessibilityNodeInfo.ACTION_CLICK), "click_requested")
    }

    fun clickContentDescription(description: String): ActionResult {
        val node = findNode { it.contentDescription?.toString() == description && it.isClickable }
            ?: return ActionResult(false, "content_description_target_not_found")
        return ActionResult(node.performAction(AccessibilityNodeInfo.ACTION_CLICK), "content_description_click_requested")
    }

    fun focusField(): ActionResult {
        val node = findNode { it.isEditable && it.isEnabled }
            ?: return ActionResult(false, "editable_target_not_found")
        return ActionResult(node.performAction(AccessibilityNodeInfo.ACTION_FOCUS), "field_focus_requested")
    }

    fun scrollToText(text: String): ActionResult {
        val node = findNode { it.text?.toString() == text }
            ?: return ActionResult(false, "scroll_target_not_found")
        val accepted = node.performAction(AccessibilityNodeInfo.ACTION_SHOW_ON_SCREEN)
        return ActionResult(accepted, if (accepted) "scroll_to_text_requested" else "scroll_to_text_rejected")
    }

    fun typeText(text: String): ActionResult {
        val node = findNode { it.isEditable && it.isFocused }
            ?: findNode { it.isEditable }
            ?: return ActionResult(false, "editable_target_not_found")
        val args = Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text) }
        return ActionResult(node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args), "text_requested")
    }

    fun swipe(x1: Float, y1: Float, x2: Float, y2: Float, durationMs: Long = 350L): ActionResult {
        val duration = durationMs.coerceIn(100L, 1200L)
        val path = Path().apply { moveTo(x1, y1); lineTo(x2, y2) }
        val gesture = GestureDescription.Builder().addStroke(GestureDescription.StrokeDescription(path, 0L, duration)).build()
        val accepted = service.dispatchGesture(gesture, null, null)
        return ActionResult(accepted, if (accepted) "gesture_dispatched" else "gesture_rejected")
    }

    fun globalBack(): ActionResult = ActionResult(service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK), "global_back_requested")

    private fun findNode(predicate: (AccessibilityNodeInfo) -> Boolean): AccessibilityNodeInfo? {
        fun visit(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
            if (node == null) return null
            if (predicate(node)) return node
            for (index in 0 until node.childCount) visit(node.getChild(index))?.let { return it }
            return null
        }
        return visit(service.rootInActiveWindow)
    }
}

data class ActionResult(val accepted: Boolean, val evidence: String)
