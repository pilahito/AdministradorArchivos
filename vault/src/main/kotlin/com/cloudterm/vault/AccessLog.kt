package com.cloudterm.vault

import java.time.Instant
import java.util.concurrent.CopyOnWriteArrayList

enum class VaultAccessEvent {
    UNLOCK_OK,
    UNLOCK_FAIL,
    LOCK,
    READ,
    WRITE,
    BIOMETRIC_STUB
}

data class AccessLogEntry(
    val at: Instant = Instant.now(),
    val event: VaultAccessEvent,
    val detail: String = ""
)

class AccessLog {
    private val entries = CopyOnWriteArrayList<AccessLogEntry>()

    fun log(event: VaultAccessEvent, detail: String = "") {
        entries += AccessLogEntry(event = event, detail = detail)
    }

    /** Alias usado por EncryptedVault. */
    fun record(event: VaultAccessEvent, detail: String = "") = log(event, detail)

    fun recent(limit: Int = 50): List<AccessLogEntry> =
        entries.takeLast(limit)

    fun clear() = entries.clear()
}