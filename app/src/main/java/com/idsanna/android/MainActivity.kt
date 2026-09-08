package com.idsanna.android

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); render() }
    private fun render() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 48, 32, 32); gravity = Gravity.CENTER_HORIZONTAL }
        val title = TextView(this).apply { text = "IDsanna\nAsistente Android supervisado"; textSize = 24f; gravity = Gravity.CENTER; setPadding(0,0,0,32) }
        val status = TextView(this).apply { text = "Servicio detenido"; textSize = 16f; setPadding(0,0,0,24) }
        val start = Button(this).apply { text = "Activar servicio"; setOnClickListener { requestAudio(); ContextCompat.startForegroundService(this@MainActivity, Intent(this@MainActivity, IdsannaService::class.java)); status.text = "Servicio activo: visible y listo" } }
        val listen = Button(this).apply { text = "Escuchar instrucción"; setOnClickListener { startService(Intent(this@MainActivity, IdsannaService::class.java).setAction(IdsannaService.ACTION_LISTEN)); status.text = "Escuchando instrucción" } }
        val stop = Button(this).apply { text = "Detener servicio"; setOnClickListener { stopService(Intent(this@MainActivity, IdsannaService::class.java)); status.text = "Servicio detenido" } }
        val accessibility = Button(this).apply { text = "Configurar control autorizado"; setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) } }
        val overlay = Button(this).apply { text = "Configurar burbuja/ventana"; setOnClickListener { startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:$packageName"))) } }
        root.addView(title); root.addView(status); root.addView(start); root.addView(listen); root.addView(stop); root.addView(accessibility); root.addView(overlay); setContentView(root)
    }
    private fun requestAudio() { if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 10) }
}
