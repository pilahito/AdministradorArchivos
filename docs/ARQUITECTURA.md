# Arquitectura — CloudTerm Pro

## Visión

Pasar de un monólito `:app` (CyberTerm) a una suite modular **CloudTerm Pro**
reutilizable en Android y escritorio, sin servidores propios.

## Capas

```
:app / app-desktop
        │
        ├── :terminal  →  :core-ssh (SSHJ)
        ├── :vault     →  AES-256 vault.db
        ├── :ui-shared →  temas
        └── :cloud-sync → :sync-providers (WebDAV + stubs)
```

## Módulos

| Módulo | Tipo | Dependencias |
|--------|------|--------------|
| `core-ssh` | Kotlin JVM | SSHJ, coroutines |
| `vault` | Kotlin JVM | JCE (AES/GCM, PBKDF2) |
| `sync-providers` | Kotlin JVM | HttpURLConnection |
| `cloud-sync` | Kotlin JVM | sync-providers |
| `terminal` | Kotlin JVM | core-ssh |
| `ui-shared` | Kotlin JVM | — |
| `app` | Android application | todos los anteriores |

## SSH

`SshClient` en `:core-ssh` unifica: `connect`, `startShell`, `exec`, `openSftp`, `createTunnel`.
La UI actual puede seguir usando `ClienteSshj` / JSch hasta migrar por completo.

## Terminal / NewTermux

`:terminal` define contratos (`TerminalSession`, `SshTerminalBridge`, `NewTermuxBridgeStub`).
El emulador VT se integra como dependencia externa (sin copiar GPL al repo).

## Sync

`SyncOrchestrator` usa ruta por defecto `/Apps/CloudTermPro/` y reintentos con backoff.
WebDAV está operativo; el resto son stubs de interfaz.

## Privacidad

Sin telemetría a infraestructura CloudTerm. Todo el cifrado y sync es cliente → proveedor del usuario.

## Histórico CyberTerm

La app v0.3 vivía en paquetes `com.david.administradorarchivos.*`.
Ese `applicationId` se mantiene para no romper instalaciones; el nombre visible es **CloudTerm Pro**.