package com.idsanna.android

/** Routes only the typed browser operation; it never exposes raw WebView scripting. */
class BrowserToolRouter(
    private val tools: ToolRegistry,
    private val policy: PolicyEngine,
    private val browser: BrowserWebViewController
) {
    fun route(request: ToolExecutionRequest): ToolExecutionResult {
        if (request.toolName != "browser.navigate") {
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
        val url = request.arguments["url"]
            ?: return ToolExecutionResult(request.operationId, ExecutionStatus.FAILED, "missing_url")
        val result = browser.navigate(url)
        return if (result.accepted) {
            // Navigation was requested, not verified. A later observation must complete it.
            ToolExecutionResult(request.operationId, ExecutionStatus.SIMULATED, result.evidence)
        } else {
            ToolExecutionResult(request.operationId, ExecutionStatus.DENIED, result.evidence)
        }
    }
}
