package com.david.administradorarchivos.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.david.administradorarchivos.ui.theme.FondoApp
import com.david.administradorarchivos.ui.theme.Idioma
import com.david.administradorarchivos.ui.theme.Texto
import com.david.administradorarchivos.ui.theme.TextoSuave
import com.david.administradorarchivos.ui.theme.VerdeFab

@Composable
fun PantallaHistorial() {
    StubPantalla(
        icono = Icons.Filled.History,
        tituloEs = "Historial",
        tituloEn = "History",
        cuerpoEs = "Aquí aparecerán comandos y conexiones recientes. Próximamente.",
        cuerpoEn = "Recent commands and connections will show up here. Coming soon."
    )
}

@Composable
fun PantallaKnownHosts() {
    StubPantalla(
        icono = Icons.Filled.Fingerprint,
        tituloEs = "Known hosts",
        tituloEn = "Known hosts",
        cuerpoEs = "Huellas SSH conocidas y verificación de host. Próximamente.",
        cuerpoEn = "Known SSH fingerprints and host verification. Coming soon."
    )
}

@Composable
fun PantallaTemas() {
    StubPantalla(
        icono = Icons.Filled.Palette,
        tituloEs = "Temas",
        tituloEn = "Themes",
        cuerpoEs = "Tema actual: Oscuro (cian / verde neón CloudTerm Pro). Más paletas pronto.",
        cuerpoEn = "Current theme: Dark (CloudTerm Pro cyan / neon green). More palettes soon."
    )
}

@Composable
fun PantallaAyuda() {
    StubPantalla(
        icono = Icons.Filled.HelpOutline,
        tituloEs = "Ayuda",
        tituloEn = "Help",
        cuerpoEs = "Documentación y soporte de CloudTerm Pro. Consulta el README del proyecto.",
        cuerpoEn = "CloudTerm Pro docs and support. See the project README."
    )
}

@Composable
private fun StubPantalla(
    icono: ImageVector,
    tituloEs: String,
    tituloEn: String,
    cuerpoEs: String,
    cuerpoEn: String
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(FondoApp)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icono, contentDescription = null, tint = VerdeFab, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(12.dp))
            Text(
                Idioma.t(tituloEs, tituloEn),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Texto
            )
            Spacer(Modifier.height(8.dp))
            Text(
                Idioma.t(cuerpoEs, cuerpoEn),
                color = TextoSuave,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
