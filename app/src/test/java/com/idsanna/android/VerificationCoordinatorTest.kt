package com.idsanna.android

import android.graphics.Rect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VerificationCoordinatorTest {
    @Test
    fun waitsForNewObservationBeforeVerifying() {
        val before = observation("obs-1", "com.example", "Loading")
        val after = observation("obs-2", "com.example", "Done")
        var current: AccessibilityObservation? = before
        var now = 0L

        val coordinator = VerificationCoordinator(
            latestObservation = {
                if (now >= 10L) after else current
            },
            nowMillis = { now },
            sleepMillis = { now += it },
            pollIntervalMs = 10L,
            maxWaitMs = 100L
        )

        val result = coordinator.verify(
            before,
            VerificationSpec(VerificationType.TEXT_APPEARED, expectedText = "Done")
        )

        assertTrue(result.success)
        assertEquals("text_appeared_verified", result.reason)
        assertEquals("obs-2", result.observationId)
    }

    @Test
    fun timesOutWhenNoPostObservationArrives() {
        val before = observation("obs-1", "com.example", "Loading")
        var now = 0L
        val coordinator = VerificationCoordinator(
            latestObservation = { before },
            nowMillis = { now },
            sleepMillis = { now += it },
            pollIntervalMs = 10L,
            maxWaitMs = 30L
        )

        val result = coordinator.verify(
            before,
            VerificationSpec(VerificationType.OBSERVATION_CHANGED)
        )

        assertFalse(result.success)
        assertEquals("post_observation_timeout", result.reason)
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
