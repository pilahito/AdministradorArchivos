package com.david.administradorarchivos.ui.pantallas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.david.administradorarchivos.core.datos.AlmacenClaves
import com.david.administradorarchivos.core.datos.ClaveSsh
import com.david.administradorarchivos.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaBoveda() {
    val ctx = LocalContext.current
    val llavero = remember { AlmacenClaves(ctx) }
    var claves by remember { mutableStateOf(llavero.listar()) }
    var mostrarAnadir by remember { mutableStateOf(false) }
    var seleccionadas by remember { mutableStateOf(setOf<String>()) }
    var info by remember { mutableStateOf<String?>(null) }
    var nombreClave by remember { mutableStateOf("") }
    var rutaClave by remember { mutableStateOf("") }

    fun refrescar() { claves = llavero.listar() }

    Column(Modifier.fillMaxSize().background(FondoApp).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text(
                    Idioma.t("Bóveda de Claves", "Key Vault"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Texto
                )
                Text(
                    Idioma.t("Almacenamiento local cifrado de claves SSH", "Local encrypted SSH key storage"),
                    color = TextoSuave,
                    fontSize = 12.sp
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { mostrarAnadir = true },
                colors = ButtonDefaults.buttonColors(containerColor = AzulAccion, contentColor = FondoApp),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Filled.Add, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(Idioma.t("Añadir", "Add"))
            }
            OutlinedButton(
                onClick = {
                    info = Idioma.t(
                        "Exportadas ${claves.size} claves (ruta local).",
                        "Exported ${claves.size} keys (local path)."
                    )
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Linea),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Filled.FileDownload, null, Modifier.size(16.dp), tint = TextoSuave)
                Spacer(Modifier.width(4.dp))
                Text(Idioma.t("Exportar", "Export"), color = Texto)
            }
            OutlinedButton(
                onClick = {
                    val ids = if (seleccionadas.isEmpty()) claves.map { it.id } else seleccionadas.toList()
                    ids.forEach { llavero.borrar(it) }
                    seleccionadas = emptySet()
                    refrescar()
                    info = Idioma.t("Claves eliminadas", "Keys deleted")
                },
                enabled = claves.isNotEmpty(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Rojo.copy(alpha = 0.5f)),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Filled.Delete, null, Modifier.size(16.dp), tint = Rojo)
                Spacer(Modifier.width(4.dp))
                Text(Idioma.t("Eliminar", "Delete"), color = Rojo)
            }
        }
        info?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = TextoSuave, fontSize = 12.sp)
        }
        Spacer(Modifier.height(14.dp))

        if (claves.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.VpnKey, null, tint = TextoSuave, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(Idioma.t("Bóveda vacía", "Vault empty"), color = TextoSuave)
                    Text(
                        Idioma.t("Pulsa Añadir para guardar una clave", "Tap Add to store a key"),
                        color = TextoSuave,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(claves, key = { it.id }) { c ->
                    val sel = c.id in seleccionadas
                    Card(
                        onClick = {
                            seleccionadas = if (sel) seleccionadas - c.id else seleccionadas + c.id
                        },
                        colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, if (sel) AzulAccion else Linea)
                    ) {
                        Column(
                            Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(AzulAccion.copy(alpha = 0.12f))
                                    .border(1.dp, AzulAccion.copy(alpha = 0.45f), RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Shield, null, tint = AzulAccion, modifier = Modifier.size(28.dp))
                                Icon(
                                    Icons.Filled.VpnKey,
                                    null,
                                    tint = VerdeNeon,
                                    modifier = Modifier.size(14.dp).align(Alignment.BottomEnd).offset(x = (-6).dp, y = (-6).dp)
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(
                                c.nombre,
                                color = Texto,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                maxLines = 2
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                ChipTech("RSA")
                                ChipTech("AES-256")
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(c.ruta, color = TextoSuave, fontSize = 10.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
    }

    if (mostrarAnadir) {
        AlertDialog(
            onDismissRequest = { mostrarAnadir = false },
            containerColor = FondoBarra,
            title = { Text(Idioma.t("Añadir clave", "Add key"), color = Texto) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        nombreClave,
                        { nombreClave = it },
                        label = { Text(Idioma.t("Nombre", "Name")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AzulAccion,
                            unfocusedBorderColor = Linea,
                            focusedTextColor = Texto,
                            unfocusedTextColor = Texto
                        )
                    )
                    OutlinedTextField(
                        rutaClave,
                        { rutaClave = it },
                        label = { Text(Idioma.t("Ruta del archivo", "File path")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AzulAccion,
                            unfocusedBorderColor = Linea,
                            focusedTextColor = Texto,
                            unfocusedTextColor = Texto
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (nombreClave.isNotBlank() && rutaClave.isNotBlank()) {
                            llavero.guardar(ClaveSsh(nombre = nombreClave.trim(), ruta = rutaClave.trim()))
                            refrescar()
                            nombreClave = ""
                            rutaClave = ""
                            mostrarAnadir = false
                        }
                    }
                ) { Text(Idioma.t("Guardar", "Save"), color = AzulAccion) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarAnadir = false }) {
                    Text(Idioma.t("Cancelar", "Cancel"), color = TextoSuave)
                }
            }
        )
    }
}

@Composable
private fun ChipTech(label: String) {
    Surface(
        color = FondoBarra,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, Linea)
    ) {
        Text(
            label,
            color = TextoSuave,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}