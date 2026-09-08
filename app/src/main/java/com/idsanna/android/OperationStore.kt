package com.idsanna.android

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class OperationStore(context: Context) {
    private val db = OperationDatabase(context.applicationContext).writableDatabase

    fun save(record: OperationRecord) {
        val values = ContentValues().apply {
            put("operation_id", record.operationId); put("task_id", record.taskId); put("step_id", record.stepId)
            put("tool_name", record.toolName); put("started_at", record.startedAt); put("timeout_at", record.timeoutAt)
            put("state", record.state.name); put("evidence", record.evidence)
        }
        db.insertWithOnConflict("operations", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun get(operationId: String): OperationRecord? = db.rawQuery("SELECT operation_id,task_id,step_id,tool_name,started_at,timeout_at,state,evidence FROM operations WHERE operation_id = ?", arrayOf(operationId)).use { c ->
        if (!c.moveToFirst()) return null
        OperationRecord(c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getLong(4), c.getLong(5), OperationState.valueOf(c.getString(6)), c.getString(7))
    }
}

private class OperationDatabase(context: Context) : SQLiteOpenHelper(context, "idsanna_operations.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) { db.execSQL("CREATE TABLE operations (operation_id TEXT PRIMARY KEY NOT NULL, task_id TEXT, step_id TEXT, tool_name TEXT, started_at INTEGER NOT NULL, timeout_at INTEGER NOT NULL, state TEXT NOT NULL, evidence TEXT)") }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
}
