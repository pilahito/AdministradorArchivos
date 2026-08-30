package com.david.administradorarchivos.core.seguridad

import android.util.Base64
import java.io.File
import java.security.KeyPairGenerator
import java.security.SecureRandom

data class ParClave(
    val tipo: String,
    val archivoPrivado: File,
    val archivoPublico: File,
    val publicaPem: String
)

object GeneradorClaves {
    fun rsa(directorio: File, nombre: String, bits: Int = 2048): ParClave {
        directorio.mkdirs()
        val gen = KeyPairGenerator.getInstance("RSA")
        gen.initialize(bits, SecureRandom())
        val par = gen.generateKeyPair()
        val priv = pem("PRIVATE KEY", par.private.encoded)
        val pub = pem("PUBLIC KEY", par.public.encoded)
        val fPriv = File(directorio, "$nombre")
        val fPub = File(directorio, "$nombre.pub")
        fPriv.writeText(priv)
        fPub.writeText(pub)
        return ParClave("RSA-$bits", fPriv, fPub, pub)
    }

    private fun pem(tipo: String, der: ByteArray): String {
        val b64 = Base64.encodeToString(der, Base64.NO_WRAP)
        val lineas = b64.chunked(64).joinToString("\n")
        return "-----BEGIN $tipo-----\n$lineas\n-----END $tipo-----\n"
    }
}
