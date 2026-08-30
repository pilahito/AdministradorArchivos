package com.david.administradorarchivos.core.datos

import android.content.Context
import java.util.UUID

data class ClaveSsh(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val ruta: String
)

class AlmacenClaves(contexto: Context) {
    private val prefs = contexto.getSharedPreferences("llavero_ssh", Context.MODE_PRIVATE)

    fun listar(): List<ClaveSsh> {
        val raw = prefs.getString("lista", "") ?: return emptyList()
        if (raw.isBlank()) return emptyList()
        return raw.split("\n").mapNotNull { linea ->
            val p = linea.split("\t")
            if (p.size < 3) null else ClaveSsh(p[0], p[1], p[2])
        }
    }

    fun guardar(clave: ClaveSsh) {
        escribir(listar().filter { it.id != clave.id } + clave)
    }

    fun borrar(id: String) {
        escribir(listar().filter { it.id != id })
    }

    private fun escribir(lista: List<ClaveSsh>) {
        prefs.edit().putString(
            "lista",
            lista.joinToString("\n") { "${it.id}\t${it.nombre}\t${it.ruta}" }
        ).apply()
    }
}
