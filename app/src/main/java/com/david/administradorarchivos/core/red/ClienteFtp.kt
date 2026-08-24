package com.david.administradorarchivos.core.red

import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import org.apache.commons.net.ftp.FTPSClient
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

data class DatosConexionFtp(
    val host: String,
    val puerto: Int = 21,
    val usuario: String,
    val contrasena: String,
    val usarFtps: Boolean = false
)

/**
 * Envoltorio delgado sobre Apache Commons Net. IMPORTANTE: todas las
 * llamadas de esta clase son bloqueantes (red + E/S), así que siempre deben
 * invocarse desde una corrutina en Dispatchers.IO, nunca desde el hilo
 * principal de la interfaz.
 *
 * [Suposición] Los nombres de método de FTPClient/FTPSClient que uso aquí
 * (connect, login, changeWorkingDirectory, listFiles, retrieveFile,
 * storeFile, deleteFile, rename, logout, disconnect) corresponden a la API
 * pública estable de Apache Commons Net tal como la recuerdo, pero no la
 * he verificado línea por línea contra la documentación de la versión
 * 3.11.1 exacta. Antes de compilar, conviene contrastar la firma de cada
 * método con la documentación oficial: https://commons.apache.org/proper/commons-net/apidocs/
 */
class ClienteFtp(private val datos: DatosConexionFtp) {

    private val cliente: FTPClient = if (datos.usarFtps) FTPSClient() else FTPClient()

    fun conectar() {
        cliente.connect(datos.host, datos.puerto)
        val ingresoOk = cliente.login(datos.usuario, datos.contrasena)
        if (!ingresoOk) {
            throw SeguridadRedExcepcion("No se pudo iniciar sesión FTP: usuario o contraseña incorrectos")
        }
        cliente.enterLocalPassiveMode()
        cliente.setFileType(FTP.BINARY_FILE_TYPE)
    }

    fun listarArchivos(rutaRemota: String = "."): List<FTPFile> {
        return cliente.listFiles(rutaRemota).filterNotNull()
    }

    fun descargarArchivo(rutaRemota: String, destinoLocal: File) {
        destinoLocal.parentFile?.mkdirs()
        FileOutputStream(destinoLocal).use { salida ->
            val ok = cliente.retrieveFile(rutaRemota, salida)
            if (!ok) throw SeguridadRedExcepcion("No se pudo descargar '$rutaRemota'")
        }
    }

    fun subirArchivo(origenLocal: File, rutaRemota: String) {
        FileInputStream(origenLocal).use { entrada ->
            val ok = cliente.storeFile(rutaRemota, entrada)
            if (!ok) throw SeguridadRedExcepcion("No se pudo subir '${origenLocal.name}'")
        }
    }

    fun renombrar(rutaActual: String, rutaNueva: String) {
        val ok = cliente.rename(rutaActual, rutaNueva)
        if (!ok) throw SeguridadRedExcepcion("No se pudo renombrar '$rutaActual'")
    }

    fun borrar(rutaRemota: String) {
        val ok = cliente.deleteFile(rutaRemota)
        if (!ok) throw SeguridadRedExcepcion("No se pudo borrar '$rutaRemota'")
    }

    fun desconectar() {
        if (cliente.isConnected) {
            cliente.logout()
            cliente.disconnect()
        }
    }
}

class SeguridadRedExcepcion(mensaje: String) : Exception(mensaje)
