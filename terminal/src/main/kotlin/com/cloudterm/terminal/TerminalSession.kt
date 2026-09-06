package com.cloudterm.terminal

/**
 * Sesión de terminal lógica (contrato JVM multiplataforma).
 *
 * En Android, el render real usa Termux `TerminalEmulator` + `TerminalRenderer`
 * (`SshTermuxView` en `:app`) puenteado a SSH PTY. Ver
 * `docs/licenses/TERMUX-NOTICE.md`.
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
 * Stub JVM — en Android se sustituye por SshTermuxView / Termux.
 */
class NewTermuxBridgeStub : TerminalEmulatorBridge {
    override fun attach(session: TerminalSession) = Unit
    override fun write(bytes: ByteArray) = Unit
    override fun resize(columns: Int, rows: Int) = Unit
    override fun detach() = Unit
}
