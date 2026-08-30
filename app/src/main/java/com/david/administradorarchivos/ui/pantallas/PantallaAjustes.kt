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
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    var email by remember { mutableStateOf(ClienteGoogleDrive.cuentaGuardada(ctx)?.email) }
    var info by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode != Activity.RESULT_OK) {
            info = "Login cancelado o el Client ID Android no está creado en Google Cloud (paquete + SHA-1)."
            return@rememberLauncherForActivityResult
        }
        alcance.launch {
            try {
                val cuenta = GoogleSignIn.getSignedInAccountFromIntent(res.data).getResult(ApiException::class.java)
                clienteDrive.conectarConCuenta(cuenta)
                email = cuenta.email
                val archivos = withContext(Dispatchers.IO) { clienteDrive.listarArchivos() }
                info = "Drive: ${archivos.size} archivos en Mi unidad"
            } catch (e: Exception) {
                info = e.message ?: "No se pudo entrar en Drive"
            }
        }
    }

    Column(
        Modifier.fillMaxSize().background(FondoApp).padding(16.dp).verticalScroll(rememberScrollState())
    ) {
        Text("Ajustes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Texto)
        Spacer(Modifier.height(16.dp))

        Text("Keychain / Google Drive", color = TextoSuave)
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
                    "Conexión por pantalla de permisos de Google (OAuth). Sin Client ID Android en Google Cloud el login se cierra solo.",
                    color = TextoSuave,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))
                if (email != null) {
                    Text("Sesión: $email", color = VerdeConectado)
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
                        Text("Cerrar sesión")
                    }
                } else {
                    Button(
                        onClick = { launcher.launch(clienteSignIn.signInIntent) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulAccion)
                    ) {
                        Icon(Icons.Filled.Key, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Conectar con Google")
                    }
                }
                info?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = TextoSuave)
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Sesión SSH", color = TextoSuave)
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { GestorSesion.desconectar() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = FondoTarjeta)
        ) { Text("Desconectar host activo", color = Texto) }

        Spacer(Modifier.height(24.dp))
        Text("Apariencia", color = TextoSuave)
        Text("Tema oscuro estilo cliente SSH moderno. Hosts, Terminal, SFTP y Ajustes.", color = Texto, style = MaterialTheme.typography.bodyMedium)
    }
}
