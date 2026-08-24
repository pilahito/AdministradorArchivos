package com.david.administradorarchivos.core.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

/**
 * Resaltado de sintaxis por expresiones regulares — deliberadamente
 * simple: reconoce palabras clave comunes, comentarios de línea/bloque,
 * cadenas de texto y números. No es un parser real (no entiende contexto
 * anidado, por ejemplo una palabra clave dentro de un string se resalta
 * igual), pero es suficiente para lectura rápida en pantalla, que es el
 * caso de uso pedido.
 *
 * [Suposición]: la lista de palabras clave por lenguaje es una selección
 * razonable pero no exhaustiva; si necesitas cobertura completa de un
 * lenguaje concreto, dímelo y la amplío para ese caso.
 */
private val COLOR_PALABRA_CLAVE = Color(0xFF7C6CF0)
private val COLOR_CADENA = Color(0xFF3DDC97)
private val COLOR_COMENTARIO = Color(0xFF8A93A6)
private val COLOR_NUMERO = Color(0xFF4FD1E8)

private val PALABRAS_CLAVE_POR_EXTENSION = mapOf(
    "kt" to listOf("fun", "val", "var", "class", "object", "if", "else", "when", "for", "while", "return", "import", "package", "private", "public", "override"),
    "java" to listOf("public", "private", "class", "void", "static", "final", "if", "else", "for", "while", "return", "import", "package", "new"),
    "py" to listOf("def", "class", "if", "elif", "else", "for", "while", "return", "import", "from", "as", "with", "try", "except", "self"),
    "js" to listOf("function", "const", "let", "var", "if", "else", "for", "while", "return", "import", "export", "class", "async", "await"),
    "html" to listOf("html", "head", "body", "div", "span", "script", "style", "href", "src"),
    "css" to listOf("color", "background", "margin", "padding", "display", "flex", "grid")
)

fun resaltarCodigo(codigo: String, extensionArchivo: String): AnnotatedString {
    val palabrasClave = PALABRAS_CLAVE_POR_EXTENSION[extensionArchivo.lowercase()] ?: emptyList()

    val patronComentarioLinea = Regex("//.*")
    val patronComentarioAlmohadilla = Regex("#.*")
    val patronCadena = Regex("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'")
    val patronNumero = Regex("\\b\\d+(\\.\\d+)?\\b")
    val patronPalabraClave = if (palabrasClave.isNotEmpty()) {
        Regex("\\b(${palabrasClave.joinToString("|")})\\b")
    } else null

    return buildAnnotatedString {
        append(codigo)

        fun pintarCoincidencias(patron: Regex, color: Color) {
            patron.findAll(codigo).forEach { coincidencia ->
                addStyle(SpanStyle(color = color), coincidencia.range.first, coincidencia.range.last + 1)
            }
        }

        // Orden: primero cadenas y comentarios (para que "ganen" visualmente
        // sobre palabras clave que pudieran coincidir dentro de ellos en
        // este resaltador simplificado), luego números y palabras clave.
        pintarCoincidencias(patronCadena, COLOR_CADENA)
        if (extensionArchivo.lowercase() == "py") {
            pintarCoincidencias(patronComentarioAlmohadilla, COLOR_COMENTARIO)
        } else {
            pintarCoincidencias(patronComentarioLinea, COLOR_COMENTARIO)
        }
        pintarCoincidencias(patronNumero, COLOR_NUMERO)
        patronPalabraClave?.let { pintarCoincidencias(it, COLOR_PALABRA_CLAVE) }
    }
}
