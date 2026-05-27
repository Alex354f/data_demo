package com.illareklab.data_demo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.illareklab.data_demo.data.LogMetrica
import com.illareklab.data_demo.data.NetworkManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SyncScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val networkManager = remember { NetworkManager(context) }

    // Lista reactiva observable directamente por Compose
    val listaMetricas = remember { mutableStateListOf<LogMetrica>() }
    var estaSincronizando by remember { mutableStateOf(false) }
    var mensajeEstado by remember { mutableStateOf("Caché local sincronizable") }

    // Carga asíncrona al abrir la pestaña
    LaunchedEffect(Unit) {
        listaMetricas.clear()
        listaMetricas.addAll(networkManager.obtenerDatosLocales())
        if (listaMetricas.isEmpty()) {
            mensajeEstado = "No existen registros guardados localmente."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Monitor de almacenamiento",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            "Registros temporales en la memoria del dispositivo",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                estaSincronizando = true
                scope.launch {
                    val colaOriginal = ArrayList(listaMetricas)

                    for (metrica in colaOriginal) {
                        mensajeEstado = "Transmitiendo ID: ${metrica.id}…"
                        val exito = networkManager.enviarRegistroIndividual(metrica)

                        if (exito) {
                            // Delay perceptible para evidenciar la animación
                            delay(300)

                            // Remueve de la UI → gatilla el refresco asíncrono
                            listaMetricas.remove(metrica)

                            // Limpia el archivo físico inmediatamente
                            networkManager.actualizarArchivoLocal(listaMetricas)
                        } else {
                            mensajeEstado =
                                "Transmisión fallida. Sincronización pausada para proteger la caché."
                            break
                        }
                    }

                    estaSincronizando = false
                    if (listaMetricas.isEmpty()) {
                        mensajeEstado = "Almacenamiento temporal completamente limpio."
                    }
                }
            },
            enabled = listaMetricas.isNotEmpty() && !estaSincronizando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.CloudUpload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                if (estaSincronizando) "Vaciando memoria…"
                else "Sincronizar y limpiar caché (POST)"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            mensajeEstado,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Lista reactiva con llaves únicas → Compose sabe exactamente qué item animar
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = listaMetricas, key = { it.id }) { metrica ->
                AnimatedVisibility(
                    visible = true,
                    exit = shrinkVertically(animationSpec = tween(durationMillis = 300))
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    metrica.origen,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    metrica.coordenadas,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}