package com.idsanna.android

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat

class IdsannaService : Service() {
    override fun onCreate() { super.onCreate(); createChannel(); startForeground(7, notification("IDsanna activa")) }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int { return START_STICKY }
    private fun createChannel() { getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel("idsanna", "IDsanna", NotificationManager.IMPORTANCE_LOW)) }
    private fun notification(text: String): Notification = NotificationCompat.Builder(this, "idsanna").setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("IDsanna").setContentText(text).setOngoing(true).build()
    override fun onBind(intent: Intent?): IBinder? = null
}
