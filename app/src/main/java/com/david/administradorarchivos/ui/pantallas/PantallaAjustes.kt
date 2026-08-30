package com.david.administradorarchivos.ui.pantallas

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.david.administradorarchivos.core.datos.AlmacenClaves
import com.david.administradorarchivos.core.datos.ClaveSsh
import com.david.administradorarchivos.core.red.ClienteGoogleDrive
import com.david.administradorarchivos.core.red.GestorSesion
import com.david.administradorarchivos.ui.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PantallaAjustes() {
    val ctx = LocalContext.current
    val alcance = rememberCoroutineScope()
    val clienteDrive = remember { ClienteGoogleDrive(ctx) }
    val clienteSignIn = remember { ClienteGoogleDrive.crearClienteSignIn(ctx) }
    val llavero = remember { AlmacenClaves(ctx) }
    var email by remember { mutableStateOf(ClienteGoogleDrive.cuentaGuardada(ctx)?.email) }
    var info by remember { mutableStateOf<String?>(null) }
    var claves by remember { mutableStateOf(llavero.listar()) }
    var nombreClave by remember { mutableStateOf("") }
    var rutaClave by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode != Activity.RESULT_OK) {
            info = Idioma.t(
                "Login cancelado o falta el Client ID Android en Google Cloud.",
                "Login cancelled or Android Client ID is missing in Google Cloud."
            )
            return@rememberLauncherForActivityResult
        }
        alcance.launch {
            try {
                val cuenta = GoogleSignIn.getSignedInAccountFromIntent(res.data).getResult(ApiException::class.java)
                clienteDrive.conectarConCuenta(cuenta)
                email = cuenta.email
                val archivos = withContext(Dispatchers.IO) { clienteDrive.listarArchivos() }
                info = Idioma.t(
                    "Drive: ${archivos.size} archivos en Mi unidad",
                    "Drive: ${archivos.size} files in My Drive"
                )
            } catch (e: Exception) {
                info = e.message ?: Idioma.t("No se pudo entrar en Drive", "Could not sign in to Drive")
            }
        }
    }

    Column(
        Modifier.fillMaxSize().background(FondoApp).padding(16.dp).verticalScroll(rememberScrollState())
    ) {
        Text("Sesiones", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Texto)
        Text(
            Idioma.t("Cliente SSH propio. Todo gratis en esta app.", "Our own SSH client. Everything is free here."),
            color = TextoSuave,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(16.dp))

        Text(Idioma.t("Idioma", "Language"), color = TextoSuave)
        Spacer(Modifier.height(8.dp))
        Card(colors = CardDefaults.cardColors(containerColor = FondoTarjeta), shape = RoundedCornerShape(16.dp)) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (Idioma.espanol) "Español" else "English",
                    color = Texto,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.SemiBold
                )
                Switch(checked = Idioma.espanol, onCheckedChange = { Idioma.cambiar(ctx, it) })
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(Idioma.t("Llavero SSH", "SSH keychain"), color = TextoSuave)
        Spacer(Modifier.height(8.dp))
        Card(colors = CardDefaults.cardColors(containerColor = FondoTarjeta), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    Idioma.t(
                        "Guarda la ruta de una clave en el teléfono y úsala al crear una sesión.",
                        "Save a key path on the phone and use it when creating a session."
                    ),
                    color = TextoSuave,
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    nombreClave,
                    { nombreClave = it },
                    label = { Text(Idioma.t("Nombre", "Name")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    rutaClave,
                    { rutaClave = it },
                    label = { Text(Idioma.t("Ruta del archivo (ej. /sdcard/id_rsa)", "File path (e.g. /sdcard/id_rsa)")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        if (nombreClave.isNotBlank() && rutaClave.isNotBlank()) {
                            llavero.guardar(ClaveSsh(nombre = nombreClave.trim(), ruta = rutaClave.trim()))
                            claves = llavero.listar()
                            nombreClave = ""
                            rutaClave = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulAccion)
                ) {
                    Icon(Icons.Filled.Key, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(Idioma.t("Guardar clave", "Save key"))
                }
                claves.forEach { c ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(c.nombre, color = Texto, fontWeight = FontWeight.SemiBold)
                            Text(c.ruta, color = TextoSuave, style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = {
                            llavero.borrar(c.id)
                            claves = llavero.listar()
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = Idioma.t("Borrar", "Delete"), tint = TextoSuave)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Google Drive", color = TextoSuave)
        Spacer(Modifier.height(8.dp))
        Card(colors = CardDefaults.cardColors(containerColor = FondoTarjeta), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Row {
                    Icon(Icons.Filled.Cloud, null, tint = AzulAccion)
                    Spacer(Modifier.width(10.dp))
                    Text("Google Drive", fontWeight = FontWeight.SemiBold, color = Texto)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    Idioma.t(
                        "Conexión por pantalla de permisos de Google (OAuth).",
                        "Connect with the Google permissions screen (OAuth)."
                    ),
                    color = TextoSuave,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))
                if (email != null) {
                    Text(Idioma.t("Sesión: $email", "Signed in: $email"), color = VerdeConectado)
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = {
                        alcance.launch {
                            clienteDrive.desconectar(clienteSignIn)
                            email = null
                            info = null
                        }
                    }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.Logout, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(Idioma.t("Cerrar sesión", "Sign out"))
                    }
                } else {
                    Button(
                        onClick = { launcher.launch(clienteSignIn.signInIntent) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulAccion)
                    ) {
                        Icon(Icons.Filled.Key, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(Idioma.t("Conectar con Google", "Connect with Google"))
                    }
                }
                info?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = TextoSuave)
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(Idioma.t("Sesión SSH", "SSH session"), color = TextoSuave)
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { GestorSesion.desconectar() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = FondoTarjeta)
        ) { Text(Idioma.t("Desconectar sesión activa", "Disconnect active session"), color = Texto) }
    }
}
