package com.david.administradorarchivos.core.red

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.david.administradorarchivos.R

class ServicioSesion : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val canal = "sesiones_ssh"
        if (Build.VERSION.SDK_INT >= 26) {
            val mgr = getSystemService(NotificationManager::class.java)
            mgr.createNotificationChannel(
                NotificationChannel(canal, "Sesión SSH", NotificationManager.IMPORTANCE_LOW)
            )
        }
        val noti = NotificationCompat.Builder(this, canal)
            .setContentTitle("Sesiones")
            .setContentText("Hay una sesión SSH activa")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
        startForeground(11, noti)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int =
        START_STICKY
}
