package com.david.administradorarchivos.core.red

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.LocalPortForwarder
import net.schmizz.sshj.connection.channel.direct.Parameters
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

data class EstadoTunel(
    val id: String,
    val etiqueta: String,
    val activo: Boolean,
    val error: String? = null
)

object GestorTuneles {
    private val _lista = MutableStateFlow<List<EstadoTunel>>(emptyList())
    val lista: StateFlow<List<EstadoTunel>> = _lista
    private val vivos = ConcurrentHashMap<String, Pair<SSHClient, ServerSocket>>()

    fun arrancarLocal(
        id: String,
        host: String,
        puertoSsh: Int,
        usuario: String,
        contrasena: String?,
        rutaClave: String?,
        puertoLocal: Int,
        destinoHost: String,
        destinoPuerto: Int
    ) {
        parar(id)
        thread(name = "tunel-$id", isDaemon = true) {
            try {
                val ssh = SSHClient()
                ssh.addHostKeyVerifier(PromiscuousVerifier())
                ssh.connect(host, puertoSsh)
                when {
                    !rutaClave.isNullOrBlank() -> ssh.authPublickey(usuario, rutaClave)
                    !contrasena.isNullOrBlank() -> ssh.authPassword(usuario, contrasena)
                    else -> error("Sin credenciales")
                }
                val server = ServerSocket()
                server.reuseAddress = true
                server.bind(InetSocketAddress("127.0.0.1", puertoLocal))
                val params = Parameters("127.0.0.1", puertoLocal, destinoHost, destinoPuerto)
                val forwarder: LocalPortForwarder = ssh.newLocalPortForwarder(params, server)
                vivos[id] = ssh to server
                publicar(id, "L $puertoLocal → $destinoHost:$destinoPuerto", true, null)
                forwarder.listen()
            } catch (e: Exception) {
                publicar(id, "L $puertoLocal", false, e.message)
            }
        }
    }

    fun parar(id: String) {
        vivos.remove(id)?.let { (ssh, sock) ->
            try { sock.close() } catch (_: Exception) {}
            try { ssh.disconnect() } catch (_: Exception) {}
        }
        _lista.value = _lista.value.filterNot { it.id == id }
    }

    fun pararTodos() {
        vivos.keys.toList().forEach { parar(it) }
    }

    private fun publicar(id: String, etiqueta: String, activo: Boolean, error: String?) {
        val resto = _lista.value.filterNot { it.id == id }
        _lista.value = resto + EstadoTunel(id, etiqueta, activo, error)
    }
}
