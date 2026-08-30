# Sesiones

Cliente **SSH / SFTP / Google Drive** para Android.
Es **nuestra versión**, no Termius: mismas ideas (sesiones, terminal, archivos), sin pago ni cuenta obligatoria.

[![Compilar APK](https://github.com/pilahito/AdministradorArchivos/actions/workflows/compilar-apk.yml/badge.svg)](https://github.com/pilahito/AdministradorArchivos/actions/workflows/compilar-apk.yml)
[![Releases](https://img.shields.io/github/v/release/pilahito/AdministradorArchivos?include_prereleases)](https://github.com/pilahito/AdministradorArchivos/releases)

**Paquete:** `com.david.administradorarchivos`  
**Android:** 8.0+ (API 26)

---

## Descargar APK

1. Entra a **[Releases](https://github.com/pilahito/AdministradorArchivos/releases)**
2. Abre la versión de arriba (**Latest**)
3. Baja `Hosts-debug.apk`
4. En el teléfono: permite *orígenes desconocidos* e instala

Cada push a `main` compila un APK y lo publica en Releases.

---

## Qué incluye (todo gratis)

| Apartado | Qué hace |
|---|---|
| **Sesiones** | Lista vacía al inicio. Botón **Nueva sesión** para crear la tuya |
| **Terminal** | Comandos SSH + barra tab / esc / ctrl |
| **SFTP** | Archivos del servidor conectado |
| **Ajustes** | Español/English, llavero SSH, Google Drive OAuth |

No hay hosts de ejemplo. Tú pones el nombre de cada sesión.

---

## Google Drive

1. Proyecto en [Google Cloud Console](https://console.cloud.google.com)
2. Activa **Google Drive API**
3. Pantalla de consentimiento OAuth + tu Gmail de prueba
4. Credenciales → **ID de cliente OAuth** → tipo **Android**

| Campo | Valor |
|---|---|
| Paquete | `com.david.administradorarchivos` |
| SHA-1 | el de este APK: `keytool -printcert -jarfile Hosts-debug.apk` |

---

## Limitaciones

- Terminal: un comando → una respuesta (no `vim` / `htop` interactivo)
- APK de **debug** (pruebas, no Play Store)
- No es Termius ni desbloquea Termius Pro
