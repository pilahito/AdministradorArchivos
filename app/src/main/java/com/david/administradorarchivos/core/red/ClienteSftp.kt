package com.david.administradorarchivos.core.red

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import java.io.ByteArrayOutputStream
import java.io.File

data class DatosConexionSsh(
    val host: String,
    val puerto: Int = 22,
    val usuario: String,
    val contrasena: String? = null,
    val rutaClavePrivada: String? = null
)

class ClienteSftp(private val datos: DatosConexionSsh) {

    private lateinit var sesion: Session
    private var canalSftp: ChannelSftp? = null

    fun conectar() {
        val jsch = JSch()
        datos.rutaClavePrivada?.let { ruta ->
            val archivo = File(ruta)
            if (!archivo.exists()) {
                throw SeguridadRedExcepcion("No se encontró la clave privada: $ruta")
            }
            jsch.addIdentity(ruta)
        }

        sesion = jsch.getSession(datos.usuario, datos.host, datos.puerto)
        datos.contrasena?.let { sesion.setPassword(it) }
        sesion.setConfig("StrictHostKeyChecking", "no")
        sesion.setConfig("PreferredAuthentications", "publickey,password,keyboard-interactive")
        sesion.connect(15_000)

        val canal = sesion.openChannel("sftp")
        canal.connect(15_000)
        canalSftp = canal as ChannelSftp
    }

    fun listarArchivos(rutaRemota: String = "."): List<ChannelSftp.LsEntry> {
        val canal = canalSftp ?: throw SeguridadRedExcepcion("SFTP no está conectado")
        @Suppress("UNCHECKED_CAST")
        return (canal.ls(rutaRemota) as java.util.Vector<ChannelSftp.LsEntry>)
            .toList()
            .filter { it.filename != "." && it.filename != ".." }
    }

    fun ejecutarComando(comando: String): String {
        if (!this::sesion.isInitialized || !sesion.isConnected) {
            throw SeguridadRedExcepcion("No hay sesión SSH activa")
        }
        val canal = sesion.openChannel("exec") as ChannelExec
        canal.setCommand(comando)
        val salida = ByteArrayOutputStream()
        val errores = ByteArrayOutputStream()
        canal.outputStream = salida
        canal.setErrStream(errores)
        canal.connect(10_000)
        val inicio = System.currentTimeMillis()
        while (!canal.isClosed) {
            if (System.currentTimeMillis() - inicio > 20_000) break
            Thread.sleep(80)
        }
        canal.disconnect()
        val texto = (salida.toString("UTF-8") + errores.toString("UTF-8")).trim()
        return texto.ifBlank { "(sin salida)" }
    }

    fun desconectar() {
        try { canalSftp?.disconnect() } catch (_: Exception) {}
        try { if (this::sesion.isInitialized) sesion.disconnect() } catch (_: Exception) {}
        canalSftp = null
    }
}
