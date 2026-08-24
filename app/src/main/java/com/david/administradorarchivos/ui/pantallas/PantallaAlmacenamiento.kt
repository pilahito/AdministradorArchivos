package com.david.administradorarchivos.ui.pantallas

import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.david.administradorarchivos.core.archivos.EntradaComprimido
import com.david.administradorarchivos.core.archivos.GestorComprimidos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private val EXTENSIONES_CODIGO = setOf("java", "kt", "py", "js", "html", "css", "xml", "json", "md")

private fun iconoPara(archivo: File): ImageVector = when {
    archivo.isDirectory -> Icons.Filled.Folder
    GestorComprimidos.esComprimidoSoportado(archivo.name) -> Icons.Filled.FolderZip
    archivo.extension.lowercase() in EXTENSIONES_CODIGO -> Icons.Filled.Description
    else -> Icons.Filled.InsertDriveFile
}

@Composable
fun PantallaAlmacenamiento() {
    val contexto = LocalContext.current
    val alcanceCorrutina = rememberCoroutineScope()

    var carpetaActual by remember { mutableStateOf(Environment.getExternalStorageDirectory()) }
    var archivosEnCarpeta by remember { mutableStateOf<List<File>>(emptyList()) }
    var contenidoComprimido by remember { mutableStateOf<List<EntradaComprimido>?>(null) }
    var comprimidoAbierto by remember { mutableStateOf<File?>(null) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    fun refrescar() {
        archivosEnCarpeta = carpetaActual.listFiles()?.sortedWith(
            compareByDescending<File> { it.isDirectory }.thenBy { it.name.lowercase() }
        ) ?: emptyList()
    }

    LaunchedEffect(carpetaActual) { refrescar() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Mi Almacenamiento", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(
            carpetaActual.absolutePath,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(12.dp))

        mensaje?.let {
            Text(it, color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
        }

        if (contenidoComprimido != null && comprimidoAbierto != null) {
            // Vista del árbol interno de un .zip / .jar / .zap
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dentro de: ${comprimidoAbierto?.name}", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { contenidoComprimido = null; comprimidoAbierto = null }) {
                    Text("Cerrar")
                }
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn {
                items(contenidoComprimido!!) { entrada ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable {
                                alcanceCorrutina.launch {
                                    withContext(Dispatchers.IO) {
                                        val destino = File(contexto.getExternalFilesDir(null), entrada.ruta.substringAfterLast('/'))
                                        GestorComprimidos.extraerEntrada(comprimidoAbierto!!, entrada.ruta, destino)
                                    }
                                    mensaje = "Extraído: ${entrada.ruta}"
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (entrada.esCarpeta) Icons.Filled.Folder else Icons.Filled.InsertDriveFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(entrada.ruta, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        } else {
            LazyColumn {
                items(archivosEnCarpeta) { archivo ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable {
                                when {
                                    archivo.isDirectory -> carpetaActual = archivo
                                    GestorComprimidos.esComprimidoSoportado(archivo.name) -> {
                                        alcanceCorrutina.launch {
                                            val entradas = withContext(Dispatchers.IO) {
                                                GestorComprimidos.listarContenido(archivo)
                                            }
                                            contenidoComprimido = entradas
                                            comprimidoAbierto = archivo
                                        }
                                    }
                                    else -> mensaje = "Abre '${archivo.name}' desde la pestaña Editor de Código"
                                }
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(iconoPara(archivo), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(archivo.name, style = MaterialTheme.typography.bodyLarge)
                            if (!archivo.isDirectory) {
                                Text(
                                    "${archivo.length() / 1024} KB",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}
