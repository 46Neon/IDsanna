package com.idsanna.android

data class RecoveryResumeCandidate(
    val operationId: String,
    val taskId: String?,
    val stepId: String?,
    val toolName: String,
    val arguments: Map<String, String>
)
