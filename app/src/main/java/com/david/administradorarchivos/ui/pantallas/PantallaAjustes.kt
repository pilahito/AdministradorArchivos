package com.david.administradorarchivos.ui.pantallas

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var proveedor by remember { mutableStateOf("MEGA") }
    var syncAuto by remember { mutableStateOf(true) }
    var paleta by remember { mutableStateOf("cyan") }
    var seccion by remember { mutableStateOf("tema") }

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

    Column(Modifier.fillMaxSize().background(FondoApp).padding(16.dp)) {
        Text(
            Idioma.t("Ajustes", "Settings"),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Texto
        )
        Text(
            Idioma.t("CloudTerm Pro — personalización y nube", "CloudTerm Pro — theme and cloud"),
            color = TextoSuave,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(14.dp))

        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Rail de secciones
            Card(
                modifier = Modifier.width(140.dp).fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Linea)
            ) {
                Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(
                        "tema" to Idioma.t("Tema", "Theme"),
                        "nube" to Idioma.t("Proveedor nube", "Cloud provider"),
                        "idioma" to Idioma.t("Idioma", "Language"),
                        "sync" to Idioma.t("Sincronización", "Sync")
                    ).forEach { (id, label) ->
                        val sel = seccion == id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { seccion = id },
                            color = if (sel) AzulAccion.copy(alpha = 0.18f) else Color.Transparent,
                            shape = RoundedCornerShape(10.dp),
                            border = if (sel) BorderStroke(1.dp, AzulAccion.copy(alpha = 0.5f)) else null
                        ) {
                            Text(
                                label,
                                color = if (sel) AzulAccion else Texto,
                                fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp)
                            )
                        }
                    }
                }
            }

            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (seccion) {
                    "tema" -> {
                        SeccionTitulo(Idioma.t("Tema", "Theme"), Icons.Filled.Palette)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Linea)
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = true,
                                        onClick = {},
                                        colors = RadioButtonDefaults.colors(selectedColor = AzulAccion)
                                    )
                                    Text(Idioma.t("Dark Mode", "Dark Mode"), color = Texto, fontWeight = FontWeight.SemiBold)
                                }
                                Text(Idioma.t("Paleta de acentos", "Accent palette"), color = TextoSuave, fontSize = 12.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    listOf(
                                        "cyan" to AzulAccion,
                                        "neon" to VerdeNeon,
                                        "material" to Color(0xFF90A4AE)
                                    ).forEach { (id, color) ->
                                        Box(
                                            Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                                .border(
                                                    width = if (paleta == id) 3.dp else 1.dp,
                                                    color = if (paleta == id) Color.White else Linea,
                                                    shape = CircleShape
                                                )
                                                .clickable { paleta = id }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    "nube" -> {
                        SeccionTitulo(Idioma.t("Proveedor de nube", "Cloud provider"), Icons.Filled.Cloud)
                        val providers = listOf("MEGA", "Drive", "Dropbox", "OneDrive")
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            providers.forEach { p ->
                                val sel = proveedor == p
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { proveedor = p },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (sel) AzulAccion.copy(alpha = 0.15f) else FondoTarjeta
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, if (sel) AzulAccion else Linea)
                                ) {
                                    Column(
                                        Modifier.padding(10.dp).fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            Icons.Filled.Cloud,
                                            null,
                                            tint = if (sel) AzulAccion else TextoSuave,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(p, color = if (sel) AzulAccion else Texto, fontSize = 11.sp, maxLines = 1)
                                        if (sel) {
                                            Icon(
                                                Icons.Filled.CheckCircle,
                                                null,
                                                tint = AzulAccion,
                                                modifier = Modifier.size(14.dp).padding(top = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Google Drive login (existente, restyled)
                        Spacer(Modifier.height(4.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Linea)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
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
                                    OutlinedButton(
                                        onClick = {
                                            alcance.launch {
                                                clienteDrive.desconectar(clienteSignIn)
                                                email = null
                                                info = null
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        border = BorderStroke(1.dp, Linea)
                                    ) {
                                        Icon(Icons.Filled.Logout, null, Modifier.size(18.dp), tint = Texto)
                                        Spacer(Modifier.width(8.dp))
                                        Text(Idioma.t("Cerrar sesión", "Sign out"), color = Texto)
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            proveedor = "Drive"
                                            launcher.launch(clienteSignIn.signInIntent)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AzulAccion,
                                            contentColor = FondoApp
                                        ),
                                        shape = RoundedCornerShape(12.dp)
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
                    }
                    "idioma" -> {
                        SeccionTitulo(Idioma.t("Idioma", "Language"), null)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Linea)
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        if (Idioma.espanol) "Español" else "English",
                                        color = Texto,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        Idioma.t("Idioma de la interfaz", "Interface language"),
                                        color = TextoSuave,
                                        fontSize = 12.sp
                                    )
                                }
                                Switch(
                                    checked = Idioma.espanol,
                                    onCheckedChange = { Idioma.cambiar(ctx, it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = VerdeNeon
                                    )
                                )
                            }
                        }
                    }
                    else -> {
                        SeccionTitulo(Idioma.t("Sincronización automática", "Automatic sync"), null)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Linea)
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        Idioma.t("Sync automática", "Auto sync"),
                                        color = Texto,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        Idioma.t(
                                            "Sincroniza hosts y bóveda con $proveedor",
                                            "Sync hosts and vault with $proveedor"
                                        ),
                                        color = TextoSuave,
                                        fontSize = 12.sp
                                    )
                                }
                                Switch(
                                    checked = syncAuto,
                                    onCheckedChange = { syncAuto = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = VerdeNeon
                                    )
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { GestorSesion.desconectar() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = FondoTarjeta, contentColor = Texto),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(Idioma.t("Desconectar sesión SSH activa", "Disconnect active SSH session"))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeccionTitulo(titulo: String, icono: androidx.compose.ui.graphics.vector.ImageVector?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icono != null) {
            Icon(icono, null, tint = AzulAccion, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(titulo, color = Texto, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}