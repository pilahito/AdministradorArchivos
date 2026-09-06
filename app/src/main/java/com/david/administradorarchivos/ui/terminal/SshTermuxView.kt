package com.david.administradorarchivos.ui.terminal

import android.content.Context
import android.graphics.Canvas
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import com.termux.terminal.KeyHandler
import com.termux.terminal.TerminalEmulator
import com.termux.view.TerminalRenderer
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * Superficie Termux real: [TerminalEmulator] + [TerminalRenderer] (mismos artefactos
 * que `TerminalView` de terminal-view).
 *
 * `com.termux.terminal.TerminalSession` es `final` y siempre crea un PTY local vía JNI,
 * así que el puente SSH usa [TerminalEmulator.append] ← stdout SSH y
 * [TerminalOutput.write] → stdin SSH (ClienteSshj / JSch shell).
 *
 * Se monta en Compose con `AndroidView { SshTermuxView(...) }` — NO un Text stub.
 */
class SshTermuxView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val sessionClient = CloudTermSessionClient()
    private var renderer = TerminalRenderer(28, Typeface.MONOSPACE)
    private var emulator: TerminalEmulator? = null
    private var output: SshTermuxOutput? = null

    private var sshIn: InputStream? = null
    private var sshOut: OutputStream? = null
    private var readerThread: Thread? = null
    private val alive = AtomicBoolean(false)

    private var ctrlHeld = false
    private var altHeld = false
    private var onResizePty: ((cols: Int, rows: Int) -> Unit)? = null
    private var attachedKey: String? = null

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        setBackgroundColor(0xFF000000.toInt())
    }

    fun attachSshStreams(
        input: InputStream,
        outputStream: OutputStream,
        key: String = "ssh",
        onResize: (cols: Int, rows: Int) -> Unit = { _, _ -> }
    ) {
        if (attachedKey == key && alive.get() && emulator != null) {
            onResizePty = onResize
            return
        }
        detach()
        attachedKey = key
        sshIn = input
        sshOut = outputStream
        onResizePty = onResize
        alive.set(true)

        val out = SshTermuxOutput(
            context = context,
            onWriteToSsh = { bytes ->
                try {
                    sshOut?.write(bytes)
                    sshOut?.flush()
                } catch (_: Exception) {
                }
            }
        )
        output = out
        ensureEmulator(80, 24)
        startReader()
        invalidate()
    }

    fun attachDemoBanner(message: String) {
        val key = "demo:" + message.hashCode()
        if (attachedKey == key && emulator != null) return
        detach()
        attachedKey = key
        alive.set(true)
        val out = SshTermuxOutput(context, onWriteToSsh = { })
        output = out
        ensureEmulator(80, 24)
        val bytes = (message + "\r\n").toByteArray(Charsets.UTF_8)
        emulator?.append(bytes, bytes.size)
        invalidate()
    }

    fun detach() {
        alive.set(false)
        readerThread?.interrupt()
        readerThread = null
        sshIn = null
        sshOut = null
        emulator = null
        output = null
        attachedKey = null
    }

    private fun ensureEmulator(cols: Int, rows: Int) {
        val out = output ?: return
        val existing = emulator
        if (existing == null) {
            emulator = TerminalEmulator(out, cols, rows, 2000, sessionClient)
        } else if (existing.mColumns != cols || existing.mRows != rows) {
            existing.resize(cols, rows)
            onResizePty?.invoke(cols, rows)
        }
    }

    private fun startReader() {
        val stream = sshIn ?: return
        readerThread = thread(name = "SshTermuxReader", isDaemon = true) {
            val buf = ByteArray(4096)
            try {
                while (alive.get()) {
                    val n = stream.read(buf)
                    if (n <= 0) break
                    val chunk = buf.copyOf(n)
                    post {
                        emulator?.append(chunk, chunk.size)
                        invalidate()
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w <= 0 || h <= 0 || output == null) return
        val fontW = renderer.getFontWidth()
        val fontH = renderer.getFontLineSpacing()
        val cols = (w / fontW).toInt().coerceAtLeast(4)
        val rows = (h / fontH).coerceAtLeast(4)
        ensureEmulator(cols, rows)
        onResizePty?.invoke(cols, rows)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val emu = emulator
        if (emu == null) {
            canvas.drawColor(0xFF000000.toInt())
            return
        }
        renderer.render(emu, canvas, 0, -1, -1, -1, -1)
    }

    override fun onCheckIsTextEditor(): Boolean = true

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        outAttrs.inputType = EditorInfo.TYPE_NULL
        outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_FULLSCREEN
        return object : BaseInputConnection(this, true) {
            override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
                if (text != null) writeText(text.toString())
                return true
            }

            override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
                repeat(beforeLength.coerceAtLeast(0)) {
                    writeBytes(byteArrayOf(0x7F))
                }
                return true
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            requestFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }
        return true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val emu = emulator ?: return super.onKeyDown(keyCode, event)
        var mod = 0
        if (event.isCtrlPressed || ctrlHeld) mod = mod or KeyHandler.KEYMOD_CTRL
        if (event.isAltPressed || altHeld) mod = mod or KeyHandler.KEYMOD_ALT
        if (event.isShiftPressed) mod = mod or KeyHandler.KEYMOD_SHIFT
        val code = KeyHandler.getCode(
            keyCode,
            mod,
            emu.isCursorKeysApplicationMode,
            emu.isKeypadApplicationMode
        )
        if (code != null) {
            writeText(code)
            return true
        }
        val ch = event.unicodeChar
        if (ch != 0) {
            writeCodePoint(ch)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    fun setCtrl(held: Boolean) {
        ctrlHeld = held
    }

    fun setAlt(held: Boolean) {
        altHeld = held
    }

    fun sendExtraKey(label: String) {
        val emu = emulator ?: return
        when (label.lowercase()) {
            "tab" -> writeBytes(byteArrayOf(0x09))
            "esc" -> writeBytes(byteArrayOf(0x1B))
            "ctrl" -> ctrlHeld = !ctrlHeld
            "alt" -> altHeld = !altHeld
            "←", "left" -> writeText(
                KeyHandler.getCode(
                    KeyEvent.KEYCODE_DPAD_LEFT, 0,
                    emu.isCursorKeysApplicationMode, emu.isKeypadApplicationMode
                ) ?: "\u001b[D"
            )
            "↑", "up" -> writeText(
                KeyHandler.getCode(
                    KeyEvent.KEYCODE_DPAD_UP, 0,
                    emu.isCursorKeysApplicationMode, emu.isKeypadApplicationMode
                ) ?: "\u001b[A"
            )
            "↓", "down" -> writeText(
                KeyHandler.getCode(
                    KeyEvent.KEYCODE_DPAD_DOWN, 0,
                    emu.isCursorKeysApplicationMode, emu.isKeypadApplicationMode
                ) ?: "\u001b[B"
            )
            "→", "right" -> writeText(
                KeyHandler.getCode(
                    KeyEvent.KEYCODE_DPAD_RIGHT, 0,
                    emu.isCursorKeysApplicationMode, emu.isKeypadApplicationMode
                ) ?: "\u001b[C"
            )
            else -> writeText(label)
        }
        invalidate()
    }

    private fun writeText(s: String) {
        writeBytes(s.toByteArray(Charsets.UTF_8))
    }

    private fun writeCodePoint(codePoint: Int) {
        var cp = codePoint
        if (ctrlHeld && cp in 'a'.code..'z'.code) {
            cp = cp - 'a'.code + 1
            ctrlHeld = false
        }
        val chars = Character.toChars(cp)
        val prefix = if (altHeld) byteArrayOf(0x1B) else byteArrayOf()
        altHeld = false
        writeBytes(prefix + String(chars).toByteArray(Charsets.UTF_8))
    }

    private fun writeBytes(data: ByteArray) {
        try {
            sshOut?.write(data)
            sshOut?.flush()
        } catch (_: Exception) {
        }
        // Eco local si no hay SSH (banner demo): alimentar emulador
        if (sshOut == null) {
            emulator?.append(data, data.size)
            invalidate()
        }
    }

    override fun onDetachedFromWindow() {
        detach()
        super.onDetachedFromWindow()
    }
}
