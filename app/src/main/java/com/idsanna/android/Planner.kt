package com.idsanna.android

class Planner(private val tools: ToolRegistry) {
    fun createPlan(taskId: String, instruction: ParsedInstruction): ExecutionPlan {
        if (!instruction.valid) return ExecutionPlan(taskId, emptyList(), instruction.errors)
        val toolName = when (instruction.intent to instruction.target) {
            "open" to "android" -> "android.open_app"
            "inspect" to "network" -> "network.get_local_ip"
            "create" to "autocad" -> "cad.create_geometry"
            "execute" to "termux" -> "termux.run_registered_task"
            else -> null
        }
        if (toolName == null || !tools.isRegistered(toolName)) return ExecutionPlan(taskId, emptyList(), listOf("no_tool_mapping"))
        return ExecutionPlan(taskId, listOf(PlanStep("step-1", toolName, StepStatus.QUEUED, emptySet())), emptyList())
    }
}

data class ExecutionPlan(val taskId: String, val steps: List<PlanStep>, val errors: List<String>)
data class PlanStep(val id: String, val tool: String, val status: StepStatus, val dependsOn: Set<String>)
enum class StepStatus { QUEUED, RUNNING, WAITING_CONFIRMATION, COMPLETED, FAILED, CANCELLED }

data class Checkpoint(val taskId: String, val nextStepId: String?, val status: String)

class CheckpointStore {
    private val checkpoints = mutableMapOf<String, Checkpoint>()
    fun save(checkpoint: Checkpoint) { checkpoints[checkpoint.taskId] = checkpoint }
    fun get(taskId: String): Checkpoint? = checkpoints[taskId]
    fun remove(taskId: String) { checkpoints.remove(taskId) }
}
