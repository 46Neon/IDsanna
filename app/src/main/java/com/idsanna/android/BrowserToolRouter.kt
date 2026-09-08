package com.idsanna.android

/** Routes typed browser operations; raw WebView scripting is never exposed. */
class BrowserToolRouter(
    private val tools: ToolRegistry,
    private val policy: PolicyEngine,
    private val browser: BrowserWebViewController
) {
    fun route(request: ToolExecutionRequest): ToolExecutionResult {
        if (request.toolName !in setOf("browser.navigate", "browser.observe")) {
            return ToolExecutionResult(request.operationId, ExecutionStatus.DENIED, "unsupported_browser_tool")
        }
        if (!tools.isRegistered(request.toolName)) {
            return ToolExecutionResult(request.operationId, ExecutionStatus.DENIED, "unknown_tool")
        }
        val decision = policy.evaluate(request.toolName, request.permissions)
        if (decision.action == PolicyAction.DENY) {
            return ToolExecutionResult(request.operationId, ExecutionStatus.DENIED, decision.reason)
        }
        if (decision.action == PolicyAction.REQUIRE_CONFIRMATION) {
            return ToolExecutionResult(request.operationId, ExecutionStatus.WAITING_CONFIRMATION, decision.reason)
        }

        return when (request.toolName) {
            "browser.navigate" -> {
                val url = request.arguments["url"]
                    ?: return ToolExecutionResult(request.operationId, ExecutionStatus.FAILED, "missing_url")
                val result = browser.navigate(url)
                if (result.accepted) ToolExecutionResult(request.operationId, ExecutionStatus.SIMULATED, result.evidence)
                else ToolExecutionResult(request.operationId, ExecutionStatus.DENIED, result.evidence)
            }
            else -> {
                val result = browser.observe()
                if (result.ready) {
                    ToolExecutionResult(request.operationId, ExecutionStatus.COMPLETED, "observation_ready:epoch=${result.observation?.pageEpoch}")
                } else {
                    ToolExecutionResult(request.operationId, ExecutionStatus.FAILED, result.evidence)
                }
            }
        }
    }
}
