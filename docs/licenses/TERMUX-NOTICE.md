# NOTICE — Termux terminal libraries (GPL-3.0)

CloudTerm Pro integra artefactos **GPL-3.0** de [termux/termux-app](https://github.com/termux/termux-app)
vía JitPack:

- `com.github.termux.termux-app:terminal-emulator:v0.118.0`
- `com.github.termux.termux-app:terminal-view:v0.118.0`

## Qué se usa

- `TerminalEmulator` — motor VT / escape sequences
- `TerminalRenderer` — rasterizado a `Canvas` (misma pila que `TerminalView`)
- `KeyHandler` — códigos de teclas especiales

`TerminalSession` de Termux es `final` y siempre crea un PTY local con JNI.
Para SSH, CloudTerm puentea el emulador a un shell PTY remoto (`ClienteSshj`):

- stdout SSH → `TerminalEmulator.append(...)`
- teclas / `TerminalOutput.write(...)` → stdin SSH

La UI Compose monta la superficie con `AndroidView { SshTermuxView }`
(equivalente funcional a `TerminalView`, sin stub `Text`).

## Implicación de licencia

Al enlazar bibliotecas GPL-3.0, la **distribución del APK** que las incluye
queda sujeta a las obligaciones GPL-3.0 respecto a ese código (y, según
interpretación de enlace, posiblemente al binario completo).

Código propio de CloudTerm Pro permanece bajo la licencia del repositorio
(`LICENSE`). Este NOTICE documenta la dependencia GPL externa.

Fuente: https://github.com/termux/termux-app (GPL-3.0)
