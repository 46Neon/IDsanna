package com.idsanna.android

class ToolRouter(
    private val tools: ToolRegistry,
    private val policy: PolicyEngine
) {
    fun route(request: ToolExecutionRequest): ToolExecutionResult {
        if (!tools.isRegistered(request.toolName)) return ToolExecutionResult(request.operationId, ExecutionStatus.DENIED, "unknown_tool")
        val decision = policy.evaluate(request.toolName, request.permissions)
        return when (decision.action) {
            PolicyAction.DENY -> ToolExecutionResult(request.operationId, ExecutionStatus.DENIED, decision.reason)
            PolicyAction.REQUIRE_CONFIRMATION -> ToolExecutionResult(request.operationId, ExecutionStatus.WAITING_CONFIRMATION, decision.reason)
            PolicyAction.ALLOW -> ToolExecutionResult(request.operationId, ExecutionStatus.COMPLETED, "simulated_success")
        }
    }
}

data class ToolExecutionRequest(
    val taskId: String,
    val stepId: String,
    val operationId: String,
    val toolName: String,
    val arguments: Map<String, String> = emptyMap(),
    val permissions: Set<String> = emptySet(),
    val verification: VerificationSpec? = null
)

data class ToolExecutionResult(val operationId: String, val status: ExecutionStatus, val evidence: String)
enum class ExecutionStatus { SIMULATED, COMPLETED, WAITING_CONFIRMATION, DENIED, FAILED, CANCELLED }
