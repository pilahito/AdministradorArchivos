# app-desktop — CloudTerm Pro (Windows / Tauri 2)

Cliente de escritorio **CloudTerm Pro** (marca propia, open source). UI oscura neón,
pestañas Bóveda | SFTP | Workspace, terminal real con **xterm.js** y sesiones SSH vía
PTY + cliente OpenSSH del sistema.

## Requisitos

- Node.js 18+
- Rust stable + Microsoft C++ Build Tools (Windows)
- WebView2 (Windows 10/11)
- OpenSSH Client (recomendado)

## Desarrollo

```bash
cd app-desktop
npm install
npm run tauri dev
```

## Build Windows (EXE / MSI)

```bash
cd app-desktop
npm install
npm run tauri build
```

Artefactos típicos:

- `src-tauri/target/release/cloudterm-pro.exe`
- `src-tauri/target/release/bundle/nsis/*.exe`
- `src-tauri/target/release/bundle/msi/*.msi`

### Cómo ejecutar el .exe

1. Tras el build, abre `src-tauri/target/release/`.
2. Ejecuta `cloudterm-pro.exe` (portable) o instala el `.msi` / NSIS.
3. Workspace → host → Conectar (OpenSSH en PATH).

## Temas

- **CloudTerm Dark Neon** (`#0A1628`, `#00E5FF`, `#39FF14`) — por defecto
- Dracula / Solarized — stubs en la barra lateral

## Idioma

Interfaz por defecto en **español**.

## Notas

- MOTD de estado opcional al conectar.
- Sin marcas ni assets de terceros comerciales.
