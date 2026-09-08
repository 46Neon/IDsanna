package com.idsanna.android

import android.content.Context

class TaskStore(context: Context) {
    private val prefs = context.getSharedPreferences("idsanna_tasks", Context.MODE_PRIVATE)

    fun enqueue(instruction: String): String {
        val id = "task-${System.currentTimeMillis()}"
        prefs.edit()
            .putString("latest_id", id)
            .putString("latest_instruction", instruction)
            .putString("latest_status", "queued")
            .putLong("latest_created_at", System.currentTimeMillis())
            .apply()
        return id
    }

    fun latest(): Task? {
        val id = prefs.getString("latest_id", null) ?: return null
        return Task(id, prefs.getString("latest_instruction", "") ?: "", prefs.getString("latest_status", "queued") ?: "queued", prefs.getLong("latest_created_at", 0L))
    }

    data class Task(val id: String, val instruction: String, val status: String, val createdAt: Long)
}
