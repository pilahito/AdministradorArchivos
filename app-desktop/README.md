# app-desktop — CloudTerm Pro

Scaffold escritorio (Tauri / Compose Multiplatform). Ver `package.json` y `COMPOSE_MULTIPLATFORM.md`.

## Temas compartidos

Windows y Linux **comparten** los tokens de `:ui-shared` (`CloudTermThemes`):

- Dark Cyan Neon (mockup: navy `#0A1628`, cyan `#00E5FF`, neon `#39FF14`)
- Dracula, Solarized Light, Cyberpunk

La UI desktop debe importar paletas desde `ui-shared` (no duplicar hex en Tauri/CMP).

## Estado

Scaffold que **compila** el lado de configuración; aún no produce binario en CI.
Roadmap: sesiones SSH, terminal, EXE/DEB alineados con la UI Android.
