# app-desktop — CloudTerm Pro (Windows + Linux / Tauri 2)

Cliente de escritorio **CloudTerm Pro** (marca propia, open source).
UI Dark Neon, pestanas Boveda | SFTP | Workspace, xterm.js + OpenSSH/PTY.

Plataformas: Windows (NSIS/MSI), Ubuntu (deb+AppImage), Arch/Fedora (AppImage).

## Requisitos
- Node 18+, Rust, OpenSSH
- Windows: Build Tools + WebView2
- Linux: scripts/install-linux-deps.sh


## Mas info
Bundles Win+Linux documentados en CI y packaging/aur.

## Instalar
- Ubuntu/Debian: archivo .deb o AppImage del release/CI
- Arch: AppImage; stub AUR en packaging/aur (cloudterm-pro-bin)
- Fedora: AppImage

## CI
Matriz GitHub Actions: windows-latest y ubuntu-22.04

UI en espanol. Tema CloudTerm Dark Neon (#0A1628 / #00E5FF / #39FF14).
