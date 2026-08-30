# Hosts

Cliente **SSH / SFTP / Google Drive** para Android.
Interfaz oscura inspirada en Termius: lista de servidores, terminal, archivos y ajustes.

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

Cada vez que se sube código a `main`, GitHub:

- Compila un APK nuevo
- Publica un **Release** con ese APK

Si el Release aún no aparece, mira [Actions](https://github.com/pilahito/AdministradorArchivos/actions) (tiene que estar en verde).

---

## Qué incluye

| Apartado | Qué hace |
|---|---|
| **Hosts** | Lista de servidores, búsqueda, botón + para crear uno |
| **Terminal** | Enviar comandos SSH y ver la salida |
| **SFTP** | Navegar archivos del host conectado |
| **Ajustes** | Google Drive con pantalla de permisos (OAuth) |

Autenticación SSH: contraseña o clave (ruta a un archivo en el móvil).

---

## Google Drive (obligatorio para el botón)

Sin esto, **Conectar con Google** falla.

1. Crea un proyecto en [Google Cloud Console](https://console.cloud.google.com)
2. Habilita **Google Drive API**
3. Configura la **pantalla de consentimiento OAuth** (tipo *Externo*)
4. Añade tu Gmail en **usuarios de prueba**
5. Crea credenciales → **ID de cliente OAuth** → tipo **Android**

| Campo | Valor |
|---|---|
| Nombre del paquete | `com.david.administradorarchivos` |
| SHA-1 | el de **este** APK (ver abajo) |

### SHA-1 del APK de GitHub

No uses el keystore de tu PC. Usa el del archivo que instalas:

```bash
keytool -printcert -jarfile Hosts-debug.apk
```

Copia la línea **SHA1:** (`AA:BB:CC:...`) y pégala en Google Cloud. Espera 5–10 minutos.

---

## Cómo publicar un update

1. Cambia el código (o pide que lo suban a `main`)
2. Espera a que **Actions** termine en verde
3. En **Releases** aparece una versión nueva con el APK
4. Desinstala la anterior en el teléfono solo si no te deja actualizar, e instala el APK nuevo

Para una versión concreta (opcional), crea una etiqueta `v0.2.0` en GitHub: también dispara el mismo workflow.

---

## Compilar en el PC (opcional)

Necesitas JDK 17 y Android SDK.

```bash
gradle assembleDebug
```

El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.

---

## Limitaciones (v0.1)

- Terminal básica: un comando → una respuesta (no es una shell interactiva tipo `vim` / `htop`)
- El APK es **debug** (para pruebas, no Play Store)
- Google Drive no funciona hasta crear el Client ID Android
- OpenSSH / PuTTY en el PC siguen siendo mejores para trabajo serio; esta app es el mando del teléfono

---

## Historial

Ver [CHANGELOG.md](CHANGELOG.md) y [Releases](https://github.com/pilahito/AdministradorArchivos/releases).
