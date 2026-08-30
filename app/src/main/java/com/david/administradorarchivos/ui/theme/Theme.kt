package com.david.administradorarchivos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Esquema = darkColorScheme(
    primary = AzulAccion,
    secondary = VerdeFab,
    tertiary = VerdeConectado,
    background = FondoApp,
    surface = FondoTarjeta,
    surfaceVariant = FondoTarjetaHover,
    error = Rojo,
    onPrimary = Color.White,
    onBackground = Texto,
    onSurface = Texto,
    outline = Linea
)

@Composable
fun TemaAdministradorArchivos(contenido: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Esquema, typography = TipografiaApp, content = contenido)
}
