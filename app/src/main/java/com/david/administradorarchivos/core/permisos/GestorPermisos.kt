package com.david.administradorarchivos.core.permisos

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings

/**
 * Centraliza la comprobación y solicitud de permisos de almacenamiento.
 *
 * En Android 11 (API 30) en adelante, el permiso MANAGE_EXTERNAL_STORAGE no
 * se concede con un diálogo estándar: hay que llevar al usuario a una
 * pantalla de ajustes del sistema. Por eso este gestor expone un Intent
 * listo para lanzar en vez de pedir el permiso "en línea".
 */
object GestorPermisos {

    fun tieneAccesoCompleto(contexto: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true // en versiones anteriores basta con los permisos normales del manifiesto
        }
    }

    fun intentSolicitarAccesoCompleto(contexto: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                Uri.parse("package:" + contexto.packageName)
            )
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        }
    }
}
