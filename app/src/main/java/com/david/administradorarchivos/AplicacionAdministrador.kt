package com.david.administradorarchivos

import android.app.Application
import com.david.administradorarchivos.ui.theme.Idioma

class AplicacionAdministrador : Application() {
    override fun onCreate() {
        super.onCreate()
        Idioma.cargar(this)
    }
}
