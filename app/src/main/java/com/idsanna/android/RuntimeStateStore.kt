package com.idsanna.android

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class RuntimeStateStore(context: Context) : RecoveryStatePersistence {
    private val db = RuntimeDatabase(context.applicationContext).writableDatabase

    override fun saveCheckpoint(checkpoint: Checkpoint) {
        val values = ContentValues().apply {
            put("task_id", checkpoint.taskId)
            put("next_step_id", checkpoint.nextStepId)
            put("status", checkpoint.status)
        }
        db.insertWithOnConflict("checkpoints", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getCheckpoint(taskId: String): Checkpoint? = db.rawQuery(
        "SELECT task_id,next_step_id,status FROM checkpoints WHERE task_id = ?",
        arrayOf(taskId)
    ).use { c ->
        if (c.moveToFirst()) Checkpoint(c.getString(0), c.getString(1), c.getString(2)) else null
    }

    override fun saveApproval(request: ApprovalRequest) {
        val values = ContentValues().apply {
            put("approval_id", "${request.taskId}:${request.toolName}")
            put("task_id", request.taskId)
            put("tool_name", request.toolName)
            put("state", request.state.name)
        }
        db.insertWithOnConflict("approvals", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getApproval(taskId: String, toolName: String): ApprovalRequest? = db.rawQuery(
        "SELECT task_id,tool_name,state FROM approvals WHERE approval_id = ?",
        arrayOf("$taskId:$toolName")
    ).use { c ->
        if (c.moveToFirst()) ApprovalRequest(c.getString(0), c.getString(1), ApprovalState.valueOf(c.getString(2))) else null
    }
}

private class RuntimeDatabase(context: Context) : SQLiteOpenHelper(context, "idsanna_runtime.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE checkpoints (task_id TEXT PRIMARY KEY NOT NULL, next_step_id TEXT, status TEXT NOT NULL)")
        db.execSQL("CREATE TABLE approvals (approval_id TEXT PRIMARY KEY NOT NULL, task_id TEXT NOT NULL, tool_name TEXT NOT NULL, state TEXT NOT NULL)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
}
