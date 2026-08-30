package com.david.administradorarchivos.ui.pantallas

import androidx.compose.foundation.background
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
    val estado by GestorSesion.estado.collectAsState()
    var comando by remember { mutableStateOf("") }
    var historial by remember { mutableStateOf("Sesión lista. Escribe un comando (ls, pwd, whoami…)\n") }
    var enviando by remember { mutableStateOf(false) }
    val alcance = rememberCoroutineScope()
    val scroll = rememberScrollState()

    LaunchedEffect(historial) { scroll.animateScrollTo(scroll.maxValue) }

    Column(Modifier.fillMaxSize().background(FondoApp).padding(16.dp)) {
        Text("Terminal", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Texto)
        Text(
            host?.let { "${it.usuario}@${it.direccion}:${it.puerto}" } ?: "Ningún host conectado",
            color = if (host != null) VerdeConectado else TextoSuave,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(12.dp))

        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
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

        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = comando,
                onValueChange = { comando = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("comando") },
                singleLine = true,
                enabled = host != null && !enviando,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.width(8.dp))
            FloatingActionButton(
                onClick = {
                    val cmd = comando.trim()
                    if (cmd.isBlank() || host == null) return@FloatingActionButton
                    enviando = true
                    historial += "\n$ ${cmd}\n"
                    comando = ""
                    alcance.launch {
                        try {
                            val out = withContext(Dispatchers.IO) {
                                GestorSesion.clienteSftp?.ejecutarComando(cmd) ?: "Sin sesión"
                            }
                            historial += out + "\n"
                        } catch (e: Exception) {
                            historial += "error: ${e.message}\n"
                        } finally {
                            enviando = false
                        }
                    }
                },
                containerColor = AzulAccion,
                modifier = Modifier.size(52.dp)
            ) { Icon(Icons.Filled.Send, contentDescription = "Enviar") }
        }
        if (host == null) {
            Spacer(Modifier.height(8.dp))
            Text("Ve a Hosts, crea un servidor y tócalo para conectar.", color = TextoSuave)
        }
    }
}
