package com.idsanna.android

/** Visible, process-local browser session. No background or hidden WebView is created. */
object BrowserSession {
    @Volatile private var controller: BrowserWebViewController? = null

    fun attach(controller: BrowserWebViewController) { this.controller = controller }
    fun detach(controller: BrowserWebViewController) { if (this.controller === controller) this.controller = null }

    fun route(request: ToolExecutionRequest, tools: ToolRegistry, policy: PolicyEngine): ToolExecutionResult {
        val active = controller ?: return ToolExecutionResult(request.operationId, ExecutionStatus.FAILED, "browser_session_not_visible")
        return BrowserToolRouter(tools, policy, active).route(request)
    }
}
