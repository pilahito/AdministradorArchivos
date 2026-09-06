package com.cloudterm.sync.stubs

import com.cloudterm.sync.RemoteEntry
import com.cloudterm.sync.SyncProvider

/**
 * Base para proveedores aún no implementados.
 */
abstract class StubSyncProvider(
    override val id: String,
    override val displayName: String
) : SyncProvider {
    private fun notReady(): Nothing =
        throw UnsupportedOperationException("$displayName: stub — pendiente de SDK / OAuth")

    override suspend fun list(path: String): List<RemoteEntry> = notReady()
    override suspend fun download(remotePath: String): ByteArray = notReady()
    override suspend fun upload(remotePath: String, data: ByteArray) = notReady()
    override suspend fun delete(remotePath: String) = notReady()
    override suspend fun mkdir(remotePath: String) = notReady()
}

/** MEGA — stub (SDK oficial o mega.java en roadmap). */
class MegaStubProvider : StubSyncProvider("mega", "MEGA")

/** Google Drive — stub (OAuth ya existe parcialmente en :app). */
class GoogleDriveStubProvider : StubSyncProvider("gdrive", "Google Drive")

/** Dropbox — stub. */
class DropboxStubProvider : StubSyncProvider("dropbox", "Dropbox")

/** Microsoft OneDrive — stub. */
class OneDriveStubProvider : StubSyncProvider("onedrive", "OneDrive")

/** TeraBox — stub. */
class TeraBoxStubProvider : StubSyncProvider("terabox", "TeraBox")

/** 1fichier — stub. */
class OneFichierStubProvider : StubSyncProvider("1fichier", "1fichier")