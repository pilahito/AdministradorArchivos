# Administrador de Archivos — estilo Termius + Google Drive

App Android nativa (Kotlin + Jetpack Compose, Material 3, tema oscuro
inspirado en Termius) con:

- Explorador de almacenamiento local
- Conexiones **SFTP / SSH**, **FTP**, **FTPS**
- **Google Drive** con flujo OAuth oficial (pantalla de consentimiento /
  “link de permisos” de Google)
- Editor de código con resaltado de sintaxis básico

## Cómo obtener el APK (GitHub Actions)

1. Crea un repositorio nuevo en GitHub y sube **todo** el contenido de esta
   carpeta (incluida `.github/`).
2. Al hacer `git push` a `main`, el workflow
   `.github/workflows/compilar-apk.yml` se dispara solo.
3. Ve a **Actions** → ejecución terminada → **Artifacts** →
   `administrador-archivos-apk` → descarga el `.apk`.
4. Instálalo en el teléfono (permitir “origen desconocido” la primera vez).

También puedes abrir el proyecto en **Android Studio** y
`Build > Build APK(s)`.

## Configurar Google Drive (obligatorio para que funcione el login)

Sin esto, el botón “Conectar con Google” fallará con error de configuración.

1. Entra en [Google Cloud Console](https://console.cloud.google.com)
2. Crea un proyecto (o usa uno existente)
3. **APIs y servicios → Biblioteca** → busca “Google Drive API” → **Habilitar**
4. **APIs y servicios → Credenciales → Crear credenciales → ID de cliente de OAuth**
   - Tipo de aplicación: **Android**
   - Nombre del paquete: `com.david.administradorarchivos`
   - SHA-1 del keystore de depuración:

     ```bash
     # Linux / macOS
     keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
     ```

     En Windows (PowerShell):

     ```powershell
     keytool -list -v -keystore $env:USERPROFILE\.android\debug.keystore -alias androiddebugkey -storepass android -keypass android
     ```

     Copia el valor de **SHA1** (formato `AA:BB:CC:...`) y pégalo en la consola.
5. (Primera vez) Configura la **pantalla de consentimiento OAuth**:
   - Tipo de usuario: Externo (o Interno si es Workspace)
   - Añade tu email de prueba en “Usuarios de prueba”
   - Scopes: añade `.../auth/drive` (o deja el que pida la app)

Después de crear el Client ID Android, espera 5–10 minutos y vuelve a
probar el botón **Conectar con Google** en la app. Se abrirá la pantalla
oficial de Google donde el usuario acepta los permisos (el “link de
permisos”).

## Qué hace cada pestaña

| Pestaña   | Función                                      |
|-----------|----------------------------------------------|
| **Hosts** | SFTP, FTP, FTPS y Google Drive               |
| **Archivos** | Explorador local + ZIP/JAR                   |
| **Editor**  | Editor de texto/código con resaltado simple  |

## Notas de seguridad

- En `ClienteSftp.kt`, `StrictHostKeyChecking` está en `"no"` para facilitar
  pruebas. Cámbialo a verificación real (`known_hosts`) antes de usar
  servidores de producción.
- Google Drive no almacena contraseñas: solo el token OAuth que otorga
  Google tras el consentimiento del usuario.

## Por qué no hay un .apk precompilado aquí

El entorno de generación no tiene el Android SDK ni acceso a
`dl.google.com`. El workflow de GitHub Actions sí puede compilar el APK
completo.
