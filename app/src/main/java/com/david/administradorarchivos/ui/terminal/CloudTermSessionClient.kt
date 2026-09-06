package com.david.administradorarchivos.ui.terminal

import android.util.Log
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient

/**
 * Cliente mínimo de sesión Termux (solo logging / cursor).
 * No usamos [TerminalSession] local: el I/O va por SSH.
 */
class CloudTermSessionClient : TerminalSessionClient {
    override fun onTextChanged(changedSession: TerminalSession?) = Unit
    override fun onTitleChanged(changedSession: TerminalSession?) = Unit
    override fun onSessionFinished(finishedSession: TerminalSession?) = Unit
    override fun onCopyTextToClipboard(session: TerminalSession?, text: String?) = Unit
    override fun onPasteTextFromClipboard(session: TerminalSession?) = Unit
    override fun onBell(session: TerminalSession?) = Unit
    override fun onColorsChanged(session: TerminalSession?) = Unit
    override fun onTerminalCursorStateChange(state: Boolean) = Unit
    override fun getTerminalCursorStyle(): Int? = null

    override fun logError(tag: String?, message: String?) {
        Log.e(tag ?: "CloudTerm", message ?: "")
    }

    override fun logWarn(tag: String?, message: String?) {
        Log.w(tag ?: "CloudTerm", message ?: "")
    }

    override fun logInfo(tag: String?, message: String?) {
        Log.i(tag ?: "CloudTerm", message ?: "")
    }

    override fun logDebug(tag: String?, message: String?) {
        Log.d(tag ?: "CloudTerm", message ?: "")
    }

    override fun logVerbose(tag: String?, message: String?) {
        Log.v(tag ?: "CloudTerm", message ?: "")
    }

    override fun logStackTraceWithMessage(tag: String?, message: String?, e: Exception?) {
        Log.e(tag ?: "CloudTerm", message, e)
    }

    override fun logStackTrace(tag: String?, e: Exception?) {
        Log.e(tag ?: "CloudTerm", "stack", e)
    }
}
