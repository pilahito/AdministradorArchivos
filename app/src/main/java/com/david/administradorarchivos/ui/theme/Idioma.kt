package com.david.administradorarchivos.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object Idioma {
    var espanol by mutableStateOf(true)
        private set

    fun cargar(ctx: Context) {
        espanol = ctx.getSharedPreferences("ajustes", Context.MODE_PRIVATE)
            .getBoolean("espanol", true)
    }

    fun cambiar(ctx: Context, usarEspanol: Boolean) {
        espanol = usarEspanol
        ctx.getSharedPreferences("ajustes", Context.MODE_PRIVATE)
            .edit().putBoolean("espanol", usarEspanol).apply()
    }

    fun t(es: String, en: String): String = if (espanol) es else en
}
