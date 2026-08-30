package com.david.administradorarchivos.core.datos

import android.content.Context
import java.util.UUID

data class HostGuardado(
    val id: String = UUID.randomUUID().toString(),
    val alias: String,
    val direccion: String,
    val puerto: Int = 22,
    val usuario: String,
    val contrasena: String = "",
    val rutaClave: String = "",
    val protocolo: String = "SSH",
    val etiquetas: String = "ssh",
    val color: Int = 0
)

class AlmacenHosts(contexto: Context) {
    private val prefs = contexto.getSharedPreferences("hosts_vault", Context.MODE_PRIVATE)

    fun listar(): List<HostGuardado> {
        val raw = prefs.getString("lista", "") ?: return emptyList()
        if (raw.isBlank()) return emptyList()
        return raw.split("\n").mapNotNull { linea ->
            val p = linea.split("\t")
            if (p.size < 8) return@mapNotNull null
            HostGuardado(
                id = p[0],
                alias = p[1],
                direccion = p[2],
                puerto = p[3].toIntOrNull() ?: 22,
                usuario = p[4],
                contrasena = p[5],
                rutaClave = p[6],
                protocolo = p[7],
                etiquetas = p.getOrNull(8) ?: "ssh",
                color = p.getOrNull(9)?.toIntOrNull() ?: 0
            )
        }
    }

    fun guardar(host: HostGuardado) {
        val actual = listar().filter { it.id != host.id } + host
        escribir(actual)
    }

    fun borrar(id: String) {
        escribir(listar().filter { it.id != id })
    }

    private fun escribir(lista: List<HostGuardado>) {
        val texto = lista.joinToString("\n") {
            listOf(
                it.id, it.alias, it.direccion, it.puerto.toString(),
                it.usuario, it.contrasena, it.rutaClave, it.protocolo,
                it.etiquetas, it.color.toString()
            ).joinToString("\t")
        }
        prefs.edit().putString("lista", texto).apply()
    }
}
