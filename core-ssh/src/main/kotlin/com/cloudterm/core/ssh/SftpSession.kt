package com.cloudterm.core.ssh

import java.io.InputStream
import java.io.OutputStream

data class SftpEntry(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val modifiedEpochMs: Long?
)

/**
 * Fachada SFTP mínima sobre SSHJ.
 */
interface SftpSession : AutoCloseable {
    fun ls(path: String): List<SftpEntry>
    fun download(remotePath: String, localOut: OutputStream)
    fun upload(localIn: InputStream, remotePath: String)
    fun mkdir(path: String)
    fun rm(path: String)
    fun rmdir(path: String)
    fun rename(from: String, to: String)
}