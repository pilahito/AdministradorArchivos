package com.david.administradorarchivos.ui.navegacion

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.david.administradorarchivos.ui.pantallas.PantallaAjustes
import com.david.administradorarchivos.ui.pantallas.PantallaHosts
import com.david.administradorarchivos.ui.pantallas.PantallaSftp
import com.david.administradorarchivos.ui.pantallas.PantallaTerminales

private data class Destino(val ruta: String, val titulo: String)

private val DESTINOS = listOf(
    Destino("hosts", "Hosts"),
    Destino("terminales", "Terminal"),
    Destino("sftp", "SFTP"),
    Destino("ajustes", "Ajustes")
)

@Composable
fun NavegacionPrincipal() {
    val nav = rememberNavController()
    val entrada by nav.currentBackStackEntryAsState()
    val ruta = entrada?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                DESTINOS.forEach { d ->
                    val icono = when (d.ruta) {
                        "hosts" -> Icons.Filled.Dns
                        "terminales" -> Icons.Filled.Terminal
                        "sftp" -> Icons.Filled.Folder
                        else -> Icons.Filled.Settings
                    }
                    NavigationBarItem(
                        selected = ruta == d.ruta,
                        onClick = {
                            nav.navigate(d.ruta) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(icono, contentDescription = d.titulo) },
                        label = { Text(d.titulo) }
                    )
                }
            }
        }
    ) { pad ->
        NavHost(
            navController = nav,
            startDestination = "hosts",
            modifier = Modifier.padding(pad)
        ) {
            composable("hosts") { PantallaHosts(onAbrirTerminal = { nav.navigate("terminales") }) }
            composable("terminales") { PantallaTerminales() }
            composable("sftp") { PantallaSftp() }
            composable("ajustes") { PantallaAjustes() }
        }
    }
}
