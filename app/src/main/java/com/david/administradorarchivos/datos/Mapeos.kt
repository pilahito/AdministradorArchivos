package com.david.administradorarchivos.datos

import com.david.administradorarchivos.core.datos.HostGuardado
import com.david.administradorarchivos.datos.entidades.HostEntidad

fun HostEntidad.aGuardado() = HostGuardado(
    id = id,
    alias = alias,
    direccion = direccion,
    puerto = puerto,
    usuario = usuario,
    contrasena = contrasena,
    rutaClave = claveId.orEmpty(),
    protocolo = protocolo,
    etiquetas = etiquetas,
    color = color
)

fun HostGuardado.aEntidad() = HostEntidad(
    id = id,
    alias = alias,
    direccion = direccion,
    puerto = puerto,
    usuario = usuario,
    contrasena = contrasena,
    claveId = rutaClave.ifBlank { null },
    protocolo = protocolo,
    grupoId = null,
    distro = "linux",
    color = color,
    etiquetas = etiquetas
)
