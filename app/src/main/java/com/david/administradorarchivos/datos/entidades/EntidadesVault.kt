package com.david.administradorarchivos.datos.entidades

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "grupos")
data class GrupoEntidad(
    @PrimaryKey val id: String,
    val nombre: String,
    val color: Int = 0,
    val orden: Int = 0
)

@Entity(
    tableName = "hosts",
    foreignKeys = [
        ForeignKey(
            entity = GrupoEntidad::class,
            parentColumns = ["id"],
            childColumns = ["grupoId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("grupoId")]
)
data class HostEntidad(
    @PrimaryKey val id: String,
    val alias: String,
    val direccion: String,
    val puerto: Int = 22,
    val usuario: String,
    val contrasena: String = "",
    val claveId: String? = null,
    val protocolo: String = "SSH",
    val grupoId: String? = null,
    val distro: String = "linux",
    val color: Int = 0,
    val etiquetas: String = "ssh"
)

@Entity(tableName = "claves")
data class ClaveEntidad(
    @PrimaryKey val id: String,
    val nombre: String,
    val tipo: String = "ED25519",
    val rutaPrivada: String = "",
    val publica: String = "",
    val comentario: String = ""
)

@Entity(
    tableName = "tuneles",
    foreignKeys = [
        ForeignKey(
            entity = HostEntidad::class,
            parentColumns = ["id"],
            childColumns = ["hostId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("hostId")]
)
data class TunelEntidad(
    @PrimaryKey val id: String,
    val hostId: String,
    val tipo: String, // LOCAL, REMOTO, SOCKS5
    val puertoLocal: Int,
    val destinoHost: String = "127.0.0.1",
    val destinoPuerto: Int = 0,
    val autoArranque: Boolean = false,
    val activo: Boolean = false
)

@Entity(tableName = "snippets")
data class SnippetEntidad(
    @PrimaryKey val id: String,
    val nombre: String,
    val comando: String,
    val etiquetas: String = ""
)
