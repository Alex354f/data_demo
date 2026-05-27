package com.illareklab.data_demo.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    companion object {
        const val MSG_KEY = "MSG_KEY"
        private const val CANAL_ID = "datademo_alerts"
    }

    override fun doWork(): Result {
        val mensaje = inputData.getString(MSG_KEY) ?: "Alerta activada"

        val manager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // El canal puede crearse muchas veces sin efectos secundarios
        val channel = NotificationChannel(
            CANAL_ID,
            "Recordatorios",
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(applicationContext, CANAL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Notificación temporal DataDemo")
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
        return Result.success()
    }
}