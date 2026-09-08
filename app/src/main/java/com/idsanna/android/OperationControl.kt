package com.idsanna.android

import java.util.concurrent.ConcurrentHashMap

/** Clock injectable in tests so timeout behaviour is deterministic. */
fun interface OperationClock {
    fun nowMillis(): Long
}

object SystemOperationClock : OperationClock {
    override fun nowMillis(): Long = System.currentTimeMillis()
}

data class OperationRecord(
    val operationId: String,
    val taskId: String? = null,
    val stepId: String? = null,
    val toolName: String? = null,
    val startedAt: Long,
    val timeoutAt: Long,
    val state: OperationState = OperationState.RUNNING,
    val evidence: String? = null
)

enum class OperationState {
    RUNNING,
    CANCELLED,
    COMPLETED,
    FAILED,
    TIMED_OUT
}

class OperationControl(
    private val clock: OperationClock = SystemOperationClock,
    private val defaultTimeoutMs: Long = DEFAULT_TIMEOUT_MS,
    private val persistence: OperationPersistence? = null
) {
    private val operations = ConcurrentHashMap<String, OperationRecord>()

    /** Backward-compatible overload for the original API. */
    fun start(operationId: String, timeoutMs: Long): Boolean =
        start(operationId = operationId, timeoutMs = timeoutMs)

    fun start(
        operationId: String,
        taskId: String? = null,
        stepId: String? = null,
        toolName: String? = null,
        timeoutMs: Long = defaultTimeoutMs
    ): Boolean {
        require(operationId.isNotBlank()) { "operationId must not be blank" }
        if (operations.containsKey(operationId)) return false
        val persisted = persistence?.get(operationId)
        if (persisted != null) return false

        val now = clock.nowMillis()
        val boundedTimeout = timeoutMs.coerceIn(MIN_TIMEOUT_MS, MAX_TIMEOUT_MS)
        val record = OperationRecord(
            operationId = operationId,
            taskId = taskId,
            stepId = stepId,
            toolName = toolName,
            startedAt = now,
            timeoutAt = now + boundedTimeout
        )
        if (operations.putIfAbsent(operationId, record) != null) return false
        persistence?.save(record)
        return true
    }

    fun canExecute(operationId: String): Boolean {
        val record = operations[operationId] ?: return false
        if (record.state != OperationState.RUNNING) return false
        if (isExpired(operationId)) return false
        return operations[operationId]?.state == OperationState.RUNNING
    }

    fun cancel(operationId: String): Boolean = transition(operationId, OperationState.CANCELLED, "cancelled")

    fun complete(operationId: String, evidence: String? = null): Boolean =
        transition(operationId, OperationState.COMPLETED, evidence)

    fun fail(operationId: String, evidence: String? = null): Boolean =
        transition(operationId, OperationState.FAILED, evidence)

    fun isExpired(operationId: String): Boolean {
        val record = operations[operationId] ?: return false
        if (record.state != OperationState.RUNNING) return false
        if (clock.nowMillis() < record.timeoutAt) return false
        operations.computeIfPresent(operationId) { _, current ->
            if (current.state == OperationState.RUNNING) {
                current.copy(state = OperationState.TIMED_OUT, evidence = "timeout")
            } else current
        }
        persistCurrent(operationId)
        return operations[operationId]?.state == OperationState.TIMED_OUT
    }

    fun state(operationId: String): OperationState? = operations[operationId]?.state

    fun record(operationId: String): OperationRecord? =
        operations[operationId] ?: persistence?.get(operationId)

    private fun transition(operationId: String, next: OperationState, evidence: String?): Boolean {
        val updated = operations.computeIfPresent(operationId) { _, current ->
            if (current.state == OperationState.RUNNING) current.copy(state = next, evidence = evidence)
            else current
        }
        if (updated != null) persistence?.save(updated)
        return updated?.state == next
    }

    private fun persistCurrent(operationId: String) {
        operations[operationId]?.let { persistence?.save(it) }
    }

    companion object {
        const val DEFAULT_TIMEOUT_MS = 30_000L
        const val MIN_TIMEOUT_MS = 100L
        const val MAX_TIMEOUT_MS = 300_000L
    }
}
