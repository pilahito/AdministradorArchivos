package com.david.administradorarchivos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// La app usa siempre esquema oscuro (estética estilo Termius): fondo casi
// negro, acentos cian/violeta, verde para indicar conexión activa.
private val EsquemaOscuro = darkColorScheme(
    primary = AcentoCian,
    secondary = AcentoVioleta,
    tertiary = VerdeConectado,
    background = FondoPrincipal,
    surface = FondoTarjeta,
    surfaceVariant = FondoTarjetaElevada,
    error = RojoError,
    onPrimary = FondoPrincipal,
    onBackground = TextoPrimario,
    onSurface = TextoPrimario,
    outline = BordeSutil
)

@Composable
fun TemaAdministradorArchivos(contenido: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaOscuro,
        typography = TipografiaApp,
        content = contenido
    )
}
