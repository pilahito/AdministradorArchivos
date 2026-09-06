package com.cloudterm.vault

import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Stub de bóveda cifrada `vault.db`.
 *
 * - Desbloqueo por passphrase (PBKDF2 → AES-256-GCM)
 * - Biometría: stubs documentados (Android BiometricPrompt / desktop OS keystore)
 * - Access logs locales
 *
 * ## Biometría (futuro)
 * - Android: `BiometricPrompt` + clave envuelta en Android Keystore
 * - Desktop: stub `BiometricUnlock` que devolverá UnsupportedOperation hasta
 *   integrar Windows Hello / macOS Touch ID / libfido2
 */
class EncryptedVault(
    private val vaultFile: File,
    private val accessLog: AccessLog = AccessLog()
) {
    private val unlocked = AtomicBoolean(false)
    private var sessionKey: CharArray? = null
    private var plaintextCache: ByteArray = ByteArray(0)

    val isUnlocked: Boolean get() = unlocked.get()

    fun unlock(passphrase: CharArray): Boolean {
        return try {
            if (!vaultFile.exists()) {
                // Primera vez: crea bóveda vacía cifrada
                val empty = ByteArray(0)
                vaultFile.parentFile?.mkdirs()
                vaultFile.writeBytes(VaultCrypto.encrypt(empty, passphrase))
                plaintextCache = empty
            } else {
                plaintextCache = VaultCrypto.decrypt(vaultFile.readBytes(), passphrase)
            }
            sessionKey = passphrase.copyOf()
            unlocked.set(true)
            accessLog.record(VaultAccessEvent.UNLOCK_OK)
            true
        } catch (e: Exception) {
            accessLog.record(VaultAccessEvent.UNLOCK_FAIL, e.message ?: "error")
            false
        }
    }

    /**
     * Stub biométrico — documentado, no implementado.
     * En Android se enlazará con BiometricPrompt + Keystore.
     */
    fun unlockWithBiometricStub(): Boolean {
        accessLog.record(
            VaultAccessEvent.BIOMETRIC_STUB,
            "Biometría pendiente: BiometricPrompt / Windows Hello / Touch ID"
        )
        return false
    }

    fun lock() {
        sessionKey?.fill('\u0000')
        sessionKey = null
        plaintextCache.fill(0)
        plaintextCache = ByteArray(0)
        unlocked.set(false)
        accessLog.record(VaultAccessEvent.LOCK)
    }

    fun readBytes(): ByteArray {
        check(isUnlocked) { "Bóveda bloqueada" }
        accessLog.record(VaultAccessEvent.READ, "size=${plaintextCache.size}")
        return plaintextCache.copyOf()
    }

    fun writeBytes(data: ByteArray) {
        check(isUnlocked) { "Bóveda bloqueada" }
        val key = sessionKey ?: error("Sin sesión")
        plaintextCache = data.copyOf()
        vaultFile.writeBytes(VaultCrypto.encrypt(plaintextCache, key))
        accessLog.record(VaultAccessEvent.WRITE, "size=${data.size}")
    }

    fun logs(): List<AccessLogEntry> = accessLog.recent()
}