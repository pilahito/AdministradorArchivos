# Hosts — cliente SSH / SFTP / Drive

App Android (Kotlin + Jetpack Compose) con interfaz tipo Termius:

- **Hosts**: lista de servidores, botón +, conectar al tocar
- **Terminal**: enviar comandos SSH
- **SFTP**: archivos del host conectado
- **Ajustes**: Google Drive (OAuth / pantalla de permisos)

## Cómo bajar el APK

1. Entra a [Actions](https://github.com/pilahito/AdministradorArchivos/actions)
2. Abre el último **Compilar APK** con ✓ verde
3. En **Artifacts** descarga `administrador-archivos-apk`
4. Instálalo en el teléfono (origen desconocido)

Cada vez que se sube código a `main`, GitHub vuelve a compilar un APK nuevo.

## Google Drive

Hay que crear un OAuth Client ID tipo Android en Google Cloud:

- Paquete: `com.david.administradorarchivos`
- SHA-1 del keystore debug

Sin eso, el botón de Google fallará.
