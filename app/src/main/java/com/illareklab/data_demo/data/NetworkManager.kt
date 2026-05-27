package com.illareklab.data_demo.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

// Estructura de datos que la UI consumirá de forma reactiva
data class LogMetrica(
    val id: Long,                  // timestamp como identificador único invariable
    val origen: String,            // "GNSS_PURO" o "FLP_INTELLIGENT"
    val coordenadas: String,       // "Lat: -12.04, Lng: -77.04"
    val textoPlanoOriginal: String // línea original tal cual fue escrita
)

class NetworkManager(private val context: Context) {

    private val file = File(context.getExternalFilesDir(null), "historial_sensores.txt")

    // Reemplazar por tu endpoint real o IP local de pruebas
    private val endpoint = "https://webhook.site/382fe7c3-cee4-4da5-9915-a6cbec3ccc87"

    // ── 1. Lectura del historial plano → lista de objetos ────────────
    fun obtenerDatosLocales(): List<LogMetrica> {
        if (!file.exists() || file.length() == 0L) return emptyList()

        return file.readLines().mapNotNull { linea ->
            // Formato esperado: "timestamp | TIPO_SENSOR | Lat: ..., Lng: ..."
            val partes = linea.split(" | ")
            if (partes.size >= 3) {
                LogMetrica(
                    id = partes[0].toLongOrNull() ?: System.currentTimeMillis(),
                    origen = partes[1],
                    coordenadas = partes[2],
                    textoPlanoOriginal = linea
                )
            } else null
        }
    }

    // ── 2. Envío individual asíncrono ────────────────────────────────
    suspend fun enviarRegistroIndividual(metrica: LogMetrica): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val urlConnection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "text/plain; charset=utf-8")
                    doOutput = true
                    connectTimeout = 3000
                    readTimeout = 3000
                }

                urlConnection.outputStream.use { os ->
                    val bytes = metrica.textoPlanoOriginal.toByteArray(Charsets.UTF_8)
                    os.write(bytes, 0, bytes.size)
                }

                val responseCode = urlConnection.responseCode
                urlConnection.disconnect()

                responseCode == HttpURLConnection.HTTP_OK ||
                        responseCode == HttpURLConnection.HTTP_CREATED
            } catch (e: Exception) {
                false
            }
        }

    // ── 3. Reescritura segura del archivo físico ────────────────────
    fun actualizarArchivoLocal(metricasRestantes: List<LogMetrica>) {
        if (metricasRestantes.isEmpty()) {
            file.writeText("") // vaciado total cuando la cola llegó a cero
        } else {
            val nuevoContenido =
                metricasRestantes.joinToString("\n") { it.textoPlanoOriginal } + "\n"
            file.writeText(nuevoContenido)
        }
    }
}