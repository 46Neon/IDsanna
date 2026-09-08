package com.idsanna.android

class AndroidToolRouter(
    private val actions: AccessibilityActionExecutor,
    private val operationControl: OperationControl = OperationControl(),
    private val operationStore: OperationStore? = null
) {
    fun execute(request: ToolExecutionRequest): AndroidExecutionResult {
        if (!operationControl.start(request.operationId, request.taskId, request.stepId, request.toolName)) return AndroidExecutionResult(request.operationId, false, "duplicate_operation")
        persist(request.operationId)
        if (!operationControl.canExecute(request.operationId)) return finish(request.operationId, false, "operation_not_executable")
        val result = when (request.toolName) {
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
        return finish(request.operationId, result.accepted, result.evidence)
    }

    fun cancel(operationId: String): Boolean {
        val changed = operationControl.cancel(operationId)
        if (changed) persist(operationId)
        return changed
    }

    private fun finish(operationId: String, accepted: Boolean, evidence: String): AndroidExecutionResult {
        if (accepted) operationControl.complete(operationId, evidence) else operationControl.fail(operationId, evidence)
        persist(operationId)
        return AndroidExecutionResult(operationId, accepted, evidence)
    }

    private fun persist(operationId: String) { operationControl.record(operationId)?.let { operationStore?.save(it) } }
    private fun result(request: ToolExecutionRequest, action: ActionResult) = AndroidExecutionResult(request.operationId, action.accepted, action.evidence)
}

data class AndroidExecutionResult(val operationId: String, val accepted: Boolean, val evidence: String)
