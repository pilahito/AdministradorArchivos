package com.david.administradorarchivos.ui.navegacion

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.david.administradorarchivos.ui.pantallas.PantallaAlmacenamiento
import com.david.administradorarchivos.ui.pantallas.PantallaConexiones
import com.david.administradorarchivos.ui.pantallas.PantallaEditorCodigo

sealed class Pestana(val ruta: String, val titulo: String) {
    object Almacenamiento : Pestana("almacenamiento", "Archivos")
    object Conexiones : Pestana("conexiones", "Hosts")
    object Editor : Pestana("editor", "Editor")
}

private val PESTANAS = listOf(Pestana.Almacenamiento, Pestana.Conexiones, Pestana.Editor)

@Composable
fun NavegacionPrincipal() {
    val controladorNav: NavHostController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val entradaActual by controladorNav.currentBackStackEntryAsState()
                val rutaActual = entradaActual?.destination?.route

                PESTANAS.forEach { pestana ->
                    val icono = when (pestana) {
                        Pestana.Almacenamiento -> Icons.Filled.Folder
                        Pestana.Conexiones -> Icons.Filled.Wifi
                        Pestana.Editor -> Icons.Filled.Code
                    }
                    NavigationBarItem(
                        selected = rutaActual == pestana.ruta,
                        onClick = {
                            controladorNav.navigate(pestana.ruta) {
                                popUpTo(controladorNav.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(icono, contentDescription = pestana.titulo) },
                        label = { Text(pestana.titulo) }
                    )
                }
            }
        }
    ) { paddingInterno ->
        NavHost(
            navController = controladorNav,
            startDestination = Pestana.Conexiones.ruta,
            modifier = Modifier.padding(paddingInterno)
        ) {
            composable(Pestana.Almacenamiento.ruta) { PantallaAlmacenamiento() }
            composable(Pestana.Conexiones.ruta) { PantallaConexiones() }
            composable(Pestana.Editor.ruta) { PantallaEditorCodigo() }
        }
    }
}
