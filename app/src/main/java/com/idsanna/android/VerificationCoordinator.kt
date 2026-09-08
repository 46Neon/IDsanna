package com.idsanna.android

/**
 * Waits for a post-action accessibility observation and evaluates it.
 *
 * The provider is injected so the coordinator can be tested without Android.
 * In production it is backed by ObservationStore.latest().
 */
class VerificationCoordinator(
    private val latestObservation: () -> AccessibilityObservation?,
    private val engine: VerificationEngine = VerificationEngine(),
    private val nowMillis: () -> Long = { System.currentTimeMillis() },
    private val sleepMillis: (Long) -> Unit = { Thread.sleep(it) },
    private val pollIntervalMs: Long = 50L,
    private val maxWaitMs: Long = 1_000L
) {
    fun captureBefore(): AccessibilityObservation? = latestObservation()

    fun verify(
        before: AccessibilityObservation?,
        spec: VerificationSpec
    ): VerificationEvidence {
        val startedAt = nowMillis()
        var after = latestObservation()
        val beforeId = before?.observationId

        while (after == null || after.observationId == beforeId) {
            if (nowMillis() - startedAt >= maxWaitMs.coerceAtLeast(0L)) {
                return VerificationEvidence(false, "post_observation_timeout", after?.observationId)
            }
            sleepMillis(pollIntervalMs.coerceAtLeast(1L))
            after = latestObservation()
        }

        return engine.verify(before, after, spec)
    }
}
