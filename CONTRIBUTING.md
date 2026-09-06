# Contribuir a CloudTerm Pro

¡Gracias por tu interés!

## Cómo empezar

1. Fork + clone del repo
2. Rama desde `main`: `git checkout -b feature/mi-cambio`
3. Abre el proyecto en Android Studio (Giraffe+) o IntelliJ
4. Compila módulos JVM: `./gradlew :core-ssh:compileKotlin`

## Estilo

- Kotlin oficial, jvmTarget 17
- UI en español por defecto (strings)
- No incluir secretos, Client IDs privados ni keystores en el repo
- No pegar código GPL de Termux / Material Files — solo APIs / puentes

## Módulos

Prefiere cambios en `:core-ssh`, `:vault`, `:sync-providers`, etc. antes de hinchar `:app`.

## Pull requests

- Describe el *porqué*
- Enlaza issues si aplica
- CI APK debe pasar (o justificar por qué no)
- Un PR por tema

## Código de conducta

Sé respetuoso. Issues y PRs en español o inglés.