# Build — CloudTerm Pro

## Requisitos

- JDK 17+
- Android SDK 34 (solo para `:app`)
- Gradle 8.7+ (wrapper recomendado) o Android Studio

## Comandos

```bash
# Todo lo compilable en JVM (sin Android SDK)
./gradlew :core-ssh:compileKotlin
./gradlew :vault:compileKotlin :ui-shared:compileKotlin
./gradlew :sync-providers:compileKotlin :cloud-sync:compileKotlin :terminal:compileKotlin

# APK debug
./gradlew :app:assembleDebug

# APK release (necesita signing local — no está en el repo)
./gradlew :app:assembleRelease
```

## CI

| Workflow | Función |
|----------|---------|
| `.github/workflows/android.yml` | compileJvm + assembleDebug |
| `.github/workflows/compilar-apk.yml` | APK + Release (legado CyberTerm, actualizado) |
| `.github/workflows/release.yml` | Esqueleto de release etiquetado |
| `.github/workflows/desktop.yml` | Stub Tauri / desktop |
| `.github/workflows/deb.yml` | Stub paquete DEB |

## Escritorio

Ver `app-desktop/README.md` — scaffold Tauri / Compose Multiplatform (aún no produce binario en CI).

## Notas Windows

Si no hay `gradlew`, usa Android Studio o instala Gradle 8.7 y genera el wrapper:

```bash
gradle wrapper --gradle-version 8.7
```