package com.idsanna.android

/**
 * Performs the non-destructive startup pass over persisted operations.
 *
 * It never executes Android actions. RESUME is returned to the caller, which
 * must hand the step back to the normal Planner/Policy/Router path.
 */
class RecoveryCoordinator(
    private val persistence: OperationPersistence,
    private val recovery: OperationRecovery = OperationRecovery(),
    private val resultKnown: (OperationRecord) -> Boolean = { it.evidence != null },
    private val safeToResume: (OperationRecord) -> Boolean = { record ->
        record.toolName in SAFE_RESUMABLE_TOOLS
    }
) {
    fun recover(): List<RecoveryResult> = persistence.list(OperationState.RUNNING).map { record ->
        val decision = recovery.decide(
            record = record,
            resultKnown = resultKnown(record),
            safeToResume = safeToResume(record)
        )

        when (decision.action) {
            RecoveryAction.MARK_TIMED_OUT ->
                record.copy(state = OperationState.TIMED_OUT, evidence = decision.reason)
                    .also(persistence::save)

            RecoveryAction.MARK_COMPLETED_FROM_EVIDENCE ->
                record.copy(state = OperationState.COMPLETED, evidence = decision.reason)
                    .also(persistence::save)

            else -> record
        }.let { updated -> RecoveryResult(updated, decision) }
    }

    companion object {
        /** Explicit allow-list; unknown and side-effecting tools are not replayed. */
        val SAFE_RESUMABLE_TOOLS: Set<String> = setOf(
            "android.capture_screen",
            "network.get_local_ip"
        )
    }
}

data class RecoveryResult(
    val record: OperationRecord,
    val decision: RecoveryDecision
)
