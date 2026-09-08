package com.idsanna.android

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

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
        }
        db.insertWithOnConflict("operations", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    override fun get(operationId: String): OperationRecord? = db.rawQuery(
        "SELECT operation_id,task_id,step_id,tool_name,started_at,timeout_at,state,evidence " +
            "FROM operations WHERE operation_id = ?",
        arrayOf(operationId)
    ).use { c ->
        if (!c.moveToFirst()) return null
        readRecord(c)
    }

    override fun list(state: OperationState): List<OperationRecord> = db.rawQuery(
        "SELECT operation_id,task_id,step_id,tool_name,started_at,timeout_at,state,evidence " +
            "FROM operations WHERE state = ? ORDER BY started_at ASC",
        arrayOf(state.name)
    ).use { c ->
        buildList {
            while (c.moveToNext()) add(readRecord(c))
        }
    }

    private fun readRecord(c: android.database.Cursor): OperationRecord = OperationRecord(
        operationId = c.getString(0),
        taskId = c.getString(1),
        stepId = c.getString(2),
        toolName = c.getString(3),
        startedAt = c.getLong(4),
        timeoutAt = c.getLong(5),
        state = OperationState.valueOf(c.getString(6)),
        evidence = c.getString(7)
    )
}

private class OperationDatabase(context: Context) : SQLiteOpenHelper(context, "idsanna_operations.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE operations (" +
                "operation_id TEXT PRIMARY KEY NOT NULL, " +
                "task_id TEXT, step_id TEXT, tool_name TEXT, " +
                "started_at INTEGER NOT NULL, timeout_at INTEGER NOT NULL, " +
                "state TEXT NOT NULL, evidence TEXT)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
}
