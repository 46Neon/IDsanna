package com.idsanna.android

import java.util.concurrent.ConcurrentHashMap

class OperationControl {
    private val operations = ConcurrentHashMap<String, OperationState>()

    fun start(operationId: String, timeoutMs: Long = 30_000L): Boolean {
        val state = operations.putIfAbsent(operationId, OperationState.RUNNING)
        if (state != null) return false
        operations[operationId] = OperationState.RUNNING
        return true
    }

    fun cancel(operationId: String): Boolean = operations.replace(operationId, OperationState.RUNNING, OperationState.CANCELLED)
    fun complete(operationId: String): Boolean = operations.replace(operationId, OperationState.RUNNING, OperationState.COMPLETED)
    fun fail(operationId: String): Boolean = operations.replace(operationId, OperationState.RUNNING, OperationState.FAILED)
    fun state(operationId: String): OperationState? = operations[operationId]
    fun isTimedOut(startedAt: Long, timeoutMs: Long, now: Long = System.currentTimeMillis()): Boolean = now - startedAt >= timeoutMs.coerceIn(100L, 300_000L)
}

enum class OperationState { RUNNING, CANCELLED, COMPLETED, FAILED, TIMED_OUT }
