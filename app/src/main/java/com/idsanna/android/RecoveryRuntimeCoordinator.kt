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
}

data class RecoveryRuntimeSnapshot(
    val resumeCandidates: List<Checkpoint>,
    val waitingApprovals: List<ApprovalRequest>
) {
    val hasPendingWork: Boolean
        get() = resumeCandidates.isNotEmpty() || waitingApprovals.isNotEmpty()
}
