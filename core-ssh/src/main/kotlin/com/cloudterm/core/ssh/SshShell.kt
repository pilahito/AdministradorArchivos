package com.cloudterm.core.ssh

import java.io.InputStream
import java.io.OutputStream

/**
 * Shell interactivo SSH con PTY.
 * Cerrar con [close] o usar use {}.
 */
interface SshShell : AutoCloseable {
    val input: InputStream
    val output: OutputStream
    val error: InputStream
    fun resizePty(columns: Int, rows: Int)
}