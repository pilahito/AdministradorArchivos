# Compose Multiplatform (notas)

Dependencias futuras compartidas: `:core-ssh`, `:vault`, `:ui-shared`, `:cloud-sync`.

```kotlin
commonMain.dependencies {
  implementation(project(":core-ssh"))
  implementation(project(":vault"))
  implementation(project(":ui-shared"))
}
```

Empaquetado: packageDeb / packageReleaseExe (compose.desktop).
