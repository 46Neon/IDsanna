package com.idsanna.android

/** Durable boundary between recovery decisions and the normal runtime UI/state. */
interface RecoveryStatePersistence {
    fun saveCheckpoint(checkpoint: Checkpoint)
    fun saveApproval(request: ApprovalRequest)
}

class RecoveryStateIntegrator(private val state: RecoveryStatePersistence) {
    fun apply(results: List<RecoveryResult>) {
        results.forEach { result ->
            val taskId = result.record.taskId ?: "operation:${result.record.operationId}"
            val stepId = result.record.stepId
            when (result.decision.action) {
                RecoveryAction.MARK_TIMED_OUT -> state.saveCheckpoint(
                    Checkpoint(taskId, stepId, "RECOVERY_TIMED_OUT")
                )

                RecoveryAction.MARK_COMPLETED_FROM_EVIDENCE -> state.saveCheckpoint(
                    Checkpoint(taskId, stepId, "RECOVERY_COMPLETED_FROM_EVIDENCE")
                )

                RecoveryAction.RESUME -> state.saveCheckpoint(
                    Checkpoint(taskId, stepId, "RECOVERY_RESUME_PENDING")
                )

                RecoveryAction.REQUIRE_CONFIRMATION -> {
                    val toolName = result.record.toolName ?: "unknown_tool"
                    state.saveApproval(ApprovalRequest(taskId, toolName, ApprovalState.WAITING))
                    state.saveCheckpoint(Checkpoint(taskId, stepId, "RECOVERY_WAITING_APPROVAL"))
                }

                RecoveryAction.NO_OP -> Unit
            }
        }
    }
}
