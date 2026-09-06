package com.cloudterm.cloudsync

import com.cloudterm.sync.SyncProvider
import kotlinx.coroutines.delay

/**
 * Orquestador de sincronización (stub).
 *
 * Ruta remota por defecto: `/Apps/CloudTermPro/`
 * Reintentos con backoff exponencial.
 */
class SyncOrchestrator(
    private val provider: SyncProvider,
    private val remoteRoot: String = DEFAULT_REMOTE_ROOT,
    private val maxRetries: Int = 3,
    private val initialBackoffMs: Long = 500
) {
    companion object {
        const val DEFAULT_REMOTE_ROOT = "/Apps/CloudTermPro/"
    }

    suspend fun ensureRoot() {
        withRetry {
            try {
                provider.mkdir(remoteRoot.trimEnd('/'))
            } catch (_: Exception) {
                // puede existir ya
            }
        }
    }

    suspend fun push(localRelative: String, data: ByteArray) {
        val remote = join(remoteRoot, localRelative)
        withRetry { provider.upload(remote, data) }
    }

    suspend fun pull(localRelative: String): ByteArray {
        val remote = join(remoteRoot, localRelative)
        return withRetry { provider.download(remote) }
    }

    suspend fun syncVaultStub(vaultBytes: ByteArray) {
        ensureRoot()
        push("vault.db.enc", vaultBytes)
    }

    private fun join(root: String, rel: String): String =
        root.trimEnd('/') + "/" + rel.trimStart('/')

    private suspend fun <T> withRetry(block: suspend () -> T): T {
        var attempt = 0
        var wait = initialBackoffMs
        while (true) {
            try {
                return block()
            } catch (e: Exception) {
                attempt++
                if (attempt >= maxRetries) throw e
                delay(wait)
                wait *= 2
            }
        }
    }
}