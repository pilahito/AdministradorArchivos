package com.cloudterm.core.ssh

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.LocalPortForwarder
import net.schmizz.sshj.connection.channel.direct.Parameters
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.sftp.SFTPClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import net.schmizz.sshj.userauth.keyprovider.KeyProvider
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Cliente SSH de alto nivel para CloudTerm Pro.
 *
 * Envuelve SSHJ: connect, startShell, exec, openSftp, createTunnel.
 */
class SshClient(
    private val config: SshConfig
) : AutoCloseable {

    private var ssh: SSHClient? = null

    val isConnected: Boolean
        get() = ssh?.isConnected == true

    fun connect() {
        if (isConnected) return
        val client = SSHClient()
        client.timeout = config.connectTimeoutMs
        if (config.promiscuousHostKey) {
            client.addHostKeyVerifier(PromiscuousVerifier())
        }
        client.connect(config.host, config.port)
        authenticate(client)
        ssh = client
    }

    private fun authenticate(client: SSHClient) {
        val keyPath = config.privateKeyPath
        when {
            !keyPath.isNullOrBlank() && File(keyPath).exists() -> {
                val keys: KeyProvider = if (config.privateKeyPassphrase.isNullOrBlank()) {
                    client.loadKeys(keyPath)
                } else {
                    client.loadKeys(keyPath, config.privateKeyPassphrase)
                }
                client.authPublickey(config.username, keys)
            }
            !config.password.isNullOrBlank() ->
                client.authPassword(config.username, config.password)
            else ->
                error("Falta contraseña o clave SSH privada")
        }
    }

    fun exec(command: String, timeoutSec: Long = 30): String {
        val client = requireClient()
        client.startSession().use { session ->
            val cmd = session.exec(command)
            val out = cmd.inputStream.bufferedReader().readText()
            val err = cmd.errorStream.bufferedReader().readText()
            cmd.join(timeoutSec, TimeUnit.SECONDS)
            return (out + err).trimEnd()
        }
    }

    fun startShell(): SshShell {
        val client = requireClient()
        val session = client.startSession()
        session.allocateDefaultPTY()
        val shell = session.startShell()
        return object : SshShell {
            override val input: InputStream get() = shell.inputStream
            override val output: OutputStream get() = shell.outputStream
            override val error: InputStream get() = shell.errorStream
            override fun resizePty(columns: Int, rows: Int) {
                // SSHJ Session no expone resize trivial en todas las versiones;
                // documentado para integración futura con NewTermux / emulador.
            }
            override fun close() {
                try { shell.close() } catch (_: Exception) {}
                try { session.close() } catch (_: Exception) {}
            }
        }
    }

    fun openSftp(): SftpSession {
        val client = requireClient()
        val sftp: SFTPClient = client.newSFTPClient()
        return SshjSftpSession(sftp)
    }

    fun createTunnel(
        localPort: Int,
        remoteHost: String,
        remotePort: Int
    ): SshTunnel {
        val client = requireClient()
        val params = Parameters("127.0.0.1", localPort, remoteHost, remotePort)
        val serverSocket = ServerSocket()
        serverSocket.reuseAddress = true
        serverSocket.bind(InetSocketAddress("127.0.0.1", localPort))
        val forwarder: LocalPortForwarder = client.newLocalPortForwarder(params, serverSocket)
        val open = AtomicBoolean(true)
        val thread = Thread({
            try {
                forwarder.listen()
            } catch (_: Exception) {
                // cerrado
            } finally {
                open.set(false)
            }
        }, "cloudterm-tunnel-$localPort").apply {
            isDaemon = true
            start()
        }
        return object : SshTunnel {
            override val localPort: Int = localPort
            override val remoteHost: String = remoteHost
            override val remotePort: Int = remotePort
            override val isOpen: Boolean get() = open.get()
            override fun close() {
                open.set(false)
                try { forwarder.close() } catch (_: Exception) {}
                try { serverSocket.close() } catch (_: Exception) {}
                try { thread.interrupt() } catch (_: Exception) {}
            }
        }
    }

    fun disconnect() {
        try { ssh?.disconnect() } catch (_: Exception) {}
        ssh = null
    }

    override fun close() = disconnect()

    private fun requireClient(): SSHClient =
        ssh?.takeIf { it.isConnected } ?: error("Sin conexión SSH — llama a connect() primero")
}

private class SshjSftpSession(
    private val sftp: SFTPClient
) : SftpSession {
    override fun ls(path: String): List<SftpEntry> =
        sftp.ls(path).map { f ->
            SftpEntry(
                name = f.name,
                path = f.path,
                isDirectory = f.isDirectory,
                size = f.attributes.size,
                modifiedEpochMs = f.attributes.mtime * 1000L
            )
        }

    override fun download(remotePath: String, localOut: OutputStream) {
        sftp.get(remotePath, localOut)
    }

    override fun upload(localIn: InputStream, remotePath: String) {
        sftp.put(localIn, remotePath)
    }

    override fun mkdir(path: String) {
        sftp.mkdir(path)
    }

    override fun rm(path: String) {
        sftp.rm(path)
    }

    override fun rmdir(path: String) {
        sftp.rmdir(path)
    }

    override fun rename(from: String, to: String) {
        sftp.rename(from, to)
    }

    override fun close() {
        try { sftp.close() } catch (_: Exception) {}
    }
}