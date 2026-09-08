package com.idsanna.android

/** Stable boundary between a language model (or rules) and the safe runtime. */
class AgentContract(
    private val parser: InstructionParser = InstructionParser(),
    private val planner: Planner = Planner(ToolRegistry.defaults())
) {
    fun analyze(input: String): AgentResult {
        val parsed = parser.parse(input)
        if (!parsed.valid) {
            return AgentResult(
                status = if (parsed.errors.any { it.startsWith("missing_") }) AgentStatus.NEEDS_CLARIFICATION else AgentStatus.INVALID,
                normalized = parsed.normalized,
                intent = parsed.intent,
                target = parsed.target,
                parameters = parsed.parameters,
                missing = missingParameters(parsed),
                errors = parsed.errors
            )
        }

        val plan = planner.createPlan("agent-preview", parsed)
        if (plan.errors.isNotEmpty() || plan.steps.isEmpty()) {
            return AgentResult(
                status = AgentStatus.INVALID,
                normalized = parsed.normalized,
                intent = parsed.intent,
                target = parsed.target,
                parameters = parsed.parameters,
                errors = plan.errors.ifEmpty { listOf("empty_plan") }
            )
        }

        return AgentResult(
            status = AgentStatus.READY_FOR_APPROVAL,
            normalized = parsed.normalized,
            intent = parsed.intent,
            target = parsed.target,
            parameters = parsed.parameters,
            plan = plan.steps.map { AgentPlanStep(it.id, it.tool, it.dependsOn) },
            requiresConfirmation = true
        )
    }

    private fun missingParameters(parsed: ParsedInstruction): List<String> = when {
        "missing_dimensions" in parsed.errors -> listOf("dimensions")
        parsed.intent == "open" && parsed.target == "android" -> listOf("package")
        parsed.intent == "execute" && parsed.target == "termux" -> listOf("registered_task")
        else -> emptyList()
    }
}

enum class AgentStatus {
    INVALID,
    NEEDS_CLARIFICATION,
    READY_FOR_APPROVAL,
    WAITING_APPROVAL,
    EXECUTING,
    VERIFYING,
    COMPLETED,
    FAILED,
    CANCELLED
}

data class AgentPlanStep(
    val id: String,
    val tool: String,
    val dependsOn: Set<String> = emptySet()
)

data class AgentResult(
    val status: AgentStatus,
    val normalized: String,
    val intent: String,
    val target: String,
    val parameters: Map<String, String> = emptyMap(),
    val missing: List<String> = emptyList(),
    val errors: List<String> = emptyList(),
    val plan: List<AgentPlanStep> = emptyList(),
    val requiresConfirmation: Boolean = false
)
