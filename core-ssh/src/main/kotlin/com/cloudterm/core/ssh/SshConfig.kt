package com.cloudterm.core.ssh

/**
 * Parámetros de conexión SSH para CloudTerm Pro.
 */
data class SshConfig(
    val host: String,
    val port: Int = 22,
    val username: String,
    val password: String? = null,
    /** Ruta a clave privada PEM/OpenSSH. */
    val privateKeyPath: String? = null,
    /** Passphrase de la clave (opcional). */
    val privateKeyPassphrase: String? = null,
    val connectTimeoutMs: Int = 15_000,
    /** Si true, acepta cualquier host key (solo desarrollo / LAN confiable). */
    val promiscuousHostKey: Boolean = true
)