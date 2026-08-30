# Qué implementa este prompt (y qué no)

El master prompt pide una suite tipo Termius + Material Files + Termux.
Esta app es **nuestra**, open source, sin pago. No pegamos source GPL.

## Hecho en v0.3

- Paleta Slate `#0F172A`, cian `#00F0FF`, esmeralda `#00E676`
- Drawer lateral + barra inferior
- Room: grupos, hosts, claves, túneles, snippets
- SSHJ (`ClienteSshj`) + JSch (UI actual)
- Túnel local `-L` con SSHJ `LocalPortForwarder`
- Snippets ejecutables sobre la sesión activa
- Generador RSA 2048/4096 (`GeneradorClaves`)
- Catálogo `gradle/libs.versions.toml`
- Servicio foreground de sesión

## Deliberadamente NO en este APK

| Pedido | Motivo |
|---|---|
| Módulos Gradle `:core:terminal` etc. | Rompería el CI actual; la capa está en paquetes Java |
| `termux-emulator` / `termux-view` | GPL + proyecto aparte |
| Material Files FileSystemProvider | GPL + otra app |
| SQLCipher nativo | Se añade cuando el APK base esté estable |
| jcifs-ng / AWS S3 | Inflan el APK; siguiente bloque |
| ServerBox | Flutter, no Kotlin |

## Paquetes (≡ módulos lógicos)

```
datos/                 Room (bóveda)
core/red/              JSch + SSHJ + túneles + Drive
core/seguridad/        Generador de claves
ui/pantallas/          Compose
ui/navegacion/         Drawer + tabs
```
