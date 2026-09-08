package com.idsanna.android

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var listenButton: Button
    private val permissionsRequest = 20

    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); render() }

    private fun render() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 44, 32, 32); gravity = Gravity.CENTER_HORIZONTAL }
        val title = TextView(this).apply { text = "IDsanna\nAsistente Android supervisado"; textSize = 24f; gravity = Gravity.CENTER; setPadding(0, 0, 0, 18) }
        val description = TextView(this).apply { text = "La voz solo se activa con autorización visible. Las acciones del sistema aún no están habilitadas."; textSize = 15f; gravity = Gravity.CENTER; setPadding(0, 0, 0, 18) }
        status = TextView(this).apply { text = "Estado: servicio detenido"; textSize = 16f; gravity = Gravity.CENTER; setPadding(0, 0, 0, 18) }
        val session = VoiceSession(this)
        val code = TextView(this).apply { text = "Código de sesión: ${session.activationCode()}\nVálido durante cinco minutos después de activarlo"; textSize = 16f; gravity = Gravity.CENTER; setPadding(0, 0, 0, 22) }
        val start = Button(this).apply { text = "Activar servicio"; setOnClickListener { ensurePermissionsAndStart() } }
        listenButton = Button(this).apply { text = "Escuchar instrucción"; isEnabled = false; setOnClickListener { sendServiceAction(IdsannaService.ACTION_LISTEN); status.text = "Estado: escuchando" } }
        val stop = Button(this).apply { text = "Detener servicio"; setOnClickListener { stopService(Intent(this@MainActivity, IdsannaService::class.java)); listen.isEnabled = false; status.text = "Estado: servicio detenido" } }
        val accessibility = Button(this).apply { text = "Configurar control autorizado"; setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) } }
        val overlay = Button(this).apply { text = "Configurar ventana flotante"; setOnClickListener { startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:$packageName"))) } }
        root.addView(title); root.addView(description); root.addView(status); root.addView(code); root.addView(start); root.addView(listenButton); root.addView(stop); root.addView(accessibility); root.addView(overlay); setContentView(root)
    }

    private fun ensurePermissionsAndStart() {
        val required = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= 33) required.add(Manifest.permission.POST_NOTIFICATIONS)
        val missing = required.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) ActivityCompat.requestPermissions(this, missing.toTypedArray(), permissionsRequest) else startVoiceService()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, results: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
        if (requestCode == permissionsRequest && results.isNotEmpty() && results.all { it == PackageManager.PERMISSION_GRANTED }) startVoiceService()
        else status.text = "Estado: faltan permisos para activar la voz"
    }

    private fun startVoiceService() {
        ContextCompat.startForegroundService(this, Intent(this, IdsannaService::class.java))
        status.text = "Estado: servicio activo y visible"
        listenButton.isEnabled = true
    }

    private fun sendServiceAction(action: String) { startService(Intent(this, IdsannaService::class.java).setAction(action)) }
}
