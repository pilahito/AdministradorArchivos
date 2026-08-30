package com.david.administradorarchivos.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.david.administradorarchivos.core.red.GestorSesion
import com.david.administradorarchivos.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PantallaSftp() {
    val host by GestorSesion.hostActivo.collectAsState()
    var ruta by remember { mutableStateOf(".") }
    var nombres by remember { mutableStateOf<List<Pair<String, Boolean>>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    val alcance = rememberCoroutineScope()

    fun cargar(destino: String) {
        alcance.launch {
            try {
                val lista = withContext(Dispatchers.IO) {
                    GestorSesion.clienteSftp?.listarArchivos(destino)?.map {
                        it.filename to it.attrs.isDir
                    } ?: emptyList()
                }
                nombres = lista.sortedWith(
                    compareByDescending<Pair<String, Boolean>> { it.second }.thenBy { it.first.lowercase() }
                )
                ruta = destino
                error = null
            } catch (e: Exception) {
                error = e.message
            }
        }
    }

    LaunchedEffect(host) {
        if (host != null) cargar(".") else nombres = emptyList()
    }

    Column(Modifier.fillMaxSize().background(FondoApp).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("SFTP", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Texto)
                Text(
                    if (host != null) ruta else Idioma.t("Conecta una sesión primero", "Connect a session first"),
                    color = TextoSuave
                )
            }
            IconButton(onClick = { if (host != null) cargar(ruta) }) {
                Icon(Icons.Filled.Refresh, contentDescription = Idioma.t("Refrescar", "Refresh"), tint = AzulAccion)
            }
        }
        Spacer(Modifier.height(12.dp))
        error?.let { Text(it, color = Rojo) }

        if (host == null) {
            Text(
                Idioma.t(
                    "Ve a Sesiones, crea una y tócala. Luego vuelve aquí para ver los archivos.",
                    "Go to Sessions, create one and tap it. Then come back to browse files."
                ),
                color = TextoSuave
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(nombres) { (nombre, esDir) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(FondoTarjeta)
                            .clickable(enabled = esDir) {
                                val nueva = if (ruta == "." || ruta.isBlank()) nombre else "$ruta/$nombre"
                                cargar(nueva)
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (esDir) Icons.Filled.Folder else Icons.Filled.InsertDriveFile,
                            null,
                            tint = if (esDir) AzulAccion else TextoSuave
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(nombre, color = Texto)
                    }
                }
            }
        }
    }
}
