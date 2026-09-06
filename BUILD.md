# Build CloudTerm Pro

Guia completa: [docs/BUILD.md](docs/BUILD.md)

```bash
./gradlew :core-ssh:compileKotlin
./gradlew :app:assembleDebug
```

Modulos: `app`, `core-ssh`, `vault`, `sync-providers`, `cloud-sync`, `terminal`, `ui-shared`.

Desktop (stub): `app-desktop/` (Tauri / Compose Multiplatform).
