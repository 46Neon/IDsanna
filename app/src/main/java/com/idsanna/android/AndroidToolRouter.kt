package com.idsanna.android

class AndroidToolRouter(private val actions: AccessibilityActionExecutor) {
    fun execute(request: ToolExecutionRequest): AndroidExecutionResult {
        return when (request.toolName) {
            "android.click" -> request.arguments["text"]?.let { result(request, actions.clickText(it)) }
                ?: AndroidExecutionResult(request.operationId, false, "missing_text")
            "android.type_text" -> request.arguments["text"]?.let { result(request, actions.typeText(it)) }
                ?: AndroidExecutionResult(request.operationId, false, "missing_text")
            "android.swipe" -> runCatching {
                val r = actions.swipe(
                    request.arguments.getValue("x1").toFloat(), request.arguments.getValue("y1").toFloat(),
                    request.arguments.getValue("x2").toFloat(), request.arguments.getValue("y2").toFloat()
                )
                result(request, r)
            }.getOrElse { AndroidExecutionResult(request.operationId, false, "invalid_swipe_arguments") }
            "android.global_back" -> result(request, actions.globalBack())
            else -> AndroidExecutionResult(request.operationId, false, "unsupported_android_tool")
        }
    }

    private fun result(request: ToolExecutionRequest, action: ActionResult) = AndroidExecutionResult(request.operationId, action.accepted, action.evidence)
}

data class AndroidExecutionResult(val operationId: String, val accepted: Boolean, val evidence: String)
