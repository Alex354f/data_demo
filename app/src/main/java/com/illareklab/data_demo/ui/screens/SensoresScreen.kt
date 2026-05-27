package com.illareklab.data_demo.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.illareklab.data_demo.services.TrackingService

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SensoresScreen() {
    val context = LocalContext.current

    // Estado para almacenar el intervalo seleccionado en segundos (10s por defecto)
    var intervaloSegundos by remember { mutableLongStateOf(10L) }

    // Lista dinámica de permisos según versión de Android
    val permisos = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val estadoPermisos = rememberMultiplePermissionsState(permissions = permisos)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Pestaña 1 — Hardware de posicionamiento",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Captura continua GNSS + FLP activa.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (estadoPermisos.allPermissionsGranted) {

            // Selector de Intervalos (Segmented Control usando FilterChips)
            Text(
                "Seleccione el intervalo de muestreo:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val opciones = listOf(5L, 10L, 30L)
                opciones.forEach { segundos ->
                    ElevatedFilterChip(
                        selected = intervaloSegundos == segundos,
                        onClick = { intervaloSegundos = segundos },
                        label = { Text("$segundos seg") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val intent = Intent(context, TrackingService::class.java).apply {
                        // Enviamos el intervalo convertido a milisegundos al servicio
                        putExtra("EXTRA_INTERVALO_MS", intervaloSegundos * 1000L)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Iniciar captura cada $intervaloSegundos s")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    context.stopService(Intent(context, TrackingService::class.java))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Detener captura")
            }
        } else {
            Text(
                "Esta pantalla necesita permisos de ubicación y notificación.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { estadoPermisos.launchMultiplePermissionRequest() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Conceder permisos")
            }
        }
    }
}