package com.idsanna.android

class PolicyEngine(
    private val capabilities: CapabilityRegistry,
    private val tools: ToolRegistry
) {
    fun evaluate(toolName: String, permissions: Set<String> = emptySet()): PolicyDecision {
        val tool = tools.get(toolName) ?: return PolicyDecision(PolicyAction.DENY, "unknown_tool")
        if (!capabilities.isRegistered(tool.capability)) return PolicyDecision(PolicyAction.DENY, "unknown_capability")
        if (!capabilities.canUse(tool.capability, permissions)) return PolicyDecision(PolicyAction.DENY, "capability_unavailable")
        if (tool.requiresConfirmation || tool.risk == Risk.HIGH) return PolicyDecision(PolicyAction.REQUIRE_CONFIRMATION, "sensitive_action")
        return PolicyDecision(PolicyAction.ALLOW, "policy_allowed")
    }
}

enum class PolicyAction { ALLOW, REQUIRE_CONFIRMATION, DENY }
data class PolicyDecision(val action: PolicyAction, val reason: String)

class ApprovalManager {
    private val pending = linkedSetOf<String>()

    fun request(taskId: String, toolName: String): ApprovalRequest {
        val key = "$taskId:$toolName"
        pending.add(key)
        return ApprovalRequest(taskId, toolName, ApprovalState.WAITING)
    }

    fun approve(taskId: String, toolName: String): Boolean = pending.remove("$taskId:$toolName")
    fun reject(taskId: String, toolName: String): Boolean = pending.remove("$taskId:$toolName")
    fun isPending(taskId: String, toolName: String): Boolean = pending.contains("$taskId:$toolName")
}

enum class ApprovalState { WAITING, APPROVED, REJECTED }
data class ApprovalRequest(val taskId: String, val toolName: String, val state: ApprovalState)
