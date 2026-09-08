package com.idsanna.android

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

class MainActivity : Activity() {
    private lateinit var status: TextView
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); render() }

    private fun render() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 44, 32, 32); gravity = Gravity.CENTER_HORIZONTAL }
        val title = TextView(this).apply { text = "IDsanna\nControl Android por texto"; textSize = 24f; gravity = Gravity.CENTER; setPadding(0, 0, 0, 18) }
        val description = TextView(this).apply { text = "La burbuja permite escribir instrucciones mientras usas otras aplicaciones. Las acciones se añadirán después de validar este canal."; textSize = 15f; gravity = Gravity.CENTER; setPadding(0, 0, 0, 18) }
        status = TextView(this).apply { text = "Estado: burbuja detenida"; textSize = 16f; gravity = Gravity.CENTER; setPadding(0, 0, 0, 18) }
        val start = Button(this).apply { text = "Activar burbuja"; setOnClickListener { if (!Settings.canDrawOverlays(this@MainActivity)) { startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))); status.text = "Estado: autoriza la ventana flotante" } else { ContextCompat.startForegroundService(this@MainActivity, Intent(this@MainActivity, OverlayService::class.java)); status.text = "Estado: burbuja activa" } } }
        val stop = Button(this).apply { text = "Detener burbuja"; setOnClickListener { stopService(Intent(this@MainActivity, OverlayService::class.java)); status.text = "Estado: burbuja detenida" } }
        val accessibility = Button(this).apply { text = "Configurar control autorizado"; setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) } }
        root.addView(title); root.addView(description); root.addView(status); root.addView(start); root.addView(stop); root.addView(accessibility); setContentView(root)
    }
}
