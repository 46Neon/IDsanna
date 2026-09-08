package com.idsanna.android

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONObject

interface OperationPersistence {
    fun save(record: OperationRecord)
    fun get(operationId: String): OperationRecord?
    fun list(state: OperationState = OperationState.RUNNING): List<OperationRecord> = emptyList()
}

class OperationStore(context: Context) : OperationPersistence {
    private val db = OperationDatabase(context.applicationContext).writableDatabase

    override fun save(record: OperationRecord) {
        val values = ContentValues().apply {
            put("operation_id", record.operationId)
            put("task_id", record.taskId)
            put("step_id", record.stepId)
            put("tool_name", record.toolName)
            put("started_at", record.startedAt)
            put("timeout_at", record.timeoutAt)
            put("state", record.state.name)
            put("evidence", record.evidence)
            put("arguments_json", JSONObject(record.arguments).toString())
        }
        db.insertWithOnConflict("operations", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    override fun get(operationId: String): OperationRecord? = db.rawQuery(
        SELECT,
        arrayOf(operationId)
    ).use { c -> if (c.moveToFirst()) readRecord(c) else null }

    override fun list(state: OperationState): List<OperationRecord> = db.rawQuery(
        "$SELECT_BASE WHERE state = ? ORDER BY started_at ASC",
        arrayOf(state.name)
    ).use { c ->
        val result = mutableListOf<OperationRecord>()
        while (c.moveToNext()) result += readRecord(c)
        result
    }

    private fun readRecord(c: Cursor): OperationRecord = OperationRecord(
        operationId = c.getString(0),
        taskId = c.getString(1),
        stepId = c.getString(2),
        toolName = c.getString(3),
        startedAt = c.getLong(4),
        timeoutAt = c.getLong(5),
        state = OperationState.valueOf(c.getString(6)),
        evidence = c.getString(7),
        arguments = parseArguments(c.getString(8))
    )

    private fun parseArguments(raw: String?): Map<String, String> {
        if (raw.isNullOrBlank()) return emptyMap()
        val json = JSONObject(raw)
        val result = mutableMapOf<String, String>()
        json.keys().forEach { key -> result[key] = json.optString(key) }
        return result
    }

    companion object {
        private const val SELECT_BASE = "SELECT operation_id,task_id,step_id,tool_name,started_at,timeout_at,state,evidence,arguments_json FROM operations"
        private const val SELECT = "$SELECT_BASE WHERE operation_id = ?"
    }
}

private class OperationDatabase(context: Context) : SQLiteOpenHelper(context, "idsanna_operations.db", null, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE operations (operation_id TEXT PRIMARY KEY NOT NULL, task_id TEXT, step_id TEXT, tool_name TEXT, started_at INTEGER NOT NULL, timeout_at INTEGER NOT NULL, state TEXT NOT NULL, evidence TEXT, arguments_json TEXT NOT NULL DEFAULT '{}')")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) db.execSQL("ALTER TABLE operations ADD COLUMN arguments_json TEXT NOT NULL DEFAULT '{}'")
    }
}
