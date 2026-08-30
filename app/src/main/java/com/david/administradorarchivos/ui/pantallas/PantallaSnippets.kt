package com.david.administradorarchivos.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.david.administradorarchivos.core.red.GestorSesion
import com.david.administradorarchivos.datos.BovedaBaseDatos
import com.david.administradorarchivos.datos.entidades.SnippetEntidad
import com.david.administradorarchivos.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@Composable
fun PantallaSnippets() {
    val ctx = LocalContext.current
    val dao = remember { BovedaBaseDatos.obtener(ctx).snippets() }
    val items by dao.observar().collectAsState(initial = emptyList())
    var nombre by remember { mutableStateOf("") }
    var comando by remember { mutableStateOf("") }
    var salida by remember { mutableStateOf<String?>(null) }
    val alcance = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().background(FondoApp).padding(16.dp)) {
        Text(Idioma.t("Snippets", "Snippets"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Texto)
        Text(
            Idioma.t("Comandos rápidos sobre la sesión SSH activa", "Quick commands on the active SSH session"),
            color = TextoSuave
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(nombre, { nombre = it }, label = { Text(Idioma.t("Nombre", "Name")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(comando, { comando = it }, label = { Text("ls -la") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                if (nombre.isBlank() || comando.isBlank()) return@Button
                alcance.launch {
                    dao.guardar(
                        SnippetEntidad(
                            id = UUID.randomUUID().toString(),
                            nombre = nombre.trim(),
                            comando = comando.trim()
                        )
                    )
                    nombre = ""
                    comando = ""
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = AzulAccion, contentColor = FondoApp),
            modifier = Modifier.fillMaxWidth()
        ) { Text(Idioma.t("Guardar snippet", "Save snippet")) }
        Spacer(Modifier.height(12.dp))
        salida?.let { Text(it, color = VerdeConectado, fontFamily = FontFamily.Monospace) }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items, key = { it.id }) { s ->
                Card(colors = CardDefaults.cardColors(containerColor = FondoTarjeta), shape = RoundedCornerShape(14.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(s.nombre, color = Texto, fontWeight = FontWeight.SemiBold)
                            Text(s.comando, color = TextoSuave, fontFamily = FontFamily.Monospace)
                        }
                        IconButton(onClick = {
                            alcance.launch {
                                try {
                                    val out = withContext(Dispatchers.IO) {
                                        GestorSesion.clienteSftp?.ejecutarComando(s.comando)
                                            ?: Idioma.t("Conecta una sesión primero", "Connect a session first")
                                    }
                                    salida = out
                                } catch (e: Exception) {
                                    salida = e.message
                                }
                            }
                        }) { Icon(Icons.Filled.PlayArrow, null, tint = VerdeConectado) }
                        IconButton(onClick = { alcance.launch { dao.borrar(s.id) } }) {
                            Icon(Icons.Filled.Delete, null, tint = TextoSuave)
                        }
                    }
                }
            }
        }
    }
}
