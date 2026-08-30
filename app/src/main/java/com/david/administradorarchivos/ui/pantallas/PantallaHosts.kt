package com.david.administradorarchivos.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.david.administradorarchivos.core.datos.AlmacenHosts
import com.david.administradorarchivos.core.datos.HostGuardado
import com.david.administradorarchivos.core.red.GestorSesion
import com.david.administradorarchivos.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHosts(onAbrirTerminal: () -> Unit) {
    val ctx = LocalContext.current
    val almacen = remember { AlmacenHosts(ctx) }
    var hosts by remember { mutableStateOf(almacen.listar()) }
    var busqueda by remember { mutableStateOf("") }
    var mostrarNuevo by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var conectando by remember { mutableStateOf(false) }
    val alcance = rememberCoroutineScope()
    val estado by GestorSesion.estado.collectAsState()

    val filtrados = hosts.filter {
        val q = busqueda.lowercase()
        q.isBlank() || it.alias.lowercase().contains(q) || it.direccion.lowercase().contains(q)
    }

    fun refrescar() { hosts = almacen.listar() }

    Box(Modifier.fillMaxSize().background(FondoApp)) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("Hosts", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Texto)
            Text(estado, style = MaterialTheme.typography.bodyMedium, color = if (estado.startsWith("Conectado")) VerdeConectado else TextoSuave)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar host o ssh user@host") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
            Spacer(Modifier.height(8.dp))
            error?.let { Text(it, color = Rojo, style = MaterialTheme.typography.bodyMedium) }

            if (filtrados.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Dns, null, tint = TextoSuave, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No hay hosts", color = TextoSuave)
                        Text("Pulsa + para crear uno", color = TextoSuave, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                    items(filtrados, key = { it.id }) { h ->
                        TarjetaHost(
                            host = h,
                            onClick = {
                                conectando = true
                                error = null
                                alcance.launch {
                                    try {
                                        withContext(Dispatchers.IO) { GestorSesion.conectar(h) }
                                        onAbrirTerminal()
                                    } catch (e: Exception) {
                                        error = e.message ?: "No se pudo conectar"
                                    } finally {
                                        conectando = false
                                    }
                                }
                            },
                            onBorrar = {
                                almacen.borrar(h.id)
                                refrescar()
                            }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }

        FloatingActionButton(
            onClick = { mostrarNuevo = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            containerColor = VerdeFab,
            contentColor = FondoApp
        ) { Icon(Icons.Filled.Add, contentDescription = "Nuevo host") }
    }

    if (mostrarNuevo) {
        DialogoNuevoHost(
            onCerrar = { mostrarNuevo = false },
            onGuardar = {
                almacen.guardar(it)
                refrescar()
                mostrarNuevo = false
            }
        )
    }
}

@Composable
private fun TarjetaHost(host: HostGuardado, onClick: () -> Unit, onBorrar: () -> Unit) {
    val color = ColoresHost[kotlin.math.abs(host.color) % ColoresHost.size]
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(FondoTarjeta)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Dns, null, tint = FondoApp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(host.alias.ifBlank { host.direccion }, color = Texto, fontWeight = FontWeight.SemiBold)
            Text(
                "${host.protocolo.lowercase()} · ${host.usuario} · ${host.direccion}:${host.puerto}",
                color = TextoSuave,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        IconButton(onClick = onBorrar) {
            Icon(Icons.Filled.Delete, contentDescription = "Borrar", tint = TextoSuave)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoNuevoHost(onCerrar: () -> Unit, onGuardar: (HostGuardado) -> Unit) {
    var alias by remember { mutableStateOf("") }
    var dir by remember { mutableStateOf("") }
    var puerto by remember { mutableStateOf("22") }
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var proto by remember { mutableStateOf("SSH") }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("New Host") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(alias, { alias = it }, label = { Text("Label") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(dir, { dir = it }, label = { Text("IP or Hostname") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(puerto, { puerto = it }, label = { Text("Port") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(user, { user = it }, label = { Text("Username") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(pass, { pass = it }, label = { Text("Password") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(clave, { clave = it }, label = { Text("Ruta clave SSH (opcional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("SSH", "SFTP", "FTP").forEach { p ->
                        FilterChip(selected = proto == p, onClick = { proto = p }, label = { Text(p) })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (dir.isNotBlank() && user.isNotBlank()) {
                        onGuardar(
                            HostGuardado(
                                alias = alias.ifBlank { dir },
                                direccion = dir,
                                puerto = puerto.toIntOrNull() ?: 22,
                                usuario = user,
                                contrasena = pass,
                                rutaClave = clave,
                                protocolo = proto,
                                etiquetas = proto.lowercase(),
                                color = (dir + user).hashCode()
                            )
                        )
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancel") } }
    )
}
