package com.cloudterm.sync

/**
 * Contrato comun para proveedores de nube (MEGA, Drive, Dropbox, OneDrive, TeraBox, 1fichier, WebDAV).
 */
data class RemoteEntry(
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val size: Long = 0L,
    val etag: String? = null
)

interface SyncProvider {
    val id: String
    val displayName: String

    suspend fun list(path: String): List<RemoteEntry>
    suspend fun download(remotePath: String): ByteArray
    suspend fun upload(remotePath: String, data: ByteArray)
    suspend fun delete(remotePath: String)
    suspend fun mkdir(remotePath: String)
}
