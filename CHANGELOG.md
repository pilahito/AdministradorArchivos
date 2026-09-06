# Historial de versiones

## v1.1.0-pro — Terminal Termux + UI mockup — 2026-09-06

- Terminal real: TerminalEmulator + TerminalRenderer (Termux 0.118 via JitPack) en AndroidView (SshTermuxView)
- Puente SSH PTY: ClienteSshj.abrirShell() <-> emulador (bytes stdin/stdout)
- Extra keys: Alt, Ctrl, Esc, Tab, flechas
- UI glass dark navy / cyan #00E5FF / neon #39FF14; bottom nav Hosts|Terminal|SFTP|Boveda|Ajustes
- NOTICE GPL Termux en docs/licenses/TERMUX-NOTICE.md
## v1.0.0-pro — CloudTerm Pro (lanzamiento oficial) — 2026-09-06

Primera publicacion oficial de **CloudTerm Pro** (evolucion de CyberTerm).

### Registros de mejora

| Area | Mejora |
|------|--------|
| Identidad | Renombre visible a CloudTerm Pro; UI por defecto en espanol |
| Arquitectura | Modulos Gradle `:core-ssh`, `:vault`, `:sync-providers`, `:cloud-sync`, `:terminal`, `:ui-shared` |
| SSH/SFTP | Cliente SSHJ (shell, exec, SFTP streams, tuneles) |
| Vault | AES-256-GCM + PBKDF2, access logs, pantalla Boveda de claves |
| Sync nube | WebDAV real + stubs MEGA / Drive / Dropbox / OneDrive / TeraBox / 1fichier |
| UI | Tema Dark Cyan Neon alineado a mockups (hosts grid, terminal tabs, SFTP dual, ajustes) |
| Desktop | Scaffold `app-desktop/` (Tauri / Compose Multiplatform) — preview |
| CI/CD | Android CI (JVM + APK), release por tags, workflows desktop/deb (stub) |
| Docs | README multi-SO, SECURITY, BUILD, ARQUITECTURA, capturas Android/Windows/Linux |

### Plataformas

- **Android** — APK debug/release via CI (estable en esta release)
- **Windows** — preview desktop (scaffold); captura de producto incluida
- **Linux** — preview desktop / DEB planificado; captura de producto incluida

## v0.4.0-pro — CloudTerm Pro — 2026-09-06

- Scaffold modular + vault + sync + CI inicial
- UI mockups (hosts, terminal, SFTP, ajustes, boveda)

## v0.3 — CyberTerm

- Barra superior CyberTerm; interfaz de Sesiones

## v0.1 — 2026-08-30

Primera version publica (CyberTerm).

- Interfaz oscura tipo Termius (Hosts, Terminal, SFTP, Ajustes)
- Conexion SSH / SFTP con contrasena o clave
- Google Drive OAuth (Client ID Android)
- Compilacion automatica del APK en GitHub Actions