# CloudTerm Pro

[![Release](https://img.shields.io/github/v/release/pilahito/AdministradorArchivos?label=CloudTerm%20Pro)](https://github.com/pilahito/AdministradorArchivos/releases/latest)
[![Android CI](https://github.com/pilahito/AdministradorArchivos/actions/workflows/android.yml/badge.svg)](https://github.com/pilahito/AdministradorArchivos/actions/workflows/android.yml)
[![Licencia GPL-3.0](https://img.shields.io/github/license/pilahito/AdministradorArchivos)](LICENSE)

**Cliente SSH / SFTP open source** (alternativa gratuita a apps de pago). Marca propia **CloudTerm Pro** - sin suscripcion, sin servidores nuestros y sin copiar marcas de terceros.

> Evolucion de CyberTerm a arquitectura modular Android + escritorio.

## Descargas

**Ultima release:** [v1.1.0-pro](https://github.com/pilahito/AdministradorArchivos/releases/tag/v1.1.0-pro)

| Plataforma | Archivo | Notas |
|-----------|--------|------|
| **Android** | [CloudTermPro-android-debug.apk](https://github.com/pilahito/AdministradorArchivos/releases/download/v1.1.0-pro/CloudTermPro-android-debug.apk) | Origenes desconocidos |
| **Windows** | [CloudTermPro-windows-x64-setup.exe](https://github.com/pilahito/AdministradorArchivos/releases/download/v1.1.0-pro/CloudTermPro-windows-x64-setup.exe) | Instalador NSIS |
| **Windows** | [CloudTermPro-windows-x64.msi](https://github.com/pilahito/AdministradorArchivos/releases/download/v1.1.0-pro/CloudTermPro-windows-x64.msi) | MSI |
| **Windows** | [CloudTermPro-windows-x64.exe](https://github.com/pilahito/AdministradorArchivos/releases/download/v1.1.0-pro/CloudTermPro-windows-x64.exe) | Portable; OpenSSH en PATH |
| **Linux** | deb / AppImage | En camino (CI); ver `app-desktop/` |

## Capturas

| Android | Windows | Linux (preview) |
|:---:|:---:|:---:|
| ![Android](docs/screenshots/android.png) | ![Windows](docs/screenshots/windows.png) | ![Linux](docs/screenshots/linux.png) |

## Que incluye

- **Hosts**, **terminal**, **SFTF**, **boveda** AES-256, **sync** WebDAV
- Android: Termux `TerminalView` - Desktop: Tauri 2 + xterm.js
- Temas Dark Neon / Dracula / Solarized / Cyberpunk
- UI por defecto en **espanol**

## Privacidad

No opera nube propia. Credenciales en tu dispositivo. Sync solo con el proveedor que elijas.

## Build

```bash
./gradlew :app:assembleDebug
cd app-desktop && nlm install && nlm run tauri build
```

Detalles: [BUILD.md](BUILD.md) - [app-desktop/README.md](app-desktop/README.md) - [docs/MEJORAS.md](docs/MEJORAS.md)

## Licencia

GPL-3.0-or-later - [LICENSE](LICENSE), [NOTICE](NOTICE), [docs/licenses/TERMUX-NOTICE.md](docs/licenses/TERMUX-NOTICE.md).

**No** incluye codigo ni assets de Termius u otros clientes comerciales.

## Enlaces

- [Releases](https://github.com/pilahito/AdministradorArchivos/releases)
- [SECURITY.md](SECURITY.md) - [CHANGELOG.md](CHANGELOG.md) - [docs/POST.md](docs/POST.md)

