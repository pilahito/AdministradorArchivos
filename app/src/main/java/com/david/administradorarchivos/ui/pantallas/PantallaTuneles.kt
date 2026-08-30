package com.david.administradorarchivos.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.david.administradorarchivos.core.datos.AlmacenHosts
import com.david.administradorarchivos.core.datos.AlmacenTuneles
import com.david.administradorarchivos.core.datos.ReglaTunel
import com.david.administradorarchivos.core.red.GestorTuneles
import com.david.administradorarchivos.ui.theme.*
import java.util.UUID

private data class PresetTunel(val nombre: String, val local: String, val dest: String, val puerto: String)

@Composable
fun PantallaTuneles() {
    val ctx = LocalContext.current
    val almacenHosts = remember { AlmacenHosts(ctx) }
    val almacen = remember { AlmacenTuneles(ctx) }
    val hosts = remember { almacenHosts.listar() }
    var reglas by remember { mutableStateOf(almacen.listar()) }
    val vivos by GestorTuneles.lista.collectAsState()

    var hostId by remember { mutableStateOf(hosts.firstOrNull()?.id ?: "") }
    var nombre by remember { mutableStateOf("Web") }
    var puertoLocal by remember { mutableStateOf("8080") }
    var destinoHost by remember { mutableStateOf("127.0.0.1") }
    var destinoPuerto by remember { mutableStateOf("80") }

    val host = hosts.firstOrNull { it.id == hostId } ?: hosts.firstOrNull()
    val presets = listOf(
        PresetTunel("HTTP", "8080", "127.0.0.1", "80"),
        PresetTunel("HTTPS", "8443", "127.0.0.1", "443"),
        PresetTunel("MySQL", "3306", "127.0.0.1", "3306"),
        PresetTunel("Postgres", "5432", "127.0.0.1", "5432"),
        PresetTunel("Redis", "6379", "127.0.0.1", "6379"),
        PresetTunel("RDP", "3389", "127.0.0.1", "3389")
    )

    fun arrancar(regla: ReglaTunel) {
        val h = hosts.firstOrNull { it.id == regla.hostId } ?: host ?: return
        GestorTuneles.arrancarLocal(
            contexto = ctx.applicationContext,
            id = regla.id,
            host = h.direccion,
            puertoSsh = h.puerto,
            usuario = h.usuario,
            contrasena = h.contrasena.ifBlank { null },
            rutaClave = h.rutaClave.ifBlank { null },
            puertoLocal = regla.puertoLocal,
            destinoHost = regla.destinoHost,
            destinoPuerto = regla.destinoPuerto,
            nombre = regla.nombre
        )
    }

    Column(Modifier.fillMaxSize().background(FondoApp).padding(16.dp)) {
        Text(Idioma.t("Túneles locales", "Local tunnels"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Texto)
        Text(
            Idioma.t(
                "Igual que ssh -L puerto:destino:puerto. El teléfono escucha en 127.0.0.1 y el servidor abre el destino.",
                "Same as ssh -L port:target:port. The phone listens on 127.0.0.1 and the server opens the target."
            ),
            color = TextoSuave
        )
        Spacer(Modifier.height(12.dp))
        if (hosts.isEmpty()) {
            Text(Idioma.t("Crea una sesión SSH primero", "Create an SSH session first"), color = TextoSuave)
            return@Column
        }

        Text(Idioma.t("Sesión SSH", "SSH session"), color = TextoSuave)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            hosts.forEach { h ->
                FilterChip(
                    selected = hostId == h.id,
                    onClick = { hostId = h.id },
                    label = { Text(h.alias.ifBlank { h.direccion }) }
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(Idioma.t("Presets", "Presets"), color = TextoSuave)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            presets.forEach { p ->
                AssistChip(
                    onClick = {
                        nombre = p.nombre
                        puertoLocal = p.local
                        destinoHost = p.dest
                        destinoPuerto = p.puerto
                    },
                    label = { Text(p.nombre) }
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(nombre, { nombre = it }, label = { Text(Idioma.t("Nombre", "Name")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(puertoLocal, { puertoLocal = it.filter { c -> c.isDigit() } }, label = { Text(Idioma.t("Puerto en el móvil", "Phone port")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(destinoHost, { destinoHost = it }, label = { Text(Idioma.t("Host en el servidor", "Host on server")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(destinoPuerto, { destinoPuerto = it.filter { c -> c.isDigit() } }, label = { Text(Idioma.t("Puerto destino", "Target port")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Text(
            "ssh -L ${puertoLocal.ifBlank { "8080" }}:${destinoHost.ifBlank { "127.0.0.1" }}:${destinoPuerto.ifBlank { "80" }}",
            color = AzulAccion,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(top = 6.dp)
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                val h = host ?: return@Button
                val regla = ReglaTunel(
                    id = UUID.randomUUID().toString(),
                    hostId = h.id,
                    nombre = nombre.ifBlank { "Túnel" },
                    puertoLocal = puertoLocal.toIntOrNull() ?: 8080,
                    destinoHost = destinoHost.trim().ifBlank { "127.0.0.1" },
                    destinoPuerto = destinoPuerto.toIntOrNull() ?: 80
                )
                almacen.guardar(regla)
                reglas = almacen.listar()
                arrancar(regla)
            },
            enabled = host != null,
            colors = ButtonDefaults.buttonColors(containerColor = VerdeFab, contentColor = FondoApp),
            modifier = Modifier.fillMaxWidth()
        ) { Text(Idioma.t("Guardar y arrancar -L", "Save and start -L")) }

        Spacer(Modifier.height(16.dp))
        Text(Idioma.t("Reglas", "Rules"), fontWeight = FontWeight.SemiBold, color = Texto)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f, fill = false)) {
            items(reglas, key = { it.id }) { r ->
                val estado = vivos.firstOrNull { it.id == r.id }
                Card(colors = CardDefaults.cardColors(containerColor = FondoTarjeta), shape = RoundedCornerShape(14.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(r.nombre, color = Texto, fontWeight = FontWeight.SemiBold)
                        Text(
                            "127.0.0.1:${r.puertoLocal}  →  ${r.destinoHost}:${r.destinoPuerto}",
                            color = TextoSuave,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            when {
                                estado?.activo == true -> Idioma.t("Activo", "Active")
                                estado?.error != null -> estado.error!!
                                else -> Idioma.t("Parado", "Stopped")
                            },
                            color = when {
                                estado?.activo == true -> VerdeConectado
                                estado?.error != null -> Rojo
                                else -> TextoSuave
                            }
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { arrancar(r) }) {
                                Text(Idioma.t("Arrancar", "Start"), color = VerdeConectado)
                            }
                            TextButton(onClick = { GestorTuneles.parar(r.id) }) {
                                Text(Idioma.t("Parar", "Stop"), color = AzulAccion)
                            }
                            TextButton(onClick = {
                                GestorTuneles.parar(r.id)
                                almacen.borrar(r.id)
                                reglas = almacen.listar()
                            }) {
                                Text(Idioma.t("Borrar", "Delete"), color = Rojo)
                            }
                        }
                    }
                }
            }
        }
    }
}
