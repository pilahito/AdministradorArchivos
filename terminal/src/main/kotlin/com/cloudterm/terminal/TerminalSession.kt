package com.cloudterm.terminal

/**
 * Sesión de terminal lógica.
 *
 * Integración prevista con **NewTermux** / emulador VT (proyecto hermano GPL
 * o binding propio). Este módulo solo define el contrato; no embebe código GPL.
 *
 * ## NewTermux
 * - Render: `TerminalView` / superficie propia
 * - I/O: puente [SshTerminalBridge] ↔ `SshShell` de `:core-ssh`
 * - Colores: tokens de `:ui-shared`
 */
data class TerminalSession(
    val id: String,
    val title: String,
    val columns: Int = 80,
    val rows: Int = 24,
    val state: TerminalState = TerminalState.IDLE
)

enum class TerminalState {
    IDLE,
    CONNECTING,
    LIVE,
    CLOSED,
    ERROR
}

interface TerminalEmulatorBridge {
    fun attach(session: TerminalSession)
    fun write(bytes: ByteArray)
    fun resize(columns: Int, rows: Int)
    fun detach()
}

/**
 * Stub de emulador — documenta el punto de enganche NewTermux.
 */
class NewTermuxBridgeStub : TerminalEmulatorBridge {
    override fun attach(session: TerminalSession) {
        // TODO: enlazar con NewTermux / termux-view (licencia aparte)
    }
    override fun write(bytes: ByteArray) = Unit
    override fun resize(columns: Int, rows: Int) = Unit
    override fun detach() = Unit
}