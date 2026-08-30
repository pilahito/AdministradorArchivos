package com.david.administradorarchivos.core.red

import com.david.administradorarchivos.core.datos.HostGuardado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object GestorSesion {
    var clienteSftp: ClienteSftp? = null
        private set

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
        _hostActivo.value = host
        _estado.value = "Conectado · ${host.usuario}@${host.direccion}:${host.puerto}"
    }

    fun desconectar() {
        try { clienteSftp?.desconectar() } catch (_: Exception) {}
        clienteSftp = null
        _hostActivo.value = null
        _estado.value = "Sin conexión"
    }
}
