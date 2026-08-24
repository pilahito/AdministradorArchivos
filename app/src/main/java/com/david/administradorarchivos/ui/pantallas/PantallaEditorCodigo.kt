package com.david.administradorarchivos.ui.pantallas

import android.os.Environment
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.david.administradorarchivos.core.editor.resaltarCodigo
import com.david.administradorarchivos.ui.theme.FuenteMonoespaciada
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private val EXTENSIONES_CODIGO = setOf("java", "kt", "py", "js", "html", "css")

private fun buscarArchivosDeCodigo(raiz: File, limite: Int = 200): List<File> {
    val resultado = mutableListOf<File>()
    fun recorrer(carpeta: File) {
        if (resultado.size >= limite) return
        carpeta.listFiles()?.forEach { hijo ->
            if (resultado.size >= limite) return
            if (hijo.isDirectory) recorrer(hijo)
            else if (hijo.extension.lowercase() in EXTENSIONES_CODIGO) resultado.add(hijo)
        }
    }
    recorrer(raiz)
    return resultado
}

@Composable
fun PantallaEditorCodigo() {
    val alcanceCorrutina = rememberCoroutineScope()

    var archivoSeleccionado by remember { mutableStateOf<File?>(null) }
    var contenidoArchivo by remember { mutableStateOf("") }
    var archivosDisponibles by remember { mutableStateOf<List<File>>(emptyList()) }

    LaunchedEffect(Unit) {
        archivosDisponibles = withContext(Dispatchers.IO) {
            buscarArchivosDeCodigo(Environment.getExternalStorageDirectory())
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Editor de Código", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        if (archivoSeleccionado == null) {
            Text(
                "Archivos de código encontrados en el almacenamiento:",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            LazyColumn {
                items(archivosDisponibles) { archivo ->
                    Text(
                        archivo.path,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                alcanceCorrutina.launch {
                                    contenidoArchivo = withContext(Dispatchers.IO) { archivo.readText() }
                                    archivoSeleccionado = archivo
                                }
                            }
                            .padding(vertical = 8.dp)
                    )
                    HorizontalDivider()
                }
            }
        } else {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(archivoSeleccionado!!.name, style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { archivoSeleccionado = null }) { Text("Volver") }
            }
            Spacer(Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    resaltarCodigo(contenidoArchivo, archivoSeleccionado!!.extension),
                    fontFamily = FuenteMonoespaciada,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                )
            }
        }
    }
}
