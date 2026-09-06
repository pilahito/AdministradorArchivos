# Historial de versiones

## v0.4.0-pro — CloudTerm Pro — 2026-09-06

Evolucion de **CyberTerm** a **CloudTerm Pro** (open source, multiplataforma).

- Nuevos modulos Gradle: `:core-ssh`, `:vault`, `:sync-providers`, `:cloud-sync`, `:terminal`, `:ui-shared`
- API SSH Kotlin sobre SSHJ (connect, startShell, exec, openSftp, createTunnel)
- Boveda AES-256-GCM + PBKDF2, access logs, stubs biometricos
- Sync: WebDAV (PROPFIND/GET/PUT/DELETE) + stubs MEGA/Drive/Dropbox/OneDrive/TeraBox/1fichier
- Orquestador `:cloud-sync` con retry y ruta `/Apps/CloudTermPro/`
- Puente terminal documentado para NewTermux
- Temas: Dark Cyan Neon, Dracula, Solarized Light, Cyberpunk
- Assets logo SVG + paletas JSON
- Docs ES: README, CONTRIBUTING, SECURITY, ARQUITECTURA, BUILD
- CI: android.yml, release.yml, stubs desktop.yml / deb.yml
- Scaffold app-desktop/ (Tauri / Compose Multiplatform)
- Nombre visible **CloudTerm Pro** (applicationId sin cambios)

## v0.3 — CyberTerm

- Barra superior CyberTerm; interfaz de Sesiones

## v0.1 — 2026-08-30

Primera version publica.

- Interfaz oscura tipo Termius (Hosts, Terminal, SFTP, Ajustes)
- Conexion SSH / SFTP con contrasena o clave
- Google Drive con login OAuth (hace falta Client ID Android)
- Compilacion automatica del APK en GitHub Actions