package com.cloudterm.vault

import java.io.File
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/** Boveda AES-256-GCM. Fallback passphrase obligatorio. Sin datos biometricos en logs. */
class Vault(private val file: File = File("vault.db")) {
    data class Entry(val id: String, val label: String, val payload: ByteArray)

    fun unlock(passphrase: CharArray): Boolean {
        if (!file.exists()) { persist(passphrase, emptyList()); log("vault_created"); return true }
        return try { load(passphrase); log("unlock_ok"); true } catch (_: Exception) { log("unlock_fail"); false }
    }

    fun put(passphrase: CharArray, entry: Entry) {
        val list = load(passphrase).toMutableList()
        list.removeAll { it.id == entry.id }
        list += entry
        persist(passphrase, list)
        log("put:" + entry.id)
    }

    fun list(passphrase: CharArray): List<Entry> = load(passphrase)

    private fun derive(pass: CharArray, salt: ByteArray): SecretKeySpec {
        val f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val key = f.generateSecret(PBEKeySpec(pass, salt, 120_000, 256)).encoded
        return SecretKeySpec(key, "AES")
    }

    private fun persist(pass: CharArray, entries: List<Entry>) {
        val rnd = SecureRandom()
        val salt = ByteArray(16).also { rnd.nextBytes(it) }
        val iv = ByteArray(12).also { rnd.nextBytes(it) }
        val plain = entries.joinToString("\n") {
            it.id + "|" + it.label + "|" + Base64.getEncoder().encodeToString(it.payload)
        }.toByteArray(Charsets.UTF_8)
        val c = Cipher.getInstance("AES/GCM/NoPadding")
        c.init(Cipher.ENCRYPT_MODE, derive(pass, salt), GCMParameterSpec(128, iv))
        file.writeBytes(salt + iv + c.doFinal(plain))
    }

    private fun load(pass: CharArray): List<Entry> {
        val raw = file.readBytes(); require(raw.size > 28)
        val salt = raw.copyOfRange(0, 16); val iv = raw.copyOfRange(16, 28); val enc = raw.copyOfRange(28, raw.size)
        val c = Cipher.getInstance("AES/GCM/NoPadding")
        c.init(Cipher.DECRYPT_MODE, derive(pass, salt), GCMParameterSpec(128, iv))
        val plain = String(c.doFinal(enc), Charsets.UTF_8)
        if (plain.isBlank()) return emptyList()
        return plain.lines().filter { it.isNotBlank() }.map {
            val p = it.split("|", limit = 3)
            Entry(p[0], p[1], Base64.getDecoder().decode(p[2]))
        }
    }

    private fun log(event: String) {
        File("vault-access.log").appendText("${System.currentTimeMillis()} $event\n")
    }
}
