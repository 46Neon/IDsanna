package com.idsanna.android

/**
 * Decides what to do with a persisted operation found after process restart.
 * It never executes an operation; it only returns a safe recovery decision.
 */
class OperationRecovery(private val nowMillis: () -> Long = { System.currentTimeMillis() }) {
    fun decide(record: OperationRecord, resultKnown: Boolean, safeToResume: Boolean): RecoveryDecision {
        if (record.state != OperationState.RUNNING) {
            return RecoveryDecision(RecoveryAction.NO_OP, "operation_already_terminal")
        }

        if (nowMillis() >= record.timeoutAt) {
            return RecoveryDecision(RecoveryAction.MARK_TIMED_OUT, "operation_expired")
        }

        if (resultKnown) {
            return RecoveryDecision(RecoveryAction.MARK_COMPLETED_FROM_EVIDENCE, "result_already_known")
        }

        return if (safeToResume) {
            RecoveryDecision(RecoveryAction.RESUME, "operation_is_resumable")
        } else {
            RecoveryDecision(RecoveryAction.REQUIRE_CONFIRMATION, "operation_may_have_side_effects")
        }
    }
}

enum class RecoveryAction {
    NO_OP,
    MARK_TIMED_OUT,
    MARK_COMPLETED_FROM_EVIDENCE,
    RESUME,
    REQUIRE_CONFIRMATION
}

data class RecoveryDecision(val action: RecoveryAction, val reason: String)
