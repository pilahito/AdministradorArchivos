package com.david.administradorarchivos.ui.pantallas

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.david.administradorarchivos.core.red.GestorSesion
import com.david.administradorarchivos.ui.terminal.SshTermuxView
import com.david.administradorarchivos.ui.theme.*

/**
 * Terminal CloudTerm Pro — mockup: dots + pestana + TerminalView (Termux) + extra keys.
 * Cuerpo: AndroidView { SshTermuxView } (TerminalEmulator + TerminalRenderer), NO Text.
 */
@Composable
fun PantallaTerminales() {
    val host by GestorSesion.hostActivo.collectAsState()
    val estado by GestorSesion.estado.collectAsState()
    var termuxRef by remember { mutableStateOf<SshTermuxView?>(null) }
    var ctrlOn by remember { mutableStateOf(false) }
    var altOn by remember { mutableStateOf(false) }
    val hostKey = host?.id ?: "none"

    val pestana = host?.let { "${it.usuario}@${it.alias.ifBlank { it.direccion }}:~" }
        ?: Idioma.t("sin-sesion:~", "no-session:~")

    LaunchedEffect(hostKey, termuxRef) {
        val view = termuxRef ?: return@LaunchedEffect
        val input = GestorSesion.shellInput()
        val output = GestorSesion.shellOutput()
        if (host != null && input != null && output != null) {
            view.attachSshStreams(
                input = input,
                outputStream = output,
                key = "ssh:$hostKey",
                onResize = { c, r -> GestorSesion.resizeShell(c, r) }
            )
        } else {
            view.attachDemoBanner(
                Idioma.t(
                    "CloudTerm Pro — TerminalView (Termux)\r\n" +
                        "Conecta un host en Hosts para abrir el PTY SSH.\r\n" +
                        "Estado: $estado\r\n",
                    "CloudTerm Pro — TerminalView (Termux)\r\n" +
                        "Connect a host in Hosts to open the SSH PTY.\r\n" +
                        "Status: $estado\r\n"
                )
            )
        }
    }

    Column(Modifier.fillMaxSize().background(FondoApp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(FondoBarra)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFF5F56)))
                Box(Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFFBD2E)))
                Box(Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF27C93F)))
            }
            Spacer(Modifier.width(12.dp))
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
                .border(1.dp, Linea, RoundedCornerShape(12.dp))
                .background(Color.Black)
        ) {
            key(hostKey) {
                AndroidView(
                    factory = { ctx ->
                        SshTermuxView(ctx).also { view ->
                            termuxRef = view
                            view.layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .background(FondoBarra)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExtraKeyChip("Alt", selected = altOn) {
                altOn = !altOn
                termuxRef?.setAlt(altOn)
            }
            ExtraKeyChip("Ctrl", selected = ctrlOn) {
                ctrlOn = !ctrlOn
                termuxRef?.setCtrl(ctrlOn)
            }
            ExtraKeyChip("Esc") { termuxRef?.sendExtraKey("esc") }
            ExtraKeyChip("Tab") { termuxRef?.sendExtraKey("tab") }
            ExtraKeyChip("Left") { termuxRef?.sendExtraKey("left") }
            ExtraKeyChip("Up") { termuxRef?.sendExtraKey("up") }
            ExtraKeyChip("Down") { termuxRef?.sendExtraKey("down") }
            ExtraKeyChip("Right") { termuxRef?.sendExtraKey("right") }
        }
    }
}

@Composable
private fun ExtraKeyChip(label: String, selected: Boolean = false, onClick: () -> Unit) {
    Surface(
        color = if (selected) AzulAccion.copy(alpha = 0.25f) else FondoTarjeta,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) AzulAccion else Linea
        ),
        modifier = Modifier
            .defaultMinSize(minWidth = 44.dp, minHeight = 36.dp)
            .clickable(onClick = onClick)
    ) {
        Box(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
            Text(
                label,
                color = if (selected) AzulAccion else Texto,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
