package com.david.administradorarchivos.core.red

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import java.io.File

data class DatosConexionSsh(
    val host: String,
    val puerto: Int = 22,
    val usuario: String,
    val contrasena: String? = null,
    val rutaClavePrivada: String? = null // ruta local a un archivo de clave SSH importado
)

/**
 * Cliente SFTP + apertura de sesión SSH sobre el fork de JSch mantenido en
 * com.github.mwiede:jsch (el com.jcraft:jsch original ya no recibe
 * mantenimiento). La API pública (JSch, Session, ChannelSftp) es la misma
 * en ambos, así que el código de esta clase es compatible con cualquiera
 * de los dos artefactos.
 *
 * [Suposición] Igual que en ClienteFtp: los nombres de método que uso aquí
 * (getSession, setPassword, addIdentity, connect, openChannel, ls, get,
 * put, rm, rename) son los que recuerdo de la API de JSch, pero no los he
 * verificado contra la documentación exacta del fork mwiede. Antes de
 * compilar, contrasta contra https://github.com/mwiede/jsch — especialmente
 * la gestión de "known_hosts" y StrictHostKeyChecking, que aquí desactivo
 * de forma explícita solo para simplificar el ejemplo (ver nota de
 * seguridad más abajo).
 */
class ClienteSftp(private val datos: DatosConexionSsh) {

    private lateinit var sesion: Session
    private lateinit var canalSftp: ChannelSftp

    fun conectar() {
        val jsch = JSch()
        datos.rutaClavePrivada?.let { ruta ->
            jsch.addIdentity(ruta)
        }

        sesion = jsch.getSession(datos.usuario, datos.host, datos.puerto)
        datos.contrasena?.let { sesion.setPassword(it) }

        // ADVERTENCIA DE SEGURIDAD: StrictHostKeyChecking en "no" acepta
        // cualquier clave de host sin verificarla, lo que expone a ataques
        // de intermediario (MITM). Está así solo para que el ejemplo
        // conecte sin fricción; en una versión real deberías cargar un
        // archivo known_hosts (jsch.setKnownHosts(...)) y dejar la
        // verificación activada.
        sesion.setConfig("StrictHostKeyChecking", "no")
        sesion.connect(15_000)

        val canal = sesion.openChannel("sftp")
        canal.connect(15_000)
        canalSftp = canal as ChannelSftp
    }

    fun listarArchivos(rutaRemota: String = "."): List<ChannelSftp.LsEntry> {
        @Suppress("UNCHECKED_CAST")
        return (canalSftp.ls(rutaRemota) as java.util.Vector<ChannelSftp.LsEntry>).toList()
    }

    fun descargarArchivo(rutaRemota: String, destinoLocal: File) {
        destinoLocal.parentFile?.mkdirs()
        canalSftp.get(rutaRemota, destinoLocal.absolutePath)
    }

    fun subirArchivo(origenLocal: File, rutaRemota: String) {
        canalSftp.put(origenLocal.absolutePath, rutaRemota)
    }

    fun renombrar(rutaActual: String, rutaNueva: String) {
        canalSftp.rename(rutaActual, rutaNueva)
    }

    fun borrar(rutaRemota: String) {
        canalSftp.rm(rutaRemota)
    }

    fun desconectar() {
        if (this::canalSftp.isInitialized) canalSftp.disconnect()
        if (this::sesion.isInitialized) sesion.disconnect()
    }
}
