package com.david.administradorarchivos.ui.navegacion

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.david.administradorarchivos.ui.pantallas.PantallaAjustes
import com.david.administradorarchivos.ui.pantallas.PantallaHosts
import com.david.administradorarchivos.ui.pantallas.PantallaSftp
import com.david.administradorarchivos.ui.pantallas.PantallaSnippets
import com.david.administradorarchivos.ui.pantallas.PantallaTerminales
import com.david.administradorarchivos.ui.pantallas.PantallaTuneles
import com.david.administradorarchivos.ui.theme.FondoApp
import com.david.administradorarchivos.ui.theme.Idioma
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavegacionPrincipal() {
    val nav = rememberNavController()
    val entrada by nav.currentBackStackEntryAsState()
    val ruta = entrada?.destination?.route
    val es = Idioma.espanol
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val alcance = rememberCoroutineScope()

    fun ir(dest: String) {
        nav.navigate(dest) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
        alcance.launch { drawer.close() }
    }

    val destinosBarra = listOf(
        Triple("hosts", Idioma.t("Sesiones", "Sessions"), Icons.Filled.Dns),
        Triple("terminales", Idioma.t("Terminal", "Terminal"), Icons.Filled.Terminal),
        Triple("sftp", "SFTP", Icons.Filled.Folder),
        Triple("ajustes", Idioma.t("Ajustes", "Settings"), Icons.Filled.Settings)
    )

    ModalNavigationDrawer(
        drawerState = drawer,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = FondoApp) {
                Spacer(Modifier.height(18.dp))
                Text("Sesiones", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
                NavigationDrawerItem(
                    label = { Text(Idioma.t("Sesiones", "Sessions")) },
                    selected = ruta == "hosts",
                    onClick = { ir("hosts") },
                    icon = { Icon(Icons.Filled.Dns, null) }
                )
                NavigationDrawerItem(
                    label = { Text("Terminal") },
                    selected = ruta == "terminales",
                    onClick = { ir("terminales") },
                    icon = { Icon(Icons.Filled.Terminal, null) }
                )
                NavigationDrawerItem(
                    label = { Text("SFTP") },
                    selected = ruta == "sftp",
                    onClick = { ir("sftp") },
                    icon = { Icon(Icons.Filled.Folder, null) }
                )
                NavigationDrawerItem(
                    label = { Text("Snippets") },
                    selected = ruta == "snippets",
                    onClick = { ir("snippets") },
                    icon = { Icon(Icons.Filled.Code, null) }
                )
                NavigationDrawerItem(
                    label = { Text(Idioma.t("Túneles", "Tunnels")) },
                    selected = ruta == "tuneles",
                    onClick = { ir("tuneles") },
                    icon = { Icon(Icons.Filled.SwapHoriz, null) }
                )
                NavigationDrawerItem(
                    label = { Text(Idioma.t("Ajustes", "Settings")) },
                    selected = ruta == "ajustes",
                    onClick = { ir("ajustes") },
                    icon = { Icon(Icons.Filled.Settings, null) }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Sesiones") },
                    navigationIcon = {
                        IconButton(onClick = { alcance.launch { drawer.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menú")
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    destinosBarra.forEach { d ->
                        NavigationBarItem(
                            selected = ruta == d.first,
                            onClick = { ir(d.first) },
                            icon = { Icon(d.third, contentDescription = d.second) },
                            label = { Text(d.second) }
                        )
                    }
                }
            }
        ) { pad ->
            NavHost(navController = nav, startDestination = "hosts", modifier = Modifier.padding(pad)) {
                composable("hosts") { PantallaHosts(onAbrirTerminal = { nav.navigate("terminales") }) }
                composable("terminales") { PantallaTerminales() }
                composable("sftp") { PantallaSftp() }
                composable("snippets") { PantallaSnippets() }
                composable("tuneles") { PantallaTuneles() }
                composable("ajustes") { PantallaAjustes() }
            }
        }
    }
    @Suppress("UNUSED_VARIABLE")
    val _recompone = es
}
