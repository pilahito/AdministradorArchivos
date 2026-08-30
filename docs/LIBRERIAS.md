# Cómo usamos el código de GitHub

No pegamos el árbol completo de otros repos dentro de esta app.
En Android lo correcto es **dependencia Maven/Gradle**: Gradle baja el JAR compilado.

## Lo que SÍ está en el APK (librería)

| Proyecto | Cómo entra | Qué aporta |
|---|---|---|
| [mwiede/jsch](https://github.com/mwiede/jsch) | `com.github.mwiede:jsch:0.2.17` | SSH/SFTP que ya usa Terminal y SFTP |
| [hierynomus/sshj](https://github.com/hierynomus/sshj) | `com.hierynomus:sshj:0.38.0` | Motor SSHv2 extra (`ClienteSshj`) |
| [Apache Commons Net](https://commons.apache.org/proper/commons-net/) | `commons-net:3.11.1` | FTP |
| Google Drive API | Maven Google | OAuth + Drive |
| AndroidX Room | Maven Google | Bóveda local (hosts, claves, túneles, snippets) |

Eso **sí** es código experto de esos proyectos, empaquetado como librería.

## Lo que NO se copia al repo

| Repo | Por qué no se pega el source |
|---|---|
| [termux/termux-app](https://github.com/termux/termux-app) | GPL. `terminal-view` / `terminal-emulator` son otro proyecto enorme. Pegarlo obliga a licenciar **toda** la app como GPL y rompe el build actual. |
| [connectbot/connectbot](https://github.com/connectbot/connectbot) | GPL. Misma razón. |
| [zhanghai/MaterialFiles](https://github.com/zhanghai/MaterialFiles) | GPL. Explorador completo, no un módulo Maven. |
| [GlassHaven/Haven](https://github.com/GlassHaven/Haven) | Suite aparte; no hay artefacto Maven para pegar. |
| [lollipopkit/flutter_server_box](https://github.com/lollipopkit/flutter_server_box) | Es **Flutter**, no Kotlin. No se puede mezclar en este APK. |

De esos cogemos **ideas** (barra Ctrl/Esc/Tab, túneles, llavero, grupos) y las implementamos en código propio.

## Capa propia ya creada

- `datos/` Room: grupos, hosts, claves, túneles, snippets
- `core/red/ClienteSshj.kt` motor SSHJ
- `core/red/ClienteSftp.kt` motor JSch (el que usa la UI hoy)
- `core/red/ServicioSesion.kt` sesión en segundo plano

## Siguiente paso técnico (si lo pides)

1. Pasar la Terminal de “un comando” a shell interactiva con SSHJ.
2. Activar túneles LOCAL / REMOTO / SOCKS5 sobre el host conectado.
3. Generar claves ED25519 en el teléfono.
