# DataDemo — Captura de Sensores, Servicios y Sincronización Asíncrona

**Estudiante:** Alexander Miranda Lino  
**Curso:** Desarrolo Movil  
**Institución:** Universidad Nacional Mayor de San Marcos (UNMSM)  
**Facultad:** Facultad de Ingeniería de Sistemas e Informática (FISI)

---

## 📝 Descripción Breve
**DataDemo** es una aplicación Android nativa desarrollada en Kotlin y Jetpack Compose orientada a la gestión eficiente de hardware, persistencia local y sincronización remota de datos. Implementa una arquitectura limpia distribuida en capas (`data`, `security`, `ui`, `services`, `workers`) que asegura la tolerancia a fallos del sistema en escenarios de conectividad limitada o nula.

---

## 📸 Evidencias de Funcionamiento

### 1. Autenticación y Pantalla de Login
![Login](screenshots/01_login.png)
*Control de flujo de navegación local mediante hash criptográfico PBKDF2WithHmacSHA256 siguiendo estándares de OWASP.*

### 2. Pestaña Sensores y Notificación del Foreground Service
![Sensores](screenshots/02_sensores.png)
*Muestra de la interfaz de localización con la barra de notificaciones del sistema desplegada, evidenciando el servicio persistente en segundo plano.*

### 3. Proceso de Sincronización (Antes vs Después)
| Antes de Sincronizar (Caché Poblada) | Después de Sincronizar (Caché Vacía) |
| :---: | :---: |
| ![Antes](screenshots/03_sincronizar_antes.png) | ![Después](screenshots/03_sincronizar_despues.png) |

### 4. Pestaña Alertas (WorkManager)
![Alertas](screenshots/04_alertas.png)
*Notificación push programada y lanzada de manera diferida a través de la API de WorkManager.*

### 5. Inspección de Archivos Locales (Device File Explorer)
![Device Explorer](screenshots/05_device_explorer.png)
*Captura de Android Studio que muestra las líneas de texto del archivo `historial_sensores.txt` acumulando registros con alternancia de orígenes GNSS y FLP.*

### 6. Confirmación de Recepción en el Servidor (Webhook.site)
![Webhook](screenshots/06_webhook.png)
*Verificación del endpoint remoto en el navegador web con el formato estructurado JSON de los paquetes HTTP POST recibidos.*

---

## 🛠️ Ejercicios Completados y Modificaciones Propias

### Nivel 1: Fundamentos y Persistencia
* **Login seguro:** Validación local de credenciales con hashing avanzado para mitigar almacenamiento de texto plano.
* **Persistencia local:** Creación y escritura asíncrona en el archivo plano de texto para la caché de sensores.

### Nivel 2: Servicios y Hardware Continuo
* **Foreground Service:** Rastreo ininterrumpido de geolocalización alternando proveedores de hardware.
* **Integración de Cámara (CameraX):** Inclusión de una vista previa en tiempo real mediante `PreviewView` usando un puente compatible (`AndroidView`) en Jetpack Compose. Permite capturar fotos mediante un botón dedicado y guardarlas de forma segura en la galería pública externa (`Pictures/DataDemo-Captures`) a través de la API de `MediaStore`.
* **Grabador de Audio (MediaRecorder):** Implementación completa de captura de ondas por micrófono codificadas en formato AAC. Cuenta con un **temporizador dinámico** en pantalla en formato `MM:SS` (gestionado mediante corrutinas con un hilo secundario) y un sistema automático de liberación de recursos mediante `DisposableEffect` para prevenir fugas de memoria en el ciclo de vida de la app.

### Nivel 3: Arquitectura Avanzada e Innovaciones
* **Selector Dinámico de Intervalos de Muestreo (Modificación Destacable):** Rediseño total de la interfaz de la pestaña de sensores para incluir un control segmentado multifunción (`ElevatedFilterChip`). Este selector permite al usuario elegir en tiempo real si desea capturar datos cada **5, 10 o 30 segundos**. El intervalo seleccionado se inyecta dinámicamente como un parámetro extra (`putExtra`) en el `Intent` encargado de actualizar y despertar el bucle de localización del servicio de tracking.