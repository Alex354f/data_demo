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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.illareklab.data_demo.security.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ── Credenciales almacenadas (nunca en texto plano) ─────────────────
// Usuario válido (en producción vendría de una base de datos)
private const val USUARIO_VALIDO = "admin"

// Salt de 16 bytes ASCII: "DataDemoSalt0123"
// En producción, cada usuario tendría su propio salt aleatorio.
private val SALT_DEMO: ByteArray = byteArrayOf(
    0x44, 0x61, 0x74, 0x61, 0x44, 0x65, 0x6d, 0x6f,
    0x53, 0x61, 0x6c, 0x74, 0x30, 0x31, 0x32, 0x33
)

// Hash precomputado de "admin" con el SALT_DEMO + 120 000 iteraciones.
// Para regenerarlo, ejecute PasswordHasher.hash("nuevoPass", SALT_DEMO)
// una vez en el debugger y pegue el resultado aquí.
// Nueva contraseña: barcelona05
private const val HASH_PASSWORD_ADMIN =
    "da2a3b6941ebb247b797c0705897c20df2cf22bbd7f0bec9cbea179322e68c78"

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val scope = rememberCoroutineScope()

    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMensaje by remember { mutableStateOf("") }
    var verificando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Sistema DataDemo",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it },
            label = { Text("Usuario") },
            singleLine = true,
            enabled = !verificando,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            singleLine = true,
            enabled = !verificando,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMensaje.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMensaje, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                errorMensaje = ""
                verificando = true
                scope.launch {
                    // El hashing PBKDF2 con 120 000 iteraciones tarda
                    // ~100-300 ms. Se ejecuta en Dispatchers.Default
                    // para no bloquear el hilo de UI.
                    val coincide = withContext(Dispatchers.Default) {
                        val hashIngresado = PasswordHasher.hash(password, SALT_DEMO)
                        usuario == USUARIO_VALIDO &&
                                PasswordHasher.constantTimeEquals(
                                    hashIngresado,
                                    HASH_PASSWORD_ADMIN
                                )
                    }
                    verificando = false
                    if (coincide) {
                        onLoginSuccess()
                    } else {
                        errorMensaje = "Credenciales incorrectas. Pruebe admin/admin."
                    }
                }
            },
            enabled = !verificando && usuario.isNotBlank() && password.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(if (verificando) "Verificando…" else "Ingresar")
        }
    }
}