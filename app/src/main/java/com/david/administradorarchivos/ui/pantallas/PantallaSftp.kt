package com.david.administradorarchivos.ui.pantallas

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.david.administradorarchivos.core.red.GestorSesion
import com.david.administradorarchivos.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class TransferenciaDemo(
    val nombre: String,
    val progreso: Float,
    val velocidad: String
)

@Composable
fun PantallaSftp() {
    val host by GestorSesion.hostActivo.collectAsState()
    var ruta by remember { mutableStateOf("/") }
    var nombres by remember { mutableStateOf<List<Pair<String, Boolean>>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    val alcance = rememberCoroutineScope()
    val transferencias = remember {
        listOf(
            TransferenciaDemo("respaldo_db.tar.gz", 0.75f, "75.38 MB/s"),
            TransferenciaDemo("config.yml", 0.42f, "12.1 MB/s")
        )
    }

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

    Column(Modifier.fillMaxSize().background(FondoApp).padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("SFTP", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Texto)
                Text(
                    if (host != null) ruta else Idioma.t("Conecta un host primero", "Connect a host first"),
                    color = TextoSuave,
                    fontSize = 13.sp
                )
            }
            IconButton(onClick = { if (host != null) cargar(ruta) }) {
                Icon(Icons.Filled.Refresh, contentDescription = Idioma.t("Refrescar", "Refresh"), tint = AzulAccion)
            }
        }
        Spacer(Modifier.height(10.dp))
        error?.let { Text(it, color = Rojo) }

        if (host == null) {
            Text(
                Idioma.t(
                    "Ve a Hosts, crea uno y conéctalo. Luego vuelve aquí para ver los archivos.",
                    "Go to Hosts, create one and connect. Then come back to browse files."
                ),
                color = TextoSuave
            )
        } else {
            Row(
                Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Panel remoto
                Card(
                    modifier = Modifier.weight(1.15f).fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Linea)
                ) {
                    Column(Modifier.padding(10.dp)) {
                        Text(
                            Idioma.t("Remote File", "Remote File"),
                            color = AzulAccion,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            color = FondoBarra,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Linea)
                        ) {
                            Text(
                                ruta,
                                color = TextoSuave,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp).fillMaxWidth()
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(nombres) { (nombre, esDir) ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(FondoBarra)
                                        .clickable(enabled = esDir) {
                                            val nueva = when {
                                                ruta == "." || ruta.isBlank() || ruta == "/" ->
                                                    if (ruta == "/") "/$nombre" else nombre
                                                else -> "$ruta/$nombre".replace("//", "/")
                                            }
                                            cargar(nueva)
                                        }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (esDir) Icons.Filled.Folder else Icons.Filled.InsertDriveFile,
                                        null,
                                        tint = if (esDir) AzulAccion else TextoSuave,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(nombre, color = Texto, fontSize = 13.sp, maxLines = 1)
                                }
                            }
                        }
                    }
                }

                // Panel transferencias
                Card(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Linea)
                ) {
                    Column(Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Upload, null, tint = VerdeNeon, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                Idioma.t("Transferencias", "Transfers"),
                                color = Texto,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        transferencias.forEach { t ->
                            Column(Modifier.padding(bottom = 14.dp)) {
                                Text(
                                    Idioma.t(
                                        "Subiendo ${t.nombre} (${(t.progreso * 100).toInt()}%)",
                                        "Uploading ${t.nombre} (${(t.progreso * 100).toInt()}%)"
                                    ),
                                    color = Texto,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { t.progreso },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                    color = VerdeNeon,
                                    trackColor = Linea
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(t.velocidad, color = TextoSuave, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}