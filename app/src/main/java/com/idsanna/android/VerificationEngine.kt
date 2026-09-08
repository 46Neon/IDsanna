package com.idsanna.android

/**
 * Pure postcondition checks over accessibility observations.
 *
 * Keeping this logic independent of AccessibilityService makes it testable
 * without a physical device. The runtime is responsible for obtaining the
 * observations and deciding how long to wait for the postcondition.
 */
class VerificationEngine {
    fun verify(
        before: AccessibilityObservation?,
        after: AccessibilityObservation?,
        spec: VerificationSpec
    ): VerificationEvidence {
        if (after == null) return VerificationEvidence(false, "missing_post_observation", null)

        return when (spec.type) {
            VerificationType.FOREGROUND_PACKAGE -> {
                val expected = spec.expectedPackage
                    ?: return VerificationEvidence(false, "missing_expected_package", after.observationId)
                val success = after.packageName == expected
                VerificationEvidence(
                    success,
                    if (success) "foreground_package_verified" else "unexpected_foreground_package",
                    after.observationId
                )
            }

            VerificationType.VISIBLE_TEXT -> {
                val expected = spec.expectedText
                    ?: return VerificationEvidence(false, "missing_expected_text", after.observationId)
                val success = containsText(after, expected)
                VerificationEvidence(
                    success,
                    if (success) "visible_text_verified" else "visible_text_not_found",
                    after.observationId
                )
            }

            VerificationType.TEXT_APPEARED -> {
                val expected = spec.expectedText
                    ?: return VerificationEvidence(false, "missing_expected_text", after.observationId)
                val wasVisible = before?.let { containsText(it, expected) } == true
                val isVisible = containsText(after, expected)
                val success = !wasVisible && isVisible
                VerificationEvidence(
                    success,
                    when {
                        success -> "text_appeared_verified"
                        wasVisible -> "text_already_visible_before_action"
                        else -> "text_did_not_appear"
                    },
                    after.observationId
                )
            }

            VerificationType.TEXT_DISAPPEARED -> {
                val expected = spec.expectedText
                    ?: return VerificationEvidence(false, "missing_expected_text", after.observationId)
                val wasVisible = before?.let { containsText(it, expected) } == true
                val isVisible = containsText(after, expected)
                val success = wasVisible && !isVisible
                VerificationEvidence(
                    success,
                    when {
                        success -> "text_disappeared_verified"
                        !wasVisible -> "text_not_visible_before_action"
                        else -> "text_still_visible"
                    },
                    after.observationId
                )
            }

            VerificationType.OBSERVATION_CHANGED -> {
                val success = before == null || before.observationId != after.observationId
                VerificationEvidence(
                    success,
                    if (success) "observation_changed" else "observation_unchanged",
                    after.observationId
                )
            }
        }
    }

    private fun containsText(observation: AccessibilityObservation, expected: String): Boolean =
        observation.nodes.any { it.text == expected || it.contentDescription == expected }
}

enum class VerificationType {
    FOREGROUND_PACKAGE,
    VISIBLE_TEXT,
    TEXT_APPEARED,
    TEXT_DISAPPEARED,
    OBSERVATION_CHANGED
}

data class VerificationSpec(
    val type: VerificationType,
    val expectedText: String? = null,
    val expectedPackage: String? = null
)

data class VerificationEvidence(
    val success: Boolean,
    val reason: String,
    val observationId: String?
)
