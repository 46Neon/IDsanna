package com.idsanna.android

interface RecoveryStateReader {
    fun pendingRecoveryCheckpoints(): List<Checkpoint>
    fun pendingRecoveryApprovals(): List<ApprovalRequest>
}

/** Exposes durable recovery work to the runtime without executing it. */
class RecoveryRuntimeCoordinator(private val state: RecoveryStateReader) {
    fun snapshot(): RecoveryRuntimeSnapshot = RecoveryRuntimeSnapshot(
        resumeCandidates = state.pendingRecoveryCheckpoints()
            .filter { it.status == "RECOVERY_RESUME_PENDING" },
        waitingApprovals = state.pendingRecoveryApprovals()
    )

    /**
     * Returns data for Planner to reconstruct a safe step. It is not an
     * executable request and must still pass Policy and Router.
     */
    fun resumeCandidates(results: List<RecoveryResult>): List<RecoveryResumeCandidate> =
        results.filter { it.decision.action == RecoveryAction.RESUME }
            .mapNotNull { result ->
                val tool = result.record.toolName ?: return@mapNotNull null
                RecoveryResumeCandidate(
                    operationId = result.record.operationId,
                    taskId = result.record.taskId,
                    stepId = result.record.stepId,
                    toolName = tool,
                    arguments = result.record.arguments
                )
            }
}

data class RecoveryRuntimeSnapshot(
    val resumeCandidates: List<Checkpoint>,
    val waitingApprovals: List<ApprovalRequest>
) {
    val hasPendingWork: Boolean
        get() = resumeCandidates.isNotEmpty() || waitingApprovals.isNotEmpty()
}
