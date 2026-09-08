package com.idsanna.android

class AndroidToolRouter(
    private val actions: AccessibilityActionExecutor,
    private val operationControl: OperationControl = OperationControl(),
    private val verification: VerificationCoordinator? = null
) {
    fun execute(request: ToolExecutionRequest): AndroidExecutionResult {
        if (!operationControl.start(
                operationId = request.operationId,
                taskId = request.taskId,
                stepId = request.stepId,
                toolName = request.toolName
            )
        ) {
            return AndroidExecutionResult(request.operationId, false, "duplicate_operation")
        }

        if (!operationControl.canExecute(request.operationId)) {
            return AndroidExecutionResult(request.operationId, false, "operation_not_executable")
        }

        val before = request.verification?.let { verification?.captureBefore() }
        val actionResult = dispatch(request)
        if (!actionResult.accepted) {
            operationControl.fail(request.operationId, actionResult.evidence)
            return actionResult
        }

        val spec = request.verification
        if (spec != null) {
            val verifier = verification
            if (verifier == null) {
                operationControl.fail(request.operationId, "verification_unavailable")
                return AndroidExecutionResult(request.operationId, false, "verification_unavailable")
            }
            val evidence = verifier.verify(before, spec)
            if (!evidence.success) {
                operationControl.fail(request.operationId, evidence.reason)
                return AndroidExecutionResult(
                    request.operationId,
                    false,
                    "${actionResult.evidence};${evidence.reason}"
                )
            }
            operationControl.complete(request.operationId, evidence.reason)
            return AndroidExecutionResult(
                request.operationId,
                true,
                "${actionResult.evidence};${evidence.reason};observation=${evidence.observationId}"
            )
        }

        if (operationControl.isExpired(request.operationId)) {
            return AndroidExecutionResult(request.operationId, false, "operation_timed_out")
        }
        operationControl.complete(request.operationId, actionResult.evidence)
        return actionResult
    }

    fun cancel(operationId: String): Boolean = operationControl.cancel(operationId)

    private fun dispatch(request: ToolExecutionRequest): AndroidExecutionResult = when (request.toolName) {
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

    private fun result(request: ToolExecutionRequest, action: ActionResult) =
        AndroidExecutionResult(request.operationId, action.accepted, action.evidence)
}

data class AndroidExecutionResult(val operationId: String, val accepted: Boolean, val evidence: String)
