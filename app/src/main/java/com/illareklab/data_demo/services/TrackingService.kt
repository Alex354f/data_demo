package com.illareklab.data_demo.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.File
import java.io.FileWriter

class TrackingService : Service() {

    companion object {
        private const val CANAL_ID = "track_datademo"
        private const val NOTIF_ID = 200
        private const val INTERVALO_MS = 10_000L  // 10 segundos
    }

    private lateinit var locationManager: LocationManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var file: File

    // Listener del GNSS puro
    private val gnssListener = LocationListener { loc ->
        escribirRegistro("GNSS_PURO", loc.latitude, loc.longitude)
    }

    override fun onCreate() {
        super.onCreate()
        file = File(getExternalFilesDir(null), "historial_sensores.txt")
        crearCanalNotificacion()
        startForeground(NOTIF_ID, buildNotification())

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        iniciarCapturas()
        return START_STICKY
    }

    private fun iniciarCapturas() {
        // Verificación defensiva de permisos antes de cualquier suscripción
        val tienePermiso = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!tienePermiso) {
            stopSelf()
            return
        }

        try {
            // 1. GNSS puro de hardware satelital (cada 10 s)
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                INTERVALO_MS,
                0f,
                gnssListener
            )

            // 2. Fused Location Provider de Google (cada 10 s)
            val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                INTERVALO_MS
            ).build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { loc ->
                        escribirRegistro("FLP_INTELLIGENT", loc.latitude, loc.longitude)
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                mainLooper
            )
        } catch (e: SecurityException) {
            stopSelf()
        }
    }

    // Escritura sincronizada para evitar colisiones entre hilos GNSS y FLP
    @Synchronized
    private fun escribirRegistro(tipo: String, lat: Double, lng: Double) {
        val linea = "${System.currentTimeMillis()} | $tipo | Lat: $lat, Lng: $lng\n"
        FileWriter(file, true).use { it.write(linea) }
    }

    private fun crearCanalNotificacion() {
        val channel = NotificationChannel(
            CANAL_ID,
            "Tracking DataDemo",
            NotificationManager.IMPORTANCE_LOW
        )
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CANAL_ID)
            .setContentTitle("DataDemo: captura en curso")
            .setContentText("Grabando datos de posicionamiento cada 10 s…")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .build()

    override fun onDestroy() {
        super.onDestroy()
        try {
            locationManager.removeUpdates(gnssListener)
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (_: Exception) { /* ignorado */ }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}