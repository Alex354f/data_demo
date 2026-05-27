package com.illareklab.data_demo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.illareklab.data_demo.workers.NotificationWorker
import java.util.concurrent.TimeUnit

@Composable
fun AlertasScreen() {
    val context = LocalContext.current
    var mensaje by remember { mutableStateOf("") }
    var confirmacion by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            "Pestaña 5 — Alertas diferidas",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Programa una notificación que se dispara 10 segundos después, incluso si cierra la app.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = mensaje,
            onValueChange = { mensaje = it },
            label = { Text("Mensaje de la alerta") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val request = OneTimeWorkRequestBuilder<NotificationWorker>()
                    .setInitialDelay(10, TimeUnit.SECONDS)
                    .setInputData(workDataOf(NotificationWorker.MSG_KEY to mensaje))
                    .build()
                WorkManager.getInstance(context).enqueue(request)
                confirmacion = "Alerta encolada. Llegará en 10 s aunque cierre la app."
            },
            enabled = mensaje.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Programar notificación")
        }

        if (confirmacion.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                confirmacion,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}