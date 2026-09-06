package com.david.administradorarchivos.core.red

import com.david.administradorarchivos.core.datos.HostGuardado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import net.schmizz.sshj.connection.channel.direct.Session
import java.io.InputStream
import java.io.OutputStream

object GestorSesion {
    var clienteSftp: ClienteSftp? = null
        private set

    private var clienteSshj: ClienteSshj? = null
    private var shellSshj: Session.Shell? = null

    private val _hostActivo = MutableStateFlow<HostGuardado?>(null)
    val hostActivo: StateFlow<HostGuardado?> = _hostActivo

    private val _estado = MutableStateFlow("Sin conexión")
    val estado: StateFlow<String> = _estado

    fun conectar(host: HostGuardado) {
        desconectar()
        val cliente = ClienteSftp(
            DatosConexionSsh(
                host = host.direccion,
                puerto = host.puerto,
                usuario = host.usuario,
                contrasena = host.contrasena.ifBlank { null },
                rutaClavePrivada = host.rutaClave.ifBlank { null }
            )
        )
        cliente.conectar()
        clienteSftp = cliente

        // Shell PTY interactivo (SSHJ) para TerminalView / Termux emulator
        try {
            val ssh = ClienteSshj(
                host = host.direccion,
                puerto = host.puerto,
                usuario = host.usuario,
                contrasena = host.contrasena.ifBlank { null },
                rutaClave = host.rutaClave.ifBlank { null }
            )
            ssh.conectar()
            shellSshj = ssh.abrirShell()
            clienteSshj = ssh
        } catch (e: Exception) {
            // SFTP puede funcionar aunque el shell PTY falle; la UI mostrará el error.
            _estado.value = "SFTP OK · Shell: ${e.message}"
            clienteSshj = null
            shellSshj = null
        }

        _hostActivo.value = host
        if (shellSshj != null) {
            _estado.value = "Conectado · ${host.usuario}@${host.direccion}:${host.puerto}"
        }
    }

    /** Streams del PTY SSH para el emulador Termux. */
    fun shellInput(): InputStream? = shellSshj?.inputStream
    fun shellOutput(): OutputStream? = shellSshj?.outputStream

    fun resizeShell(columns: Int, rows: Int) {
        try {
            clienteSshj?.resizePty(columns, rows)
        } catch (_: Exception) {
        }
    }

    fun desconectar() {
        try { shellSshj?.close() } catch (_: Exception) {}
        shellSshj = null
        try { clienteSshj?.desconectar() } catch (_: Exception) {}
        clienteSshj = null
        try { clienteSftp?.desconectar() } catch (_: Exception) {}
        clienteSftp = null
        _hostActivo.value = null
        _estado.value = "Sin conexión"
    }
}
