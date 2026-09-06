# CloudTerm Pro
[![Release](https://img.shields.io/github/v/release/pilahito/AdministradorArchivos?label=CloudTerm%20Pro)](https://github.com/pilahito/AdministradorArchivos/releases)

Cliente **SSH / SFTP / tÃºneles** multiplataforma, open source y sin servidores propios.
EvoluciÃ³n de CyberTerm â†’ arquitectura modular lista para Android, escritorio y sync en la nube.

[![Compilar APK](https://github.com/pilahito/AdministradorArchivos/actions/workflows/compilar-apk.yml/badge.svg)](https://github.com/pilahito/AdministradorArchivos/actions/workflows/compilar-apk.yml)
[![Android CI](https://github.com/pilahito/AdministradorArchivos/actions/workflows/android.yml/badge.svg)](https://github.com/pilahito/AdministradorArchivos/actions/workflows/android.yml)
[![Licencia](https://img.shields.io/github/license/pilahito/AdministradorArchivos)](LICENSE)
[![Release](https://img.shields.io/github/v/release/pilahito/AdministradorArchivos?include_prereleases)](https://github.com/pilahito/AdministradorArchivos/releases)

**Descarga:** [Releases](https://github.com/pilahito/AdministradorArchivos/releases) â†’ APK Android (`CloudTermPro-debug.apk` / `CyberTerm-debug.apk`).

---

## CaracterÃ­sticas

- Sesiones SSH con contraseÃ±a o clave privada
- Terminal interactiva + SFTP
- TÃºneles locales `-L`
- BÃ³veda cifrada AES-256 (PBKDF2) â€” mÃ³dulo `:vault`
- Sync opcional vÃ­a **tus** proveedores (WebDAV listo; Drive/Dropbox/MEGA/â€¦ stubs)
- Temas: Dark Cyan Neon, Dracula, Solarized Light, Cyberpunk
- Open source â€” sin paywall ni telemetrÃ­a a servidores de CloudTerm

## Privacidad

CloudTerm Pro **no opera servidores propios**. Las credenciales y la bÃ³veda viven en tu dispositivo.
La sincronizaciÃ³n (si la activas) habla solo con el proveedor que tÃº elijas (Nextcloud/WebDAV, Drive, etc.).

## Arquitectura

```mermaid
flowchart TB
  subgraph UI
    APP[:app Android Compose]
    DESK[app-desktop Tauri / CMP]
  end
  subgraph Core
    SSH[:core-ssh SSHJ]
    TERM[:terminal NewTermux bridge]
    VAULT[:vault AES-256]
    UI_SH[:ui-shared temas]
  end
  subgraph Sync
    ORCH[:cloud-sync]
    PROV[:sync-providers]
    WD[WebDAV]
    ST[Drive Dropbox MEGA ...]
  end
  APP --> SSH
  APP --> TERM
  APP --> VAULT
  APP --> UI_SH
  APP --> ORCH
  DESK -.-> SSH
  DESK -.-> VAULT
  ORCH --> PROV
  PROV --> WD
  PROV --> ST
  TERM --> SSH
```

## MÃ³dulos Gradle

| MÃ³dulo | Rol |
|--------|-----|
| `:app` | UI Android (Compose) â€” applicationId sin cambios |
| `:core-ssh` | API Kotlin: connect / startShell / exec / openSftp / createTunnel |
| `:vault` | vault.db cifrado + logs de acceso + stubs biomÃ©tricos |
| `:sync-providers` | `SyncProvider` + WebDAV + stubs nube |
| `:cloud-sync` | Orquestador (retry, `/Apps/CloudTermPro/`) |
| `:terminal` | `TerminalSession` / `SshTerminalBridge` â†’ NewTermux |
| `:ui-shared` | Tokens de color multiplataforma |

## Build

### Android (APK)

```bash
./gradlew :app:assembleDebug
# salida: app/build/outputs/apk/debug/app-debug.apk
```

### LibrerÃ­as JVM (sin SDK Android)

```bash
./gradlew :core-ssh:compileKotlin :vault:compileKotlin :sync-providers:compileKotlin
./gradlew :cloud-sync:compileKotlin :terminal:compileKotlin :ui-shared:compileKotlin
```

### Roadmap binarios

| Artefacto | Estado |
|-----------|--------|
| APK Android | CI actual (`compilar-apk.yml` / `android.yml`) |
| EXE escritorio | Stub Tauri / Compose Multiplatform en `app-desktop/` |
| DEB Linux | Workflow stub `deb.yml` |

Ver [docs/BUILD.md](docs/BUILD.md).

## Proveedores de sync

| Proveedor | Estado |
|-----------|--------|
| WebDAV (Nextcloud, ownCloud, â€¦) | ImplementaciÃ³n bÃ¡sica |
| MEGA | Stub |
| Google Drive | Stub (+ OAuth parcial en `:app`) |
| Dropbox / OneDrive / TeraBox / 1fichier | Stubs |

Ruta remota por defecto: `/Apps/CloudTermPro/`.

## Capturas por sistema operativo

| Android | Windows | Linux |
|:---:|:---:|:---:|
| ![Android](docs/screenshots/android.png) | ![Windows](docs/screenshots/windows.png) | ![Linux](docs/screenshots/linux.png) |

Mockups de producto (referencia UI): [docs/mockups/cloudterm-pro-mockups.png](docs/mockups/cloudterm-pro-mockups.png)

Registro de mejoras: [docs/MEJORAS.md](docs/MEJORAS.md) · Historial: [CHANGELOG.md](CHANGELOG.md)



## DocumentaciÃ³n

- [Arquitectura](docs/ARQUITECTURA.md)
- [Build](docs/BUILD.md)
- [Contribuir](CONTRIBUTING.md)
- [Seguridad](SECURITY.md)
- [Fuentes / licencias](docs/FUENTES.md)
- [Changelog](CHANGELOG.md)

## Licencia

Ver [LICENSE](LICENSE). Termux / Material Files **no se copian** (GPL); ver `docs/FUENTES.md`.