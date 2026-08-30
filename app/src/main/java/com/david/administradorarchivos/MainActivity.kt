package com.david.administradorarchivos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.david.administradorarchivos.ui.navegacion.NavegacionPrincipal
import com.david.administradorarchivos.ui.theme.TemaAdministradorArchivos

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaAdministradorArchivos {
                NavegacionPrincipal()
            }
        }
    }
}
