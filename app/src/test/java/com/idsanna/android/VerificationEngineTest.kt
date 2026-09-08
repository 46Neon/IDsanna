package com.idsanna.android

import android.graphics.Rect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VerificationEngineTest {
    private val engine = VerificationEngine()

    @Test
    fun verifiesForegroundPackage() {
        val after = observation("obs-2", "com.example.target", "Ready")
        val result = engine.verify(
            before = null,
            after = after,
            spec = VerificationSpec(VerificationType.FOREGROUND_PACKAGE, expectedPackage = "com.example.target")
        )

        assertTrue(result.success)
        assertEquals("foreground_package_verified", result.reason)
    }

    @Test
    fun verifiesTextAppearedAfterAction() {
        val before = observation("obs-1", "com.example", "Loading")
        val after = observation("obs-2", "com.example", "Done")
        val result = engine.verify(
            before,
            after,
            VerificationSpec(VerificationType.TEXT_APPEARED, expectedText = "Done")
        )

        assertTrue(result.success)
        assertEquals("text_appeared_verified", result.reason)
    }

    @Test
    fun rejectsTextThatWasAlreadyVisible() {
        val before = observation("obs-1", "com.example", "Done")
        val after = observation("obs-2", "com.example", "Done")
        val result = engine.verify(
            before,
            after,
            VerificationSpec(VerificationType.TEXT_APPEARED, expectedText = "Done")
        )

        assertFalse(result.success)
        assertEquals("text_already_visible_before_action", result.reason)
    }

    @Test
    fun verifiesTextDisappeared() {
        val before = observation("obs-1", "com.example", "Busy")
        val after = observation("obs-2", "com.example", "Done")
        val result = engine.verify(
            before,
            after,
            VerificationSpec(VerificationType.TEXT_DISAPPEARED, expectedText = "Busy")
        )

        assertTrue(result.success)
        assertEquals("text_disappeared_verified", result.reason)
    }

    @Test
    fun rejectsMissingPostObservation() {
        val result = engine.verify(
            before = null,
            after = null,
            spec = VerificationSpec(VerificationType.OBSERVATION_CHANGED)
        )

        assertFalse(result.success)
        assertEquals("missing_post_observation", result.reason)
    }

    private fun observation(id: String, packageName: String, text: String): AccessibilityObservation =
        AccessibilityObservation(
            observationId = id,
            timestamp = 1L,
            packageName = packageName,
            className = "android.view.View",
            nodes = listOf(
                ObservedNode(
                    text = text,
                    contentDescription = null,
                    className = "android.widget.TextView",
                    clickable = false,
                    editable = false,
                    enabled = true,
                    bounds = Rect(0, 0, 10, 10)
                )
            )
        )
}
