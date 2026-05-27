package com.illareklab.data_demo.security

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Hashing de contraseñas con PBKDF2WithHmacSHA256.
 *
 * - 120 000 iteraciones (recomendación OWASP 2023 para SHA-256).
 * - Salida de 256 bits (32 bytes), serializada en hexadecimal.
 * - Comparación en tiempo constante (constantTimeEquals) para evitar
 *   ataques de temporización (timing attacks).
 */
object PasswordHasher {

    private const val ALGORITMO = "PBKDF2WithHmacSHA256"
    private const val ITERACIONES = 120_000
    private const val LONGITUD_HASH_BITS = 256

    /**
     * Calcula el hash de [password] usando el [salt] proporcionado y lo
     * devuelve como cadena hexadecimal en minúsculas.
     */
    fun hash(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(
            password.toCharArray(),
            salt,
            ITERACIONES,
            LONGITUD_HASH_BITS
        )
        val factory = SecretKeyFactory.getInstance(ALGORITMO)
        val bytes = factory.generateSecret(spec).encoded

        // Borrado defensivo del array intermedio en memoria
        spec.clearPassword()

        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Comparación de hashes en **tiempo constante**: el tiempo de respuesta
     * no depende de en qué byte difieren los strings, lo que evita que un
     * atacante deduzca el hash midiendo latencias.
     */
    fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var diff = 0
        for (i in a.indices) {
            diff = diff or (a[i].code xor b[i].code)
        }
        return diff == 0
    }
}