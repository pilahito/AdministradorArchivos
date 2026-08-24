package com.david.administradorarchivos.core.archivos

import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/**
 * .zip, .jar y .zap comparten el mismo formato contenedor (ZIP), así que
 * .jar y .zap se leen con la misma lógica nativa java.util.zip — no hacen
 * falta bibliotecas externas para esta parte.
 *
 * [Suposición]: ".zap" no es una extensión de archivo comprimido estándar
 * en el ecosistema Android/Java. La trato aquí como un contenedor ZIP
 * renombrado porque así lo especificaste, pero si en tu caso ".zap" tiene
 * una estructura interna distinta (por ejemplo cabeceras propias de alguna
 * herramienta concreta), esta clase no la va a reconocer correctamente.
 */
data class EntradaComprimido(
    val ruta: String,
    val esCarpeta: Boolean,
    val tamanoBytes: Long
)

object GestorComprimidos {

    private val EXTENSIONES_SOPORTADAS = setOf("zip", "jar", "zap")

    fun esComprimidoSoportado(nombreArchivo: String): Boolean {
        val extension = nombreArchivo.substringAfterLast('.', "").lowercase()
        return extension in EXTENSIONES_SOPORTADAS
    }

    /** Lista el árbol interno de entradas de un archivo comprimido. */
    fun listarContenido(archivo: File): List<EntradaComprimido> {
        ZipFile(archivo).use { zip ->
            return zip.entries().asSequence().map { entrada: ZipEntry ->
                EntradaComprimido(
                    ruta = entrada.name,
                    esCarpeta = entrada.isDirectory,
                    tamanoBytes = entrada.size
                )
            }.toList()
        }
    }

    /** Extrae una única entrada (archivo) al destino indicado. */
    fun extraerEntrada(archivo: File, rutaEntrada: String, destino: File) {
        ZipFile(archivo).use { zip ->
            val entrada = zip.getEntry(rutaEntrada)
                ?: throw IllegalArgumentException("La entrada '$rutaEntrada' no existe en el comprimido")
            destino.parentFile?.mkdirs()
            zip.getInputStream(entrada).use { entrada_in ->
                FileOutputStream(destino).use { salida ->
                    entrada_in.copyTo(salida)
                }
            }
        }
    }

    /** Extrae el comprimido completo (todas las entradas) a una carpeta destino. */
    fun extraerTodo(archivo: File, carpetaDestino: File) {
        ZipFile(archivo).use { zip ->
            zip.entries().asSequence().forEach { entrada ->
                val destino = File(carpetaDestino, entrada.name)
                if (entrada.isDirectory) {
                    destino.mkdirs()
                } else {
                    destino.parentFile?.mkdirs()
                    zip.getInputStream(entrada).use { entrada_in ->
                        FileOutputStream(destino).use { salida ->
                            entrada_in.copyTo(salida)
                        }
                    }
                }
            }
        }
    }
}
