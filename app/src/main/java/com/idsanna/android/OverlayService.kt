package com.idsanna.android

import android.app.NotificationChannel
import android.app.Service
import androidx.core.app.NotificationCompat
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.view.inputmethod.InputMethodManager

class OverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private var bubble: TextView? = null
    private var panel: LinearLayout? = null
    private var bubbleParams: WindowManager.LayoutParams? = null
    private var downX = 0f
    private var downY = 0f
    private var startX = 0
    private var startY = 0
    private var moved = false

    override fun onCreate() { super.onCreate(); createChannel(); startForeground(8, NotificationCompat.Builder(this, "idsanna_overlay").setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("IDsanna").setContentText("Burbuja de instrucciones activa").setOngoing(true).build()); windowManager = getSystemService(WINDOW_SERVICE) as WindowManager; showBubble() }
    private fun createChannel() { getSystemService(android.app.NotificationManager::class.java).createNotificationChannel(NotificationChannel("idsanna_overlay", "IDsanna control", android.app.NotificationManager.IMPORTANCE_LOW)) }

    private fun baseParams(width: Int, height: Int): WindowManager.LayoutParams = WindowManager.LayoutParams(width, height, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.START; x = 16; y = 220 }

    private fun showBubble() {
        bubble = TextView(this).apply { text = "ID"; textSize = 14f; setTextColor(Color.WHITE); gravity = Gravity.CENTER; setBackgroundColor(Color.rgb(103, 80, 164)); setPadding(22, 16, 22, 16) }
        bubbleParams = baseParams(120, 70)
        bubble?.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> { downX = event.rawX; downY = event.rawY; startX = bubbleParams!!.x; startY = bubbleParams!!.y; moved = false; true }
                MotionEvent.ACTION_MOVE -> { val dx = (event.rawX - downX).toInt(); val dy = (event.rawY - downY).toInt(); if (kotlin.math.abs(dx) > 8 || kotlin.math.abs(dy) > 8) moved = true; bubbleParams!!.x = startX + dx; bubbleParams!!.y = startY + dy; windowManager.updateViewLayout(bubble, bubbleParams); true }
                MotionEvent.ACTION_UP -> { if (!moved) togglePanel(); else dockBubble(); true }
                else -> true
            }
        }
        windowManager.addView(bubble, bubbleParams)
    }

    private fun dockBubble() { val metrics = resources.displayMetrics; bubbleParams!!.x = if (bubbleParams!!.x < metrics.widthPixels / 2) 0 else metrics.widthPixels - 120; windowManager.updateViewLayout(bubble, bubbleParams); showPanel() }

    private fun togglePanel() { if (panel == null) showPanel() else hidePanel() }

    private fun showPanel() {
        if (panel != null) return
        val input = EditText(this).apply { hint = "Escribe una instrucción para IDsanna"; setTextColor(Color.WHITE); setHintTextColor(Color.LTGRAY); setSingleLine(false); minLines = 2 }
        val taskStatus = TextView(this).apply { setTextColor(Color.WHITE); textSize = 14f; setPadding(0, 10, 0, 10); text = TaskStore(this@OverlayService).latest()?.let { taskCard(it) } ?: "No hay tareas todavía" }
        val send = Button(this).apply { text = "Enviar"; setOnClickListener { val command = input.text.toString().trim(); if (command.isNotEmpty()) { val store = TaskStore(this@OverlayService); val task = store.enqueue(command); val agent = AgentContract().analyze(command); store.updateStatus(task, taskStatusFor(agent.status)); taskStatus.text = agentCard(task, command, agent); Toast.makeText(this@OverlayService, "Instrucción analizada", Toast.LENGTH_SHORT).show(); input.text.clear() } } }
        val close = Button(this).apply { text = "Cerrar"; setOnClickListener { hidePanel() } }
        panel = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(18, 14, 18, 14); setBackgroundColor(Color.rgb(35, 35, 42)); addView(TextView(this@OverlayService).apply { text = "IDsanna · instrucción"; setTextColor(Color.WHITE); textSize = 16f }); addView(taskStatus); addView(input); addView(send); addView(close) }
        input.isFocusableInTouchMode = true
        val p = WindowManager.LayoutParams(720, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.START; x = if (bubbleParams!!.x == 0) 8 else resources.displayMetrics.widthPixels - 728; y = bubbleParams!!.y.coerceAtLeast(80); softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE }
        windowManager.addView(panel, p)
        input.requestFocus()
        input.post { (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(input, InputMethodManager.SHOW_IMPLICIT) }
    }

    private fun taskStatusFor(status: AgentStatus): String = when (status) {
        AgentStatus.INVALID -> "invalid"
        AgentStatus.NEEDS_CLARIFICATION -> "needs_clarification"
        AgentStatus.READY_FOR_APPROVAL -> "approval_required"
        else -> "queued"
    }

    private fun agentCard(taskId: String, instruction: String, result: AgentResult): String {
        val missing = if (result.missing.isEmpty()) "ninguno" else result.missing.joinToString(", ")
        val tools = if (result.plan.isEmpty()) "ninguna" else result.plan.joinToString(", ") { it.tool }
        return "Tarea analizada\nID: $taskId\nEstado: ${statusLabel(taskStatusFor(result.status))}\nIntención: ${result.intent}\nDestino: ${result.target}\nParámetros faltantes: $missing\nHerramientas propuestas: $tools\nEjecución: no iniciada\nVerificación: pendiente\n\n${resultMessage(result)}\n\nInstrucción guardada:\n$instruction"
    }

    private fun resultMessage(result: AgentResult): String = when (result.status) {
        AgentStatus.NEEDS_CLARIFICATION -> "Se necesitan más datos antes de continuar."
        AgentStatus.READY_FOR_APPROVAL -> "Plan listo; requiere aprobación antes de ejecutar."
        AgentStatus.INVALID -> "La instrucción no es válida: ${result.errors.joinToString(", ")}"
        else -> "Resultado: ${result.status}"
    }

    private fun taskCard(task: TaskStore.Task): String = "Tarea registrada\nID: ${task.id}\nEstado: ${statusLabel(task.status)}\nEjecución: no iniciada\nVerificación: pendiente\n\n${task.instruction}"

    private fun statusLabel(status: String): String = when (status) {
        "queued" -> "creada"
        "planned" -> "planificada"
        "approval_required" -> "aprobación requerida"
        "running" -> "ejecución iniciada"
        "completed" -> "ejecutada"
        "verified" -> "verificada"
        "failed" -> "fallida"
        "cancelled" -> "cancelada"
        else -> status
    }

    private fun hidePanel() { panel?.let { windowManager.removeView(it) }; panel = null }
    override fun onDestroy() { hidePanel(); bubble?.let { windowManager.removeView(it) }; bubble = null; super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
}
