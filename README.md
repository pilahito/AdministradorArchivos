# Sesiones

Suite **open source** de administración remota para Android:
SSH, terminal, SFTP, túneles, snippets y llavero.

No es Termius. No hay pago ni telemetría comercial.

[![Compilar APK](https://github.com/pilahito/AdministradorArchivos/actions/workflows/compilar-apk.yml/badge.svg)](https://github.com/pilahito/AdministradorArchivos/actions/workflows/compilar-apk.yml)
[![Releases](https://img.shields.io/github/v/release/pilahito/AdministradorArchivos?include_prereleases)](https://github.com/pilahito/AdministradorArchivos/releases)

**Paquete:** `com.david.administradorarchivos`  
**Android 8+** · paleta Slate / cian / esmeralda

---

## Descargar

https://github.com/pilahito/AdministradorArchivos/releases

Instala `Hosts-debug.apk` (permite orígenes desconocidos).

---

## Qué hay ahora

| Módulo | Qué hace |
|---|---|
| Sesiones | Crear / guardar / conectar hosts |
| Terminal | Comandos SSH + teclas tab/esc/ctrl |
| SFTP | Listar archivos del servidor |
| Snippets | Comandos de un toque |
| Túneles | Reenvío local `-L` con SSHJ |
| Ajustes | Idioma, llavero, Google Drive |

Librerías reales: **JSch**, **SSHJ**, **Room**, Commons Net.  
No se copia Termux ni Material Files (GPL).

Detalle: [docs/LIBRERIAS.md](docs/LIBRERIAS.md) · [docs/ARQUITECTURA.md](docs/ARQUITECTURA.md)

---

## Qué no entra en este APK

- Emulador VT100 de Termux
- Doble panel de Material Files
- SQLCipher nativo / SMB / S3 (siguiente bloque)
- ServerBox (es Flutter)

---

## Uso rápido

1. **Nueva sesión** → IP, puerto, usuario, contraseña
2. Toca la tarjeta → Terminal
3. Menú ☰ → Snippets o Túneles
