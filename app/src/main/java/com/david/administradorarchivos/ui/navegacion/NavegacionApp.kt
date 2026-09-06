package com.david.administradorarchivos.ui.navegacion

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.david.administradorarchivos.ui.pantallas.PantallaAjustes
import com.david.administradorarchivos.ui.pantallas.PantallaAyuda
import com.david.administradorarchivos.ui.pantallas.PantallaBoveda
import com.david.administradorarchivos.ui.pantallas.PantallaHistorial
import com.david.administradorarchivos.ui.pantallas.PantallaHosts
import com.david.administradorarchivos.ui.pantallas.PantallaKnownHosts
import com.david.administradorarchivos.ui.pantallas.PantallaSftp
import com.david.administradorarchivos.ui.pantallas.PantallaSnippets
import com.david.administradorarchivos.ui.pantallas.PantallaTemas
import com.david.administradorarchivos.ui.pantallas.PantallaTerminales
import com.david.administradorarchivos.ui.pantallas.PantallaTuneles
import com.david.administradorarchivos.ui.theme.AzulAccion
import com.david.administradorarchivos.ui.theme.FondoApp
import com.david.administradorarchivos.ui.theme.FondoBarra
import com.david.administradorarchivos.ui.theme.Idioma
import com.david.administradorarchivos.ui.theme.Linea
import com.david.administradorarchivos.ui.theme.Texto
import com.david.administradorarchivos.ui.theme.TextoSuave
import com.david.administradorarchivos.ui.theme.VerdeFab
import kotlinx.coroutines.launch

private data class DestinoDrawer(
    val ruta: String,
    val etiquetaEs: String,
    val etiquetaEn: String,
    val icono: ImageVector,
    val subtituloEs: String? = null,
    val subtituloEn: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavegacionPrincipal() {
    val nav = rememberNavController()
    val entrada by nav.currentBackStackEntryAsState()
    val ruta = entrada?.destination?.route
    val es = Idioma.espanol
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val alcance = rememberCoroutineScope()
    var solicitarNuevoHost by remember { mutableStateOf(false) }

    fun ir(dest: String) {
        nav.navigate(dest) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
        alcance.launch { drawer.close() }
    }

    // IA tipo drawer Android (CloudTerm Pro): Hosts → Ayuda
    val destinos = listOf(
        DestinoDrawer("hosts", "Hosts", "Hosts", Icons.Filled.Dns),
        DestinoDrawer("terminales", "Terminales", "Terminals", Icons.Filled.Terminal),
        DestinoDrawer("sftp", "SFTP", "SFTP", Icons.Filled.Folder),
        DestinoDrawer("reenvio", "Reenvío de puertos", "Port forwarding", Icons.Filled.SwapHoriz),
        DestinoDrawer("snippets", "Snippets", "Snippets", Icons.Filled.Code),
        DestinoDrawer("boveda", "Llavero / Bóveda", "Keychain / Vault", Icons.Filled.Key),
        DestinoDrawer("historial", "Historial", "History", Icons.Filled.History),
        DestinoDrawer("known_hosts", "Known hosts", "Known hosts", Icons.Filled.Fingerprint),
        DestinoDrawer("temas", "Temas", "Themes", Icons.Filled.Palette, "Oscuro", "Dark"),
        DestinoDrawer("ajustes", "Ajustes", "Settings", Icons.Filled.Settings),
        DestinoDrawer("ayuda", "Ayuda", "Help", Icons.Filled.HelpOutline)
    )

    val tituloRuta = destinos.firstOrNull { it.ruta == ruta }?.let {
        Idioma.t(it.etiquetaEs, it.etiquetaEn)
    } ?: "CloudTerm Pro"

    val mostrarFab = ruta in setOf("hosts", "sftp", "boveda", "snippets", "reenvio", "historial")

    ModalNavigationDrawer(
        drawerState = drawer,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = FondoApp) {
                Column(Modifier.fillMaxSize()) {
                Spacer(Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Filled.Shield,
                        contentDescription = null,
                        tint = VerdeFab,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "CloudTerm Pro",
                            style = MaterialTheme.typography.titleLarge,
                            color = AzulAccion,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            Idioma.t("SSH · SFTP · Túneles", "SSH · SFTP · Tunnels"),
                            color = TextoSuave,
                            fontSize = 12.sp
                        )
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = Linea
                )
                destinos.forEach { d ->
                    val seleccionado = ruta == d.ruta
                    NavigationDrawerItem(
                        label = {
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    Idioma.t(d.etiquetaEs, d.etiquetaEn),
                                    modifier = Modifier.weight(1f)
                                )
                                if (d.subtituloEs != null) {
                                    Text(
                                        Idioma.t(d.subtituloEs, d.subtituloEn ?: d.subtituloEs),
                                        color = TextoSuave,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        },
                        selected = seleccionado,
                        onClick = { ir(d.ruta) },
                        icon = {
                            Icon(
                                d.icono,
                                contentDescription = null,
                                tint = if (seleccionado) VerdeFab else Texto
                            )
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = VerdeFab.copy(alpha = 0.18f),
                            selectedTextColor = VerdeFab,
                            selectedIconColor = VerdeFab,
                            unselectedContainerColor = FondoApp,
                            unselectedTextColor = Texto,
                            unselectedIconColor = Texto
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    Idioma.t(
                        "CloudTerm Pro — sync, bóveda y reenvío de puertos",
                        "CloudTerm Pro — sync, vault and port forwarding"
                    ),
                    color = TextoSuave,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(16.dp)
                )
                }
            }
        }
    ) {
        Scaffold(
            containerColor = FondoApp,
            topBar = {
                if (ruta != "hosts" && ruta != "terminales") {
                    TopAppBar(
                        title = {
                            Text(
                                tituloRuta,
                                color = Texto,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { alcance.launch { drawer.open() } }) {
                                Icon(Icons.Filled.Menu, contentDescription = Idioma.t("Menú", "Menu"), tint = AzulAccion)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = FondoBarra,
                            titleContentColor = Texto
                        )
                    )
                }
            },
            floatingActionButton = {
                if (mostrarFab) {
                    FloatingActionButton(
                        onClick = {
                            when (ruta) {
                                "hosts" -> solicitarNuevoHost = true
                                else -> {
                                    solicitarNuevoHost = true
                                    ir("hosts")
                                }
                            }
                        },
                        containerColor = VerdeFab,
                        contentColor = FondoApp
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = Idioma.t("Añadir", "Add"))
                    }
                }
            }
        ) { pad ->
            NavHost(
                navController = nav,
                startDestination = "hosts",
                modifier = Modifier.padding(pad)
            ) {
                composable("hosts") {
                    PantallaHosts(
                        onAbrirTerminal = { nav.navigate("terminales") },
                        onAbrirMenu = { alcance.launch { drawer.open() } },
                        solicitarNuevo = solicitarNuevoHost,
                        onNuevoConsumido = { solicitarNuevoHost = false }
                    )
                }
                composable("terminales") { PantallaTerminales() }
                composable("sftp") { PantallaSftp() }
                composable("reenvio") { PantallaTuneles() }
                composable("snippets") { PantallaSnippets() }
                composable("boveda") { PantallaBoveda() }
                composable("historial") { PantallaHistorial() }
                composable("known_hosts") { PantallaKnownHosts() }
                composable("temas") { PantallaTemas() }
                composable("ajustes") { PantallaAjustes() }
                composable("ayuda") { PantallaAyuda() }
            }
        }
    }
    @Suppress("UNUSED_VARIABLE")
    val _recompone = es
}
