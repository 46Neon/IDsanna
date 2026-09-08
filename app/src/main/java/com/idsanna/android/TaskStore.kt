package com.idsanna.android

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TaskStore(context: Context) {
    private val db = TaskDatabase(context.applicationContext).writableDatabase

    fun enqueue(instruction: String): String {
        val id = "task-${System.currentTimeMillis()}"
        val values = ContentValues().apply {
            put("id", id); put("instruction", instruction); put("status", "queued"); put("created_at", System.currentTimeMillis())
        }
        db.insertOrThrow("tasks", null, values)
        return id
    }

    fun latest(): Task? = query("SELECT id,instruction,status,created_at FROM tasks ORDER BY created_at DESC LIMIT 1")

    fun recent(limit: Int = 20): List<Task> {
        val result = mutableListOf<Task>()
        db.rawQuery("SELECT id,instruction,status,created_at FROM tasks ORDER BY created_at DESC LIMIT ?", arrayOf(limit.coerceIn(1, 100).toString())).use { c ->
            while (c.moveToNext()) result.add(Task(c.getString(0), c.getString(1), c.getString(2), c.getLong(3)))
        }
        return result
    }

    fun updateStatus(id: String, status: String): Boolean {
        val values = ContentValues().apply { put("status", status) }
        return db.update("tasks", values, "id = ?", arrayOf(id)) == 1
    }

    private fun query(sql: String): Task? = db.rawQuery(sql, null).use { c -> if (c.moveToFirst()) Task(c.getString(0), c.getString(1), c.getString(2), c.getLong(3)) else null }

    data class Task(val id: String, val instruction: String, val status: String, val createdAt: Long)
}

private class TaskDatabase(context: Context) : SQLiteOpenHelper(context, "idsanna_tasks.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) { db.execSQL("CREATE TABLE tasks (id TEXT PRIMARY KEY NOT NULL, instruction TEXT NOT NULL, status TEXT NOT NULL, created_at INTEGER NOT NULL)") }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
}
