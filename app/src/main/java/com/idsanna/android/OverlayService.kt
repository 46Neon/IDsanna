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
        val send = Button(this).apply { text = "Enviar"; setOnClickListener { val command = input.text.toString().trim(); if (command.isNotEmpty()) { Toast.makeText(this@OverlayService, "Instrucción recibida", Toast.LENGTH_SHORT).show(); input.text.clear(); } } }
        val close = Button(this).apply { text = "Cerrar"; setOnClickListener { hidePanel() } }
        panel = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(18, 14, 18, 14); setBackgroundColor(Color.rgb(35, 35, 42)); addView(TextView(this@OverlayService).apply { text = "IDsanna · instrucción"; setTextColor(Color.WHITE); textSize = 16f }); addView(input); addView(send); addView(close) }
        val p = WindowManager.LayoutParams(720, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.START; x = if (bubbleParams!!.x == 0) 8 else resources.displayMetrics.widthPixels - 728; y = bubbleParams!!.y.coerceAtLeast(80) }
        windowManager.addView(panel, p)
    }

    private fun hidePanel() { panel?.let { windowManager.removeView(it) }; panel = null }
    override fun onDestroy() { hidePanel(); bubble?.let { windowManager.removeView(it) }; bubble = null; super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
}
