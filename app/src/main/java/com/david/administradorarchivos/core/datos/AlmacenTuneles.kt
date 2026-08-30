package com.david.administradorarchivos.core.datos

import android.content.Context
import java.util.UUID

data class ReglaTunel(
    val id: String = UUID.randomUUID().toString(),
    val hostId: String,
    val nombre: String,
    val puertoLocal: Int,
    val destinoHost: String,
    val destinoPuerto: Int
)

class AlmacenTuneles(contexto: Context) {
    private val prefs = contexto.getSharedPreferences("tuneles_vault", Context.MODE_PRIVATE)

    fun listar(): List<ReglaTunel> {
        val raw = prefs.getString("lista", "") ?: return emptyList()
        if (raw.isBlank()) return emptyList()
        return raw.split("\n").mapNotNull { linea ->
            val p = linea.split("\t")
            if (p.size < 6) return@mapNotNull null
            ReglaTunel(
                id = p[0],
                hostId = p[1],
                nombre = p[2],
                puertoLocal = p[3].toIntOrNull() ?: 8080,
                destinoHost = p[4],
                destinoPuerto = p[5].toIntOrNull() ?: 80
            )
        }
    }

    fun guardar(regla: ReglaTunel) {
        escribir(listar().filter { it.id != regla.id } + regla)
    }

    fun borrar(id: String) {
        escribir(listar().filter { it.id != id })
    }

    private fun escribir(lista: List<ReglaTunel>) {
        val texto = lista.joinToString("\n") {
            listOf(
                it.id, it.hostId, it.nombre,
                it.puertoLocal.toString(), it.destinoHost, it.destinoPuerto.toString()
            ).joinToString("\t")
        }
        prefs.edit().putString("lista", texto).apply()
    }
}
