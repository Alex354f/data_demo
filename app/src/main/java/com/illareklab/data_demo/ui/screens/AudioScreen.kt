package com.illareklab.data_demo.ui.screens

import android.Manifest
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AudioScreen() {
    val context = LocalContext.current

    // 1. Estado del permiso para acceder al micrófono físico
    val permisoMicrofono = rememberPermissionState(permission = Manifest.permission.RECORD_AUDIO)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Pestaña 3 — Audio Hardware",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 2. Control de flujo según estado del permiso
        if (permisoMicrofono.status.isGranted) {
            AudioRecorderContent(context = context)
        } else {
            Text(
                "Se requiere el permiso de micrófono para realizar grabaciones de prueba.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            Button(onClick = { permisoMicrofono.launchPermissionRequest() }) {
                Text("Conceder Permiso de Audio")
            }
        }
    }
}

@Composable
fun AudioRecorderContent(context: Context) {
    var estaGrabando by remember { mutableStateOf(false) }
    var tiempoTranscurrido by remember { mutableLongStateOf(0L) } // Tiempo en segundos
    var rutaArchivoAudio by remember { mutableStateOf<File?>(null) }

    // Instancia del MediaRecorder
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }

    // 3. Temporizador asíncrono acoplado al estado de grabación
    LaunchedEffect(estaGrabando) {
        if (estaGrabando) {
            tiempoTranscurrido = 0L
            while (estaGrabando) {
                delay(1000L)
                tiempoTranscurrido++
            }
        }
    }

    // 4. Limpieza del hardware al destruir la vista (evita memory leaks)
    DisposableEffect(Unit) {
        onDispose {
            if (estaGrabando) {
                try {
                    mediaRecorder?.stop()
                } catch (_: Exception) {}
            }
            mediaRecorder?.release()
            mediaRecorder = null
        }
    }

    // Animación visual de pulso en el icono del micrófono
    val infiniteTransition = rememberInfiniteTransition(label = "pulsoMic")
    val scaleFactor by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (estaGrabando) 1.25f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "escala"
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Indicador visual de estado
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(100.dp)
            .scale(scaleFactor)
            .background(
                color = if (estaGrabando) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = if (estaGrabando) Icons.Default.Stop else Icons.Default.Mic,
            contentDescription = "Estado Micrófono",
            tint = if (estaGrabando) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(40.dp)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 5. Renderizado del tiempo transcurrido (llamada corregida sin espacios)
    Text(
        text = formatearTiempo(tiempoTranscurrido),
        style = MaterialTheme.typography.displayMedium,
        color = if (estaGrabando) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = if (estaGrabando) "Grabando audio..." else "Micrófono listo",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(24.dp))

    // 6. Botones de Acción
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!estaGrabando) {
            Button(
                onClick = {
                    val archivo = crearArchivoAudioLocal(context)
                    rutaArchivoAudio = archivo

                    mediaRecorder = crearMediaRecorder(context, archivo).apply {
                        try {
                            prepare()
                            start()
                            estaGrabando = true
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error al preparar MediaRecorder: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text("Iniciar Grabación")
            }
        } else {
            Button(
                onClick = {
                    try {
                        mediaRecorder?.apply {
                            stop()
                            release()
                        }
                    } catch (e: Exception) {
                        // Maneja errores si la grabación fue extremadamente corta
                    } finally {
                        mediaRecorder = null
                        estaGrabando = false
                        Toast.makeText(context, "Audio guardado con éxito", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text("Detener y Guardar")
            }
        }

        // Muestra la ubicación del último archivo generado
        AnimatedVisibility(visible = !estaGrabando && rutaArchivoAudio != null) {
            Text(
                text = "Último archivo: ${rutaArchivoAudio?.name}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

// Función auxiliar de formateo corregida y limpia
private fun formatearTiempo(segundosTotales: Long): String {
    val minutos = segundosTotales / 60
    val segundos = segundosTotales % 60
    return String.format(Locale.US, "%02d:%02d", minutos, segundos)
}

// Crea la referencia del archivo dentro del sandbox privado de la aplicación
private fun crearArchivoAudioLocal(context: Context): File {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
    val nombreArchivo = "AUDIO_$timestamp.mp3"
    return File(context.getExternalFilesDir(null), nombreArchivo)
}

// Factory para soportar MediaRecorder en todas las APIs de Android
@Suppress("DEPRECATION")
private fun crearMediaRecorder(context: Context, archivoDestino: File): MediaRecorder {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MediaRecorder(context)
    } else {
        MediaRecorder()
    }.apply {
        setAudioSource(MediaRecorder.AudioSource.MIC)
        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        setOutputFile(archivoDestino.absolutePath)
    }
}