package com.david.administradorarchivos.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.david.administradorarchivos.core.red.GestorSesion
import com.david.administradorarchivos.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PantallaTerminales() {
    val host by GestorSesion.hostActivo.collectAsState()
    var comando by remember { mutableStateOf("") }
    var historial by remember {
        mutableStateOf(
            Idioma.t(
                "Sesión lista. Escribe un comando (ls, pwd, whoami…)\n",
                "Session ready. Type a command (ls, pwd, whoami…)\n"
            )
        )
    }
    var enviando by remember { mutableStateOf(false) }
    val alcance = rememberCoroutineScope()
    val scroll = rememberScrollState()

    LaunchedEffect(historial) { scroll.animateScrollTo(scroll.maxValue) }

    fun enviar(cmd: String) {
        val texto = cmd.trim()
        if (texto.isBlank() || host == null || enviando) return
        enviando = true
        historial += "\n$ $texto\n"
        comando = ""
        alcance.launch {
            try {
                val out = withContext(Dispatchers.IO) {
                    GestorSesion.clienteSftp?.ejecutarComando(texto)
                        ?: Idioma.t("Sin sesión", "No session")
                }
                historial += out + "\n"
            } catch (e: Exception) {
                historial += "error: ${e.message}\n"
            } finally {
                enviando = false
            }
        }
    }

    Column(Modifier.fillMaxSize().background(FondoApp)) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                Idioma.t("Terminal", "Terminal"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Texto
            )
            Text(
                host?.let { "${it.usuario}@${it.direccion}:${it.puerto}" }
                    ?: Idioma.t("Ninguna sesión conectada", "No session connected"),
                color = if (host != null) VerdeConectado else TextoSuave,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(androidx.compose.ui.graphics.Color(0xFF0B0D12))
                .padding(12.dp)
        ) {
            Text(
                historial,
                color = VerdeConectado,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxSize().verticalScroll(scroll)
            )
        }

        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "tab" to "\t",
                "esc" to "",
                "ctrl" to "",
                "alt" to "",
                "/" to "/",
                "|" to "|",
                "-" to "-",
                "~" to "~"
            ).forEach { (etiqueta, inserta) ->
                AssistChip(
                    onClick = {
                        if (inserta.isNotEmpty()) comando += inserta
                    },
                    label = { Text(etiqueta, fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = FondoTarjeta,
                        labelColor = VerdeConectado
                    )
                )
            }
        }

        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = comando,
                onValueChange = { comando = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(Idioma.t("comando", "command")) },
                singleLine = true,
                enabled = host != null && !enviando,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.width(8.dp))
            FloatingActionButton(
                onClick = { enviar(comando) },
                containerColor = AzulAccion,
                modifier = Modifier.size(52.dp)
            ) { Icon(Icons.Filled.Send, contentDescription = Idioma.t("Enviar", "Send")) }
        }
        if (host == null) {
            Text(
                Idioma.t(
                    "Ve a Sesiones, pulsa Nueva sesión y luego toca la tarjeta para conectar.",
                    "Go to Sessions, tap New session, then tap the card to connect."
                ),
                color = TextoSuave,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
    }
}
