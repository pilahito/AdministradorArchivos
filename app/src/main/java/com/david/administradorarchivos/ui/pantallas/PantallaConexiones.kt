package com.david.administradorarchivos.ui.pantallas

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.david.administradorarchivos.core.red.ArchivoDrive
import com.david.administradorarchivos.core.red.ClienteFtp
import com.david.administradorarchivos.core.red.ClienteGoogleDrive
import com.david.administradorarchivos.core.red.ClienteSftp
import com.david.administradorarchivos.core.red.DatosConexionFtp
import com.david.administradorarchivos.core.red.DatosConexionSsh
import com.david.administradorarchivos.ui.theme.AcentoCian
import com.david.administradorarchivos.ui.theme.FondoTarjeta
import com.david.administradorarchivos.ui.theme.FondoTarjetaElevada
import com.david.administradorarchivos.ui.theme.VerdeConectado
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class ProtocoloRed(val etiqueta: String) {
    SFTP("SFTP"),
    FTP("FTP"),
    FTPS("FTPS"),
    GOOGLE_DRIVE("Drive")
}

@Composable
fun PantallaConexiones() {
    val contexto = LocalContext.current
    val alcanceCorrutina = rememberCoroutineScope()

    var protocolo by remember { mutableStateOf(ProtocoloRed.SFTP) }
    var host by remember { mutableStateOf("") }
    var puerto by remember { mutableStateOf("22") }
    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var rutaClavePrivada by remember { mutableStateOf("") }

    var estadoConexion by remember { mutableStateOf("Sin conectar") }
    var conectando by remember { mutableStateOf(false) }
    var listadoRemoto by remember { mutableStateOf<List<String>>(emptyList()) }
    var listadoDrive by remember { mutableStateOf<List<ArchivoDrive>>(emptyList()) }
    var errorTexto by remember { mutableStateOf<String?>(null) }
    var emailDrive by remember { mutableStateOf<String?>(null) }

    val clienteDrive = remember { ClienteGoogleDrive(contexto) }
    val clienteSignIn = remember { ClienteGoogleDrive.crearClienteSignIn(contexto) }

    // Restaurar sesión de Google Drive si ya estaba autenticado
    LaunchedEffect(Unit) {
        val cuentaGuardada = ClienteGoogleDrive.cuentaGuardada(contexto)
        if (cuentaGuardada != null) {
            clienteDrive.conectarConCuenta(cuentaGuardada)
            emailDrive = cuentaGuardada.email
            estadoConexion = "Google Drive: ${cuentaGuardada.email}"
            try {
                listadoDrive = withContext(Dispatchers.IO) { clienteDrive.listarArchivos() }
            } catch (_: Exception) { }
        }
    }

    // Launcher del flujo OAuth: abre la pantalla de Google con el "link de permisos"
    val launcherSignIn = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == Activity.RESULT_OK) {
            alcanceCorrutina.launch {
                try {
                    val tarea = GoogleSignIn.getSignedInAccountFromIntent(resultado.data)
                    val cuenta = tarea.getResult(ApiException::class.java)
                    clienteDrive.conectarConCuenta(cuenta)
                    emailDrive = cuenta.email
                    estadoConexion = "Google Drive: ${cuenta.email}"
                    listadoDrive = withContext(Dispatchers.IO) { clienteDrive.listarArchivos() }
                    listadoRemoto = emptyList()
                    errorTexto = null
                } catch (e: ApiException) {
                    errorTexto = "Error de autenticación Google (${e.statusCode}): ${e.message}"
                    estadoConexion = "Sin conectar"
                } catch (e: Exception) {
                    errorTexto = e.message ?: "Error al conectar con Google Drive"
                    estadoConexion = "Sin conectar"
                } finally {
                    conectando = false
                }
            }
        } else {
            conectando = false
            errorTexto = "Inicio de sesión cancelado"
        }
    }

    fun conectarSshOFtp() {
        errorTexto = null
        conectando = true
        alcanceCorrutina.launch {
            try {
                val nombres = withContext(Dispatchers.IO) {
                    when (protocolo) {
                        ProtocoloRed.SFTP -> {
                            val cliente = ClienteSftp(
                                DatosConexionSsh(
                                    host = host,
                                    puerto = puerto.toIntOrNull() ?: 22,
                                    usuario = usuario,
                                    contrasena = contrasena.ifBlank { null },
                                    rutaClavePrivada = rutaClavePrivada.ifBlank { null }
                                )
                            )
                            cliente.conectar()
                            val entradas = cliente.listarArchivos(".")
                            cliente.desconectar()
                            entradas.map { it.filename }
                        }
                        else -> {
                            val cliente = ClienteFtp(
                                DatosConexionFtp(
                                    host = host,
                                    puerto = puerto.toIntOrNull() ?: 21,
                                    usuario = usuario,
                                    contrasena = contrasena,
                                    usarFtps = protocolo == ProtocoloRed.FTPS
                                )
                            )
                            cliente.conectar()
                            val entradas = cliente.listarArchivos(".")
                            cliente.desconectar()
                            entradas.map { it.name }
                        }
                    }
                }
                listadoRemoto = nombres
                listadoDrive = emptyList()
                estadoConexion = "Conectado a $host"
            } catch (e: Exception) {
                errorTexto = e.message ?: "Error de conexión"
                estadoConexion = "Sin conectar"
            } finally {
                conectando = false
            }
        }
    }

    fun conectarGoogleDrive() {
        errorTexto = null
        conectando = true
        // Abre la UI oficial de Google Sign-In → pantalla de consentimiento / link de permisos
        launcherSignIn.launch(clienteSignIn.signInIntent)
    }

    fun desconectarDrive() {
        alcanceCorrutina.launch {
            clienteDrive.desconectar(clienteSignIn)
            emailDrive = null
            listadoDrive = emptyList()
            estadoConexion = "Sin conectar"
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Cabecera estilo Termius
        Text(
            "Hosts & Conexiones",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (estadoConexion.startsWith("Conectado") || emailDrive != null)
                            VerdeConectado else Color.Gray
                    )
            )
            Spacer(Modifier.width(8.dp))
            Text(
                estadoConexion,
                style = MaterialTheme.typography.bodyMedium,
                color = if (estadoConexion.startsWith("Conectado") || emailDrive != null)
                    VerdeConectado
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }

        Spacer(Modifier.height(20.dp))

        // Selector de protocolo — tarjetas estilo Termius
        Text("Protocolo", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProtocoloRed.entries.forEach { opcion ->
                val seleccionado = protocolo == opcion
                val icono = when (opcion) {
                    ProtocoloRed.SFTP -> Icons.Filled.Terminal
                    ProtocoloRed.GOOGLE_DRIVE -> Icons.Filled.Cloud
                    else -> Icons.Filled.Storage
                }
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (seleccionado) FondoTarjetaElevada else FondoTarjeta)
                        .border(
                            width = if (seleccionado) 1.5.dp else 1.dp,
                            color = if (seleccionado) AcentoCian else MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            protocolo = opcion
                            puerto = when (opcion) {
                                ProtocoloRed.SFTP -> "22"
                                ProtocoloRed.GOOGLE_DRIVE -> ""
                                else -> "21"
                            }
                        }
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(icono, null, Modifier.size(18.dp), tint = if (seleccionado) AcentoCian else MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            opcion.etiqueta,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (seleccionado) AcentoCian else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        if (protocolo == ProtocoloRed.GOOGLE_DRIVE) {
            // Google Drive: conexión por link de permisos (OAuth)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Cloud, contentDescription = null, tint = AcentoCian)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Google Drive",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Se abrirá la pantalla oficial de Google donde autorizas el acceso " +
                        "(consentimiento / link de permisos). No se guardan contraseñas: " +
                        "solo el token OAuth que otorga Google.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                    Spacer(Modifier.height(16.dp))

                    if (emailDrive != null) {
                        Text(
                            "Sesión: $emailDrive",
                            style = MaterialTheme.typography.bodyLarge,
                            color = VerdeConectado
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { desconectarDrive() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Logout, contentDescription = null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Cerrar sesión")
                        }
                    } else {
                        Button(
                            onClick = { conectarGoogleDrive() },
                            enabled = !conectando,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AcentoCian)
                        ) {
                            Icon(Icons.Filled.Link, contentDescription = null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (conectando) "Abriendo permisos…" else "Conectar con Google")
                        }
                    }
                }
            }
        } else {
            // SSH / FTP
            OutlinedTextField(
                host, { host = it },
                label = { Text("Host / IP") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                puerto, { puerto = it },
                label = { Text("Puerto") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                usuario, { usuario = it },
                label = { Text("Usuario") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                contrasena, { contrasena = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            if (protocolo == ProtocoloRed.SFTP) {
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    rutaClavePrivada, { rutaClavePrivada = it },
                    label = { Text("Ruta a clave privada SSH (opcional)") },
                    supportingText = { Text("Déjalo vacío para usar solo usuario y contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { conectarSshOFtp() },
                enabled = !conectando && host.isNotBlank() && usuario.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AcentoCian)
            ) {
                Text(if (conectando) "Conectando…" else "Conectar")
            }
        }

        errorTexto?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        // Listado de archivos remotos
        val hayListado = listadoRemoto.isNotEmpty() || listadoDrive.isNotEmpty()
        if (hayListado) {
            Spacer(Modifier.height(24.dp))
            Text(
                if (listadoDrive.isNotEmpty()) "Archivos en Google Drive" else "Archivos remotos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))

            if (listadoDrive.isNotEmpty()) {
                listadoDrive.forEach { archivo ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(FondoTarjeta)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (archivo.esCarpeta) Icons.Filled.Folder else Icons.Filled.InsertDriveFile,
                            contentDescription = null,
                            tint = if (archivo.esCarpeta) AcentoCian else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(archivo.nombre, style = MaterialTheme.typography.bodyLarge)
                            if (!archivo.esCarpeta && archivo.tamanio != null) {
                                Text(
                                    formatBytes(archivo.tamanio),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }
            } else {
                listadoRemoto.forEach { nombre ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(FondoTarjeta)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.InsertDriveFile, contentDescription = null, tint = AcentoCian)
                        Spacer(Modifier.width(12.dp))
                        Text(nombre, style = MaterialTheme.typography.bodyLarge)
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return "%.1f KB".format(kb)
    val mb = kb / 1024.0
    if (mb < 1024) return "%.1f MB".format(mb)
    return "%.1f GB".format(mb / 1024.0)
}
