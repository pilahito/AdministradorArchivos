package com.david.administradorarchivos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.david.administradorarchivos.core.permisos.GestorPermisos
import com.david.administradorarchivos.ui.navegacion.NavegacionPrincipal
import com.david.administradorarchivos.ui.theme.TemaAdministradorArchivos

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaAdministradorArchivos {
                PantallaConPermisos()
            }
        }
    }
}

/**
 * Envuelve la navegación principal y, si el permiso de acceso completo al
 * almacenamiento aún no fue concedido, muestra primero un diálogo en
 * español explicando por qué se necesita antes de mandar al usuario a la
 * pantalla de ajustes del sistema.
 */
@Composable
private fun PantallaConPermisos() {
    val contexto = LocalContext.current
    var mostrarDialogo by remember { mutableStateOf(!GestorPermisos.tieneAccesoCompleto(contexto)) }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { /* obligatorio: no se cierra tocando fuera */ },
            title = { Text("Permiso de almacenamiento necesario") },
            text = {
                Text(
                    "Para explorar, comprimir y transferir tus archivos, " +
                    "Administrador de Archivos Avanzado necesita acceso completo " +
                    "al almacenamiento del dispositivo. En la siguiente pantalla, " +
                    "activa el permiso para esta aplicación."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    contexto.startActivity(GestorPermisos.intentSolicitarAccesoCompleto(contexto))
                    mostrarDialogo = false
                }) { Text("Ir a Ajustes") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) { Text("Ahora no") }
            }
        )
    }

    NavegacionPrincipal()
}
