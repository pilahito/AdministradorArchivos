package com.david.administradorarchivos.ui.pantallas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun PantallaHosts(
    onAbrirTerminal: () -> Unit,
    onAbrirMenu: (() -> Unit)? = null,
    solicitarNuevo: Boolean = false,
    onNuevoConsumido: () -> Unit = {}
) {
    val ctx = LocalContext.current
    val teclado = LocalSoftwareKeyboardController.current
    val almacen = remember { AlmacenHosts(ctx) }
    var sesiones by remember { mutableStateOf(almacen.listar()) }
    var busqueda by remember { mutableStateOf("") }
    var mostrarNuevo by remember { mutableStateOf(false) }
    var prefill by remember { mutableStateOf(ConexionRapida()) }
    var error by remember { mutableStateOf<String?>(null) }
    var conectando by remember { mutableStateOf(false) }
    var hostConectar by remember { mutableStateOf<HostGuardado?>(null) }
    var menuHostId by remember { mutableStateOf<String?>(null) }
    val activos = remember { mutableStateMapOf<String, Boolean>() }
    val alcance = rememberCoroutineScope()
    val estado by GestorSesion.estado.collectAsState()

    val filtrados = sesiones.filter {
        val q = busqueda.lowercase()
        q.isBlank() || it.alias.lowercase().contains(q) || it.direccion.lowercase().contains(q) ||
            it.usuario.lowercase().contains(q) || it.etiquetas.lowercase().contains(q)
    }

    fun refrescar() { sesiones = almacen.listar() }

    fun abrirFormulario() {
        teclado?.hide()
        prefill = parsearConexionRapida(busqueda)
        mostrarNuevo = true
    }

    LaunchedEffect(solicitarNuevo) {
        if (solicitarNuevo) {
            abrirFormulario()
            onNuevoConsumido()
        }
    }

    fun conectar(h: HostGuardado) {
        conectando = true
        error = null
        alcance.launch {
            try {
                withContext(Dispatchers.IO) { GestorSesion.conectar(h) }
                hostConectar = null
                onAbrirTerminal()
            } catch (e: Exception) {
                error = e.message ?: Idioma.t(
                    "No se pudo conectar. Revisa IP, puerto, usuario y contraseña.",
                    "Could not connect. Check IP, port, user and password."
                )
            } finally {
                conectando = false
            }
        }
    }

    Box(Modifier.fillMaxSize().background(FondoApp)) {
        Column(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
            // Cabecera CloudTerm Pro + búsqueda + badge MEGA + FAB
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (onAbrirMenu != null) {
                    IconButton(onClick = onAbrirMenu) {
                        Icon(Icons.Filled.Menu, contentDescription = Idioma.t("Menú", "Menu"), tint = AzulAccion)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f, fill = false)) {
                    Box(
                        Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AzulAccion.copy(alpha = 0.15f))
                            .border(1.dp, AzulAccion.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Shield, null, tint = VerdeFab, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "CloudTerm Pro",
                        color = Texto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1
                    )
                }
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    modifier = Modifier.weight(1.2f).height(48.dp),
                    placeholder = { Text(Idioma.t("Buscar", "Search"), fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Filled.Search, null, tint = TextoSuave, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzulAccion,
                        unfocusedBorderColor = Linea,
                        focusedContainerColor = FondoBarra,
                        unfocusedContainerColor = FondoBarra,
                        focusedTextColor = Texto,
                        unfocusedTextColor = Texto,
                        cursorColor = AzulAccion
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = { abrirFormulario() })
                )
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            Idioma.t("MEGA · Sincronizado", "MEGA · Synced"),
                            color = AzulAccion,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    },
                    leadingIcon = { Icon(Icons.Filled.Cloud, null, tint = AzulAccion, modifier = Modifier.size(16.dp)) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = FondoTarjeta),
                    border = BorderStroke(1.dp, AzulAccion.copy(alpha = 0.55f)),
                    shape = RoundedCornerShape(20.dp)
                )
            }

            Spacer(Modifier.height(12.dp))
            Text(
                Idioma.t("Lista de Hosts", "Host list"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Texto
            )
            Text(
                if (conectando) Idioma.t("Conectando…", "Connecting…") else estado,
                style = MaterialTheme.typography.bodyMedium,
                color = if (estado.startsWith("Conectado") || estado.startsWith("Connected")) VerdeConectado else TextoSuave
            )
            Spacer(Modifier.height(8.dp))
            error?.let { Text(it, color = Rojo, style = MaterialTheme.typography.bodyMedium) }

            if (filtrados.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Dns, null, tint = TextoSuave, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(Idioma.t("No hay hosts", "No hosts"), color = TextoSuave)
                        Text(
                            Idioma.t("Pulsa + para crear uno", "Tap + to create one"),
                            color = TextoSuave,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 168.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filtrados, key = { it.id }) { h ->
                        if (!activos.containsKey(h.id)) activos[h.id] = true
                        TarjetaHostPro(
                            host = h,
                            activo = activos[h.id] == true,
                            onActivo = { activos[h.id] = it },
                            menuAbierto = menuHostId == h.id,
                            onMenu = { menuHostId = if (menuHostId == h.id) null else h.id },
                            onClick = { hostConectar = h },
                            onBorrar = {
                                almacen.borrar(h.id)
                                menuHostId = null
                                refrescar()
                            }
                        )
                    }
                }
            }
        }
    }

    hostConectar?.let { h ->
        ModalBottomSheet(
            onDismissRequest = { if (!conectando) hostConectar = null },
            containerColor = FondoBarra,
            contentColor = Texto,
            dragHandle = { BottomSheetDefaults.DragHandle(color = TextoSuave) }
        ) {
            HojaConectar(
                host = h,
                conectando = conectando,
                onConectar = { conectar(it) },
                onCerrar = { if (!conectando) hostConectar = null }
            )
        }
    }

    if (mostrarNuevo) {
        DialogoNuevaSesion(
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
private fun TarjetaHostPro(
    host: HostGuardado,
    activo: Boolean,
    onActivo: (Boolean) -> Unit,
    menuAbierto: Boolean,
    onMenu: () -> Unit,
    onClick: () -> Unit,
    onBorrar: () -> Unit
) {
    val tags = host.etiquetas
        .split(',', '|', ';', ' ')
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .ifEmpty { listOf(host.protocolo) }
    val authLabel = if (host.rutaClave.isNotBlank()) "SSH Key" else "Password"
    val color = ColoresHost[kotlin.math.abs(host.color) % ColoresHost.size]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Linea.copy(alpha = 0.7f))
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color.copy(alpha = 0.2f))
                        .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Dns, null, tint = color, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    host.alias.ifBlank { host.direccion },
                    color = Texto,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Box {
                    IconButton(onClick = onMenu, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Menú", tint = TextoSuave)
                    }
                    DropdownMenu(expanded = menuAbierto, onDismissRequest = onMenu) {
                        DropdownMenuItem(
                            text = { Text(Idioma.t("Eliminar", "Delete")) },
                            onClick = onBorrar
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                tags.take(2).forEachIndexed { i, tag ->
                    val chipColor = if (i == 0) AzulAccion else VerdeNeon
                    Surface(
                        color = chipColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, chipColor.copy(alpha = 0.45f))
                    ) {
                        Text(
                            tag.replaceFirstChar { it.uppercase() },
                            color = chipColor,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(host.direccion, color = TextoSuave, fontSize = 12.sp, maxLines = 1)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (host.rutaClave.isNotBlank()) Icons.Filled.Key else Icons.Filled.Password,
                    null,
                    tint = TextoSuave,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(authLabel, color = TextoSuave, fontSize = 11.sp, modifier = Modifier.weight(1f))
                Switch(
                    checked = activo,
                    onCheckedChange = onActivo,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = VerdeNeon,
                        uncheckedThumbColor = TextoSuave,
                        uncheckedTrackColor = Linea
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaConectar(
    host: HostGuardado,
    conectando: Boolean,
    onConectar: (HostGuardado) -> Unit,
    onCerrar: () -> Unit
) {
    var ip by remember(host.id) { mutableStateOf(host.direccion) }
    var puerto by remember(host.id) { mutableStateOf(host.puerto.toString()) }
    var usuario by remember(host.id) { mutableStateOf(host.usuario) }
    var auth by remember(host.id) {
        mutableStateOf(if (host.rutaClave.isNotBlank()) "SSH Key" else "Password")
    }
    var expandAuth by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(AzulAccion.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Cloud, null, tint = AzulAccion)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                host.alias.ifBlank { host.direccion },
                color = Texto,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
        OutlinedTextField(
            ip, { ip = it },
            label = { Text("IP") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors()
        )
        OutlinedTextField(
            puerto, { puerto = it },
            label = { Text(Idioma.t("Puerto", "Port")) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors()
        )
        OutlinedTextField(
            usuario, { usuario = it },
            label = { Text(Idioma.t("Usuario", "Username")) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors()
        )
        ExposedDropdownMenuBox(expanded = expandAuth, onExpandedChange = { expandAuth = it }) {
            OutlinedTextField(
                value = auth,
                onValueChange = {},
                readOnly = true,
                label = { Text(Idioma.t("Autenticación", "Authentication")) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandAuth) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                colors = fieldColors()
            )
            ExposedDropdownMenu(expanded = expandAuth, onDismissRequest = { expandAuth = false }) {
                listOf("SSH Key", "Password").forEach { op ->
                    DropdownMenuItem(
                        text = { Text(op) },
                        onClick = { auth = op; expandAuth = false }
                    )
                }
            }
        }
        Button(
            onClick = {
                val actualizado = host.copy(
                    direccion = ip.trim(),
                    puerto = puerto.toIntOrNull() ?: 22,
                    usuario = usuario.trim()
                )
                onConectar(actualizado)
            },
            enabled = !conectando && ip.isNotBlank() && usuario.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VerdeNeon, contentColor = FondoApp),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (conectando) {
                CircularProgressIndicator(Modifier.size(22.dp), color = FondoApp, strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
            }
            Text(
                if (conectando) Idioma.t("Conectando…", "Connecting…") else Idioma.t("Conectar", "Connect"),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        TextButton(onClick = onCerrar, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(Idioma.t("Cancelar", "Cancel"), color = TextoSuave)
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AzulAccion,
    unfocusedBorderColor = Linea,
    focusedLabelColor = AzulAccion,
    unfocusedLabelColor = TextoSuave,
    focusedTextColor = Texto,
    unfocusedTextColor = Texto,
    cursorColor = AzulAccion,
    focusedContainerColor = FondoTarjeta,
    unfocusedContainerColor = FondoTarjeta
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoNuevaSesion(
    inicial: ConexionRapida,
    onCerrar: () -> Unit,
    onGuardar: (HostGuardado) -> Unit
) {
    var alias by remember { mutableStateOf("") }
    var dir by remember { mutableStateOf(inicial.direccion) }
    var puerto by remember { mutableStateOf(inicial.puerto.toString()) }
    var user by remember { mutableStateOf(inicial.usuario) }
    var pass by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var proto by remember { mutableStateOf(inicial.protocolo) }
    var etiquetas by remember { mutableStateOf("Producción,AWS") }
    var aviso by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onCerrar,
        containerColor = FondoBarra,
        title = { Text(Idioma.t("Nuevo host", "New host"), color = Texto) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                aviso?.let { Text(it, color = Rojo) }
                OutlinedTextField(
                    alias, { alias = it },
                    label = { Text(Idioma.t("Nombre (opcional)", "Name (optional)")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors()
                )
                OutlinedTextField(
                    dir, { dir = it },
                    label = { Text(Idioma.t("IP o hostname", "IP or hostname")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors()
                )
                OutlinedTextField(
                    puerto, { puerto = it },
                    label = { Text(Idioma.t("Puerto", "Port")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors()
                )
                OutlinedTextField(
                    user, { user = it },
                    label = { Text(Idioma.t("Usuario", "Username")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors()
                )
                OutlinedTextField(
                    pass, { pass = it },
                    label = { Text(Idioma.t("Contraseña", "Password")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = fieldColors()
                )
                OutlinedTextField(
                    clave, { clave = it },
                    label = { Text(Idioma.t("Ruta clave SSH (opcional)", "SSH key path (optional)")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors()
                )
                OutlinedTextField(
                    etiquetas, { etiquetas = it },
                    label = { Text(Idioma.t("Etiquetas (coma)", "Tags (comma)")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("SSH", "SFTP", "FTP").forEach { p ->
                        FilterChip(
                            selected = proto == p,
                            onClick = { proto = p },
                            label = { Text(p) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AzulAccion.copy(alpha = 0.25f),
                                selectedLabelColor = AzulAccion
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        dir.isBlank() -> aviso = Idioma.t("Falta la IP o el hostname", "IP or hostname is required")
                        user.isBlank() -> aviso = Idioma.t("Falta el usuario", "Username is required")
                        else -> onGuardar(
                            HostGuardado(
                                alias = alias.ifBlank { dir.trim() },
                                direccion = dir.trim(),
                                puerto = puerto.toIntOrNull() ?: 22,
                                usuario = user.trim(),
                                contrasena = pass,
                                rutaClave = clave.trim(),
                                protocolo = proto,
                                etiquetas = etiquetas.ifBlank { proto.lowercase() },
                                color = (dir + user).hashCode()
                            )
                        )
                    }
                }
            ) { Text(Idioma.t("Guardar", "Save"), color = AzulAccion) }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) { Text(Idioma.t("Cancelar", "Cancel"), color = TextoSuave) }
        }
    )
}