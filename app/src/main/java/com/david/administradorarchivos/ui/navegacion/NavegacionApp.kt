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
import com.david.administradorarchivos.ui.theme.Idioma

@Composable
fun NavegacionPrincipal() {
    val nav = rememberNavController()
    val entrada by nav.currentBackStackEntryAsState()
    val ruta = entrada?.destination?.route
    val es = Idioma.espanol

    val destinos = listOf(
        Triple("hosts", Idioma.t("Sesiones", "Sessions"), Icons.Filled.Dns),
        Triple("terminales", Idioma.t("Terminal", "Terminal"), Icons.Filled.Terminal),
        Triple("sftp", "SFTP", Icons.Filled.Folder),
        Triple("ajustes", Idioma.t("Ajustes", "Settings"), Icons.Filled.Settings)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                destinos.forEach { d ->
                    NavigationBarItem(
                        selected = ruta == d.first,
                        onClick = {
                            nav.navigate(d.first) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(d.third, contentDescription = d.second) },
                        label = { Text(d.second) }
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
    // lee `es` para recomponer al cambiar idioma
    @Suppress("UNUSED_VARIABLE")
    val _recompone = es
}
