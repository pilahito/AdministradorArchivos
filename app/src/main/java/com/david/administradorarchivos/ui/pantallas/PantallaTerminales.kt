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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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
    val estado by GestorSesion.estado.collectAsState()

    LaunchedEffect(historial) { scroll.animateScrollTo(scroll.maxValue) }

    val pestana = host?.let { "${it.usuario}@${it.alias.ifBlank { it.direccion }}:~" }
        ?: Idioma.t("sin-sesión:~", "no-session:~")

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
        // Pestaña estilo terminal
        Row(
            Modifier
                .fillMaxWidth()
                .background(FondoBarra)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = FondoTarjeta,
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Linea)
            ) {
                Text(
                    pestana,
                    color = AzulAccion,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                if (host != null) Idioma.t("Conectado", "Connected") else Idioma.t("Desconectado", "Disconnected"),
                color = if (host != null) VerdeConectado else TextoSuave,
                fontSize = 12.sp
            )
        }

        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF071018))
                .padding(12.dp)
        ) {
            Text(
                colorearSalidaTerminal(historial),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.fillMaxSize().verticalScroll(scroll)
            )
        }

        if (host != null) {
            Text(
                Idioma.t(
                    "Conectado exitosamente ${host!!.usuario}@${host!!.direccion}. $estado",
                    "Connected successfully ${host!!.usuario}@${host!!.direccion}. $estado"
                ),
                color = VerdeConectado,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
            )
        }

        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 6.dp),
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
                    onClick = { if (inserta.isNotEmpty()) comando += inserta },
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
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AzulAccion,
                    unfocusedBorderColor = Linea,
                    focusedTextColor = Texto,
                    unfocusedTextColor = Texto,
                    cursorColor = AzulAccion
                )
            )
            Spacer(Modifier.width(8.dp))
            FloatingActionButton(
                onClick = { enviar(comando) },
                containerColor = AzulAccion,
                contentColor = FondoApp,
                modifier = Modifier.size(52.dp)
            ) { Icon(Icons.Filled.Send, contentDescription = Idioma.t("Enviar", "Send")) }
        }
        if (host == null) {
            Text(
                Idioma.t(
                    "Ve a Hosts, pulsa + y luego toca la tarjeta para conectar.",
                    "Go to Hosts, tap +, then tap the card to connect."
                ),
                color = TextoSuave,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
    }
}

/** Colorea dirs en cian y binarios/ejecutables en verde (heurística ls -la). */
private fun colorearSalidaTerminal(texto: String) = buildAnnotatedString {
    texto.lineSequence().forEachIndexed { idx, linea ->
        if (idx > 0) append("\n")
        when {
            linea.startsWith("$ ") || linea.startsWith("# ") -> {
                withStyle(SpanStyle(color = AzulAccion, fontWeight = FontWeight.Bold)) { append(linea) }
            }
            linea.startsWith("d") && linea.length > 10 && linea.contains(Regex("\\s")) -> {
                val partes = linea.split(Regex("\\s+"), limit = 9)
                if (partes.size >= 9) {
                    withStyle(SpanStyle(color = TextoSuave)) {
                        append(partes.dropLast(1).joinToString(" ") + " ")
                    }
                    withStyle(SpanStyle(color = AzulAccion, fontWeight = FontWeight.SemiBold)) {
                        append(partes.last())
                    }
                } else {
                    withStyle(SpanStyle(color = AzulAccion)) { append(linea) }
                }
            }
            linea.startsWith("-") && linea.length > 3 && linea[3] == 'x' -> {
                val partes = linea.split(Regex("\\s+"), limit = 9)
                if (partes.size >= 9) {
                    withStyle(SpanStyle(color = TextoSuave)) {
                        append(partes.dropLast(1).joinToString(" ") + " ")
                    }
                    withStyle(SpanStyle(color = VerdeNeon, fontWeight = FontWeight.SemiBold)) {
                        append(partes.last())
                    }
                } else {
                    withStyle(SpanStyle(color = VerdeNeon)) { append(linea) }
                }
            }
            linea.startsWith("error:", ignoreCase = true) -> {
                withStyle(SpanStyle(color = Rojo)) { append(linea) }
            }
            else -> withStyle(SpanStyle(color = VerdeConectado)) { append(linea) }
        }
    }
}