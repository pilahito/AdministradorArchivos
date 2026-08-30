package com.david.administradorarchivos.core.red

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import java.io.File
import java.util.concurrent.TimeUnit

class ClienteSshj(
    private val host: String,
    private val puerto: Int,
    private val usuario: String,
    private val contrasena: String? = null,
    private val rutaClave: String? = null
) {
    private var cliente: SSHClient? = null
    private var sesion: Session? = null
    private var shell: Session.Shell? = null

    fun conectar() {
        val ssh = SSHClient()
        ssh.addHostKeyVerifier(PromiscuousVerifier())
        ssh.connect(host, puerto)
        when {
            !rutaClave.isNullOrBlank() && File(rutaClave).exists() ->
                ssh.authPublickey(usuario, rutaClave)
            !contrasena.isNullOrBlank() ->
                ssh.authPassword(usuario, contrasena)
            else ->
                error("Falta contraseña o clave SSH")
        }
        cliente = ssh
    }

    fun ejecutar(comando: String, timeoutSeg: Long = 30): String {
        val ssh = cliente ?: error("Sin conexión SSH")
        ssh.startSession().use { s ->
            val cmd = s.exec(comando)
            val salida = cmd.inputStream.bufferedReader().readText()
            val err = cmd.errorStream.bufferedReader().readText()
            cmd.join(timeoutSeg, TimeUnit.SECONDS)
            return (salida + err).trimEnd()
        }
    }

    fun abrirShell(): Session.Shell {
        val ssh = cliente ?: error("Sin conexión SSH")
        val s = ssh.startSession()
        s.allocateDefaultPTY()
        sesion = s
        shell = s.startShell()
        return shell!!
    }

    fun desconectar() {
        try { shell?.close() } catch (_: Exception) {}
        try { sesion?.close() } catch (_: Exception) {}
        try { cliente?.disconnect() } catch (_: Exception) {}
        shell = null
        sesion = null
        cliente = null
    }
}
