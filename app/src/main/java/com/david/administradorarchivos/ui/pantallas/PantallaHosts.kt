package com.david.administradorarchivos.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.david.administradorarchivos.core.datos.AlmacenHosts
import com.david.administradorarchivos.core.datos.HostGuardado
import com.david.administradorarchivos.core.red.GestorSesion
import com.david.administradorarchivos.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ConexionRapida(
    val usuario: String = "",
    val direccion: String = "",
    val puerto: Int = 22,
    val protocolo: String = "SSH"
)

fun parsearConexionRapida(texto: String): ConexionRapida {
    var t = texto.trim()
    var proto = "SSH"
    when {
        t.startsWith("sftp ", true) -> { proto = "SFTP"; t = t.substring(5).trim() }
        t.startsWith("ftp ", true) -> { proto = "FTP"; t = t.substring(4).trim() }
        t.startsWith("ssh ", true) -> { proto = "SSH"; t = t.substring(4).trim() }
    }
    var puerto = 22
    val puertoRegex = Regex("(?:^|\\s)(?:-p|p|port)\\s+(\\d+)", RegexOption.IGNORE_CASE)
    puertoRegex.find(t)?.let {
        puerto = it.groupValues[1].toIntOrNull() ?: 22
        t = t.replace(it.value, " ").trim()
    }
    var usuario = ""
    var direccion = t
    val arroba = t.indexOf('@')
    if (arroba > 0) {
        usuario = t.substring(0, arroba).trim()
        direccion = t.substring(arroba + 1).trim()
    }
    val dosPuntos = direccion.lastIndexOf(':')
    if (dosPuntos > 0 && direccion.substring(dosPuntos + 1).all { it.isDigit() }) {
        puerto = direccion.substring(dosPuntos + 1).toIntOrNull() ?: puerto
        direccion = direccion.substring(0, dosPuntos)
    }
    direccion = direccion.split(Regex("\\s+")).firstOrNull().orEmpty()
    return ConexionRapida(usuario, direccion, puerto, proto)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHosts(onAbrirTerminal: () -> Unit) {
    val ctx = LocalContext.current
    val teclado = LocalSoftwareKeyboardController.current
    val almacen = remember { AlmacenHosts(ctx) }
    var hosts by remember { mutableStateOf(almacen.listar()) }
    var busqueda by remember { mutableStateOf("") }
    var mostrarNuevo by remember { mutableStateOf(false) }
    var prefill by remember { mutableStateOf(ConexionRapida()) }
    var error by remember { mutableStateOf<String?>(null) }
    var conectando by remember { mutableStateOf(false) }
    val alcance = rememberCoroutineScope()
    val estado by GestorSesion.estado.collectAsState()

    val filtrados = hosts.filter {
        val q = busqueda.lowercase()
        q.isBlank() || it.alias.lowercase().contains(q) || it.direccion.lowercase().contains(q) ||
            it.usuario.lowercase().contains(q)
    }

    fun refrescar() { hosts = almacen.listar() }

    fun abrirFormulario() {
        teclado?.hide()
        prefill = parsearConexionRapida(busqueda)
        mostrarNuevo = true
    }

    fun conectar(h: HostGuardado) {
        conectando = true
        error = null
        alcance.launch {
            try {
                withContext(Dispatchers.IO) { GestorSesion.conectar(h) }
                onAbrirTerminal()
            } catch (e: Exception) {
                error = e.message ?: "No se pudo conectar. Revisa IP, puerto, usuario y contraseña."
            } finally {
                conectando = false
            }
        }
    }

    Box(Modifier.fillMaxSize().background(FondoApp)) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("Hosts", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Texto)
            Text(
                if (conectando) "Conectando…" else estado,
                style = MaterialTheme.typography.bodyMedium,
                color = if (estado.startsWith("Conectado")) VerdeConectado else TextoSuave
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("ssh user@192.168.0.250 -p 2220") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { abrirFormulario() })
            )
            Text(
                "Escribe la IP y pulsa +  ·  La barra sola no conecta",
                color = TextoSuave,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 6.dp)
            )
            Spacer(Modifier.height(8.dp))
            error?.let { Text(it, color = Rojo, style = MaterialTheme.typography.bodyMedium) }

            if (filtrados.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Dns, null, tint = TextoSuave, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No hay hosts", color = TextoSuave)
                        Text("Pulsa + y rellena IP + usuario", color = TextoSuave, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                    items(filtrados, key = { it.id }) { h ->
                        TarjetaHost(
                            host = h,
                            onClick = { conectar(h) },
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
            onClick = { abrirFormulario() },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            containerColor = VerdeFab,
            contentColor = FondoApp
        ) { Icon(Icons.Filled.Add, contentDescription = "Nuevo host") }
    }

    if (mostrarNuevo) {
        DialogoNuevoHost(
            inicial = prefill,
            onCerrar = { mostrarNuevo = false },
            onGuardar = {
                almacen.guardar(it)
                refrescar()
                mostrarNuevo = false
                busqueda = ""
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
private fun DialogoNuevoHost(
    inicial: ConexionRapida,
    onCerrar: () -> Unit,
    onGuardar: (HostGuardado) -> Unit
) {
    var alias by remember { mutableStateOf(inicial.direccion) }
    var dir by remember { mutableStateOf(inicial.direccion) }
    var puerto by remember { mutableStateOf(inicial.puerto.toString()) }
    var user by remember { mutableStateOf(inicial.usuario) }
    var pass by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var proto by remember { mutableStateOf(inicial.protocolo) }
    var aviso by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Nuevo host") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                aviso?.let { Text(it, color = Rojo) }
                OutlinedTextField(alias, { alias = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(dir, { dir = it }, label = { Text("IP o hostname (obligatorio)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(puerto, { puerto = it }, label = { Text("Puerto") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(user, { user = it }, label = { Text("Usuario (obligatorio)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    pass,
                    { pass = it },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )
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
                    when {
                        dir.isBlank() -> aviso = "Falta la IP o el hostname"
                        user.isBlank() -> aviso = "Falta el usuario"
                        else -> onGuardar(
                            HostGuardado(
                                alias = alias.ifBlank { dir },
                                direccion = dir.trim(),
                                puerto = puerto.toIntOrNull() ?: 22,
                                usuario = user.trim(),
                                contrasena = pass,
                                rutaClave = clave.trim(),
                                protocolo = proto,
                                etiquetas = proto.lowercase(),
                                color = (dir + user).hashCode()
                            )
                        )
                    }
                }
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } }
    )
}
