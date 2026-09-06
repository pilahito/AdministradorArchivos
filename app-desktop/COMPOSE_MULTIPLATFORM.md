# Compose Multiplatform (notas)

La ruta activa de escritorio es **Tauri 2** en este directorio (`app-desktop/`).

Compose Multiplatform queda como alternativa futura compartiendo tokens de `:ui-shared`.

```kotlin
// commonMain (futuro)
commonMain.dependencies {
  implementation(project(":ui-shared"))
}
```
