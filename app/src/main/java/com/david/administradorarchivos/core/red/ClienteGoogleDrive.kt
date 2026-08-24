package com.david.administradorarchivos.core.red

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File as DriveFile
import java.util.Collections

/**
 * Cliente de Google Drive que usa el flujo oficial de Google Sign-In.
 *
 * Al iniciar sesión, Google muestra la pantalla de consentimiento
 * ("link de permisos") donde el usuario autoriza el acceso a su Drive.
 * Una vez concedido, se obtiene un token OAuth y se puede listar / subir /
 * descargar archivos.
 *
 * REQUISITOS EN GOOGLE CLOUD CONSOLE (obligatorio para que funcione):
 * 1. Crear un proyecto en https://console.cloud.google.com
 * 2. Habilitar "Google Drive API"
 * 3. Crear credenciales → OAuth 2.0 Client ID → tipo "Android"
 *    - Nombre del paquete: com.david.administradorarchivos
 *    - SHA-1 del keystore de depuración (ver README)
 * 4. (Opcional) Añadir también un client ID de tipo "Web" si se usa
 *    requestIdToken, pero con DEFAULT_SIGN_IN + scopes no es necesario.
 */
class ClienteGoogleDrive(private val contexto: Context) {

    private var servicioDrive: Drive? = null
    private var cuenta: GoogleSignInAccount? = null

    companion object {
        /** Scope completo de Drive (lectura/escritura de todos los archivos del usuario). */
        val SCOPES = listOf(DriveScopes.DRIVE)

        fun crearClienteSignIn(contexto: Context): GoogleSignInClient {
            val opciones = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope(DriveScopes.DRIVE))
                .build()
            return GoogleSignIn.getClient(contexto, opciones)
        }

        /** Devuelve la cuenta ya autenticada (si la hay) sin mostrar UI. */
        fun cuentaGuardada(contexto: Context): GoogleSignInAccount? {
            return GoogleSignIn.getLastSignedInAccount(contexto)
        }
    }

    fun estaConectado(): Boolean = cuenta != null && servicioDrive != null

    fun emailCuenta(): String? = cuenta?.email

    /**
     * Configura el servicio Drive a partir de una cuenta ya obtenida del
     * Intent de Sign-In (o de la cuenta guardada).
     */
    fun conectarConCuenta(cuenta: GoogleSignInAccount) {
        this.cuenta = cuenta
        val credencial = GoogleAccountCredential.usingOAuth2(
            contexto,
            Collections.singleton(DriveScopes.DRIVE)
        ).apply {
            selectedAccount = cuenta.account
        }
        servicioDrive = Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credencial
        )
            .setApplicationName("AdministradorArchivos")
            .build()
    }

    /**
     * Lista archivos de la raíz de "Mi unidad" (o de una carpeta dada).
     * Devuelve pares (id, nombre, esCarpeta).
     */
    fun listarArchivos(idCarpeta: String = "root"): List<ArchivoDrive> {
        val servicio = servicioDrive
            ?: throw SeguridadRedExcepcion("No hay sesión de Google Drive activa")

        val resultado = servicio.files().list()
            .setQ("'$idCarpeta' in parents and trashed = false")
            .setSpaces("drive")
            .setFields("files(id, name, mimeType, size, modifiedTime)")
            .setPageSize(100)
            .execute()

        return (resultado.files ?: emptyList()).map { f ->
            ArchivoDrive(
                id = f.id,
                nombre = f.name ?: "(sin nombre)",
                esCarpeta = f.mimeType == "application/vnd.google-apps.folder",
                mimeType = f.mimeType,
                tamanio = f.getSize()
            )
        }.sortedWith(compareByDescending<ArchivoDrive> { it.esCarpeta }.thenBy { it.nombre.lowercase() })
    }

    fun desconectar(clienteSignIn: GoogleSignInClient) {
        servicioDrive = null
        cuenta = null
        // Cierra la sesión de Google (borra la cuenta guardada)
        clienteSignIn.signOut()
    }
}

data class ArchivoDrive(
    val id: String,
    val nombre: String,
    val esCarpeta: Boolean,
    val mimeType: String? = null,
    val tamanio: Long? = null
)
