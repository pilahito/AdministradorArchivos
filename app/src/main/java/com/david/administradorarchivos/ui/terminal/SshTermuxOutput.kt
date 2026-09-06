package com.david.administradorarchivos.ui.terminal

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.termux.terminal.TerminalOutput

/**
 * [TerminalOutput] que reenvía las pulsaciones del emulador Termux al PTY SSH.
 */
class SshTermuxOutput(
    private val context: Context,
    private val onWriteToSsh: (ByteArray) -> Unit,
    private val onTitle: (String) -> Unit = {},
    private val onBell: () -> Unit = {}
) : TerminalOutput() {

    override fun write(data: ByteArray, offset: Int, count: Int) {
        if (count <= 0) return
        onWriteToSsh(data.copyOfRange(offset, offset + count))
    }

    override fun titleChanged(oldTitle: String?, newTitle: String?) {
        if (!newTitle.isNullOrBlank()) onTitle(newTitle)
    }

    override fun onCopyTextToClipboard(text: String?) {
        if (text.isNullOrEmpty()) return
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("terminal", text))
    }

    override fun onPasteTextFromClipboard() {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val paste = cm.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()
        if (!paste.isNullOrEmpty()) write(paste)
    }

    override fun onBell() = onBell.invoke()

    override fun onColorsChanged() = Unit
}
