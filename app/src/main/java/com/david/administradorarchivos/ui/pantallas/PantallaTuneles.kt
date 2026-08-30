package com.david.administradorarchivos.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.david.administradorarchivos.core.datos.AlmacenHosts
import com.david.administradorarchivos.core.red.GestorTuneles
import com.david.administradorarchivos.ui.theme.*
import androidx.compose.ui.platform.LocalContext
import java.util.UUID

@Composable
fun PantallaTuneles() {
    val ctx = LocalContext.current
    val hosts = remember { AlmacenHosts(ctx).listar() }
    val tuneles by GestorTuneles.lista.collectAsState()
    var hostId by remember { mutableStateOf(hosts.firstOrNull()?.id ?: "") }
    var puertoLocal by remember { mutableStateOf("8080") }
    var destinoHost by remember { mutableStateOf("127.0.0.1") }
    var destinoPuerto by remember { mutableStateOf("80") }

    val host = hosts.firstOrNull { it.id == hostId } ?: hosts.firstOrNull()

    Column(Modifier.fillMaxSize().background(FondoApp).padding(16.dp)) {
        Text(Idioma.t("Túneles SSH", "SSH tunnels"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Texto)
        Text(
            Idioma.t("Reenvío local (-L): 127.0.0.1:puerto → destino", "Local forward (-L): 127.0.0.1:port → target"),
            color = TextoSuave
        )
        Spacer(Modifier.height(12.dp))
        if (hosts.isEmpty()) {
            Text(Idioma.t("Crea una sesión primero", "Create a session first"), color = TextoSuave)
            return@Column
        }
        hosts.forEach { h ->
            FilterChip(
                selected = hostId == h.id,
                onClick = { hostId = h.id },
                label = { Text(h.alias.ifBlank { h.direccion }) }
            )
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(puertoLocal, { puertoLocal = it }, label = { Text(Idioma.t("Puerto local", "Local port")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(destinoHost, { destinoHost = it }, label = { Text(Idioma.t("Host destino", "Target host")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(destinoPuerto, { destinoPuerto = it }, label = { Text(Idioma.t("Puerto destino", "Target port")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                val h = host ?: return@Button
                GestorTuneles.arrancarLocal(
                    id = UUID.randomUUID().toString(),
                    host = h.direccion,
                    puertoSsh = h.puerto,
                    usuario = h.usuario,
                    contrasena = h.contrasena.ifBlank { null },
                    rutaClave = h.rutaClave.ifBlank { null },
                    puertoLocal = puertoLocal.toIntOrNull() ?: 8080,
                    destinoHost = destinoHost.trim(),
                    destinoPuerto = destinoPuerto.toIntOrNull() ?: 80
                )
            },
            enabled = host != null,
            colors = ButtonDefaults.buttonColors(containerColor = VerdeFab, contentColor = FondoApp),
            modifier = Modifier.fillMaxWidth()
        ) { Text(Idioma.t("Arrancar túnel -L", "Start -L tunnel")) }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tuneles, key = { it.id }) { t ->
                Card(colors = CardDefaults.cardColors(containerColor = FondoTarjeta), shape = RoundedCornerShape(14.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(t.etiqueta, color = Texto, fontWeight = FontWeight.SemiBold)
                        Text(
                            if (t.activo) Idioma.t("Activo", "Active") else (t.error ?: Idioma.t("Parado", "Stopped")),
                            color = if (t.activo) VerdeConectado else Rojo
                        )
                        TextButton(onClick = { GestorTuneles.parar(t.id) }) {
                            Text(Idioma.t("Parar", "Stop"), color = AzulAccion)
                        }
                    }
                }
            }
        }
    }
}
