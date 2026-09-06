package com.cloudterm.terminal

import com.cloudterm.core.ssh.SshClient
import com.cloudterm.core.ssh.SshShell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive

/**
 * Puente SSH ↔ emulador de terminal.
 *
 * Stub funcional mínimo: abre shell SSHJ y reenvía bytes al [TerminalEmulatorBridge].
 */
class SshTerminalBridge(
    private val ssh: SshClient,
    private val emulator: TerminalEmulatorBridge = NewTermuxBridgeStub(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private var shell: SshShell? = null
    private var readerJob: Job? = null
    var session: TerminalSession? = null
        private set

    fun start(sessionId: String, title: String = "SSH") {
        val term = TerminalSession(id = sessionId, title = title, state = TerminalState.CONNECTING)
        session = term
        emulator.attach(term)
        if (!ssh.isConnected) ssh.connect()
        shell = ssh.startShell()
        session = term.copy(state = TerminalState.LIVE)
        readerJob = scope.launch {
            val input = shell?.input ?: return@launch
            val buf = ByteArray(4096)
            while (isActive) {
                val n = try { input.read(buf) } catch (_: Exception) { -1 }
                if (n <= 0) break
                emulator.write(buf.copyOf(n))
            }
            session = session?.copy(state = TerminalState.CLOSED)
        }
    }

    fun send(data: ByteArray) {
        shell?.output?.write(data)
        shell?.output?.flush()
    }

    fun resize(columns: Int, rows: Int) {
        shell?.resizePty(columns, rows)
        emulator.resize(columns, rows)
        session = session?.copy(columns = columns, rows = rows)
    }

    fun stop() {
        readerJob?.cancel()
        try { shell?.close() } catch (_: Exception) {}
        shell = null
        emulator.detach()
        session = session?.copy(state = TerminalState.CLOSED)
    }
}