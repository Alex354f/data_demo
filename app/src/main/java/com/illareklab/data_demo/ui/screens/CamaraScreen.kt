package com.illareklab.data_demo.ui.screens

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CamaraScreen() {
    val context = LocalContext.current

    // 1. Definición de permisos dinámicos según la versión de Android
    val permisosCamara = buildList {
        add(Manifest.permission.CAMERA)
        // En Android 10 (API 29) o inferior, se requiere el permiso de escritura para MediaStore externo
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    val estadoPermisos = rememberMultiplePermissionsState(permissions = permisosCamara)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Pestaña 2 — Cámara Hardware",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(12.dp))

        // 2. Control de flujo de UI condicionado a los permisos otorgados
        if (estadoPermisos.allPermissionsGranted) {
            CameraPreviewContent(modifier = Modifier.weight(1f))
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Se requieren permisos de cámara para continuar.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { estadoPermisos.launchMultiplePermissionRequest() }) {
                        Text("Conceder Permisos")
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreviewContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 3. Casos de uso e hilos de ejecución persistentes en recomposiciones
    val preview = remember { Preview.Builder().build() }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraSelector = remember { CameraSelector.DEFAULT_BACK_CAMERA }

    val previewView = remember { PreviewView(context) }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    // 4. Vinculación del ciclo de vida de la Cámara con el de la app Android
    LaunchedEffect(Unit) {
        val cameraProviderProvider = ProcessCameraProvider.getInstance(context)
        cameraProviderProvider.addListener({
            val cameraProvider = cameraProviderProvider.get()
            try {
                // Desvincular cualquier caso de uso anterior antes de volver a enlazar
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                // Conectar el flujo físico con la vista inflada en el AndroidView
                preview.setSurfaceProvider(previewView.surfaceProvider)
            } catch (e: Exception) {
                Toast.makeText(context, "Error al iniciar la cámara: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    // 5. Interfaz de usuario: Vista previa + Botón flotante de acción
    Box(modifier = modifier.fillMaxWidth()) {
        // Inflado del componente clásico de UI Android en Jetpack Compose
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        Button(
            onClick = { tomarFoto(context, imageCapture, cameraExecutor) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            Icon(Icons.Default.Camera, contentDescription = "Capturar")
            Spacer(modifier = Modifier.size(8.dp))
            Text("Tomar Foto")
        }
    }
}

// 6. Lógica de captura y persistencia en el almacenamiento externo (Pictures/DataDemo)
private fun tomarFoto(
    context: Context,
    imageCapture: ImageCapture,
    executor: ExecutorService
) {
    // Generar nombre de archivo único basado en el patrón temporal estándar
    val nombreArchivo = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        .format(System.currentTimeMillis())

    // Configurar metadatos para insertar la imagen en el MediaStore público (Almacenamiento Externo)
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_$nombreArchivo.jpg")
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Guarda los archivos directamente en Pictures/DataDemo-Captures
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/DataDemo-Captures")
        }
    }

    // Definición de las opciones de salida de CameraX
    val outputOptions = ImageCapture.OutputFileOptions.Builder(
        context.contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ).build()

    // Ejecutar captura asíncrona delegando el guardado al hilo secundario dedicado
    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                // Alternar de regreso al hilo principal gráfico para interactuar con la UI (Toast)
                ContextCompat.getMainExecutor(context).execute {
                    Toast.makeText(context, "Foto guardada de forma segura", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(exception: ImageCaptureException) {
                ContextCompat.getMainExecutor(context).execute {
                    Toast.makeText(context, "Fallo al guardar: ${exception.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    )
}