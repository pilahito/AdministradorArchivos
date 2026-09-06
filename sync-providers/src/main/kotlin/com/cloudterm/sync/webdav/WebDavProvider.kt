package com.cloudterm.sync.webdav

import com.cloudterm.sync.RemoteEntry
import com.cloudterm.sync.SyncProvider
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Base64

/**
 * Implementación WebDAV básica: PROPFIND / GET / PUT / DELETE.
 * Compatible con Nextcloud, ownCloud, Seafile WebDAV, etc.
 */
class WebDavProvider(
    private val baseUrl: String,
    private val username: String,
    private val password: String,
    override val id: String = "webdav",
    override val displayName: String = "WebDAV"
) : SyncProvider {

    private fun authHeader(): String {
        val raw = "$username:$password"
        val b64 = Base64.getEncoder().encodeToString(raw.toByteArray(StandardCharsets.UTF_8))
        return "Basic $b64"
    }

    private fun open(path: String, method: String): HttpURLConnection {
        val normalized = baseUrl.trimEnd('/') + "/" + path.trimStart('/')
        val conn = (URL(normalized).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            setRequestProperty("Authorization", authHeader())
            connectTimeout = 20_000
            readTimeout = 60_000
            instanceFollowRedirects = true
        }
        return conn
    }

    override suspend fun list(path: String): List<RemoteEntry> {
        val conn = open(path, "PROPFIND")
        conn.setRequestProperty("Depth", "1")
        conn.setRequestProperty("Content-Type", "application/xml; charset=utf-8")
        conn.doOutput = true
        val body = """<?xml version="1.0"?>
<d:propfind xmlns:d="DAV:">
  <d:prop><d:displayname/><d:getcontentlength/><d:resourcetype/></d:prop>
</d:propfind>"""
        OutputStreamWriter(conn.outputStream, StandardCharsets.UTF_8).use { it.write(body) }
        val code = conn.responseCode
        require(code in 200..299 || code == 207) { "PROPFIND HTTP $code" }
        val xml = BufferedReader(InputStreamReader(conn.inputStream, StandardCharsets.UTF_8)).readText()
        return parsePropfind(xml, path)
    }

    override suspend fun download(remotePath: String): ByteArray {
        val conn = open(remotePath, "GET")
        require(conn.responseCode in 200..299) { "GET HTTP ${conn.responseCode}" }
        return conn.inputStream.readBytes()
    }

    override suspend fun upload(remotePath: String, data: ByteArray) {
        val conn = open(remotePath, "PUT")
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/octet-stream")
        conn.outputStream.use { it.write(data) }
        require(conn.responseCode in 200..299) { "PUT HTTP ${conn.responseCode}" }
    }

    override suspend fun delete(remotePath: String) {
        val conn = open(remotePath, "DELETE")
        require(conn.responseCode in 200..299 || conn.responseCode == 204) {
            "DELETE HTTP ${conn.responseCode}"
        }
    }

    override suspend fun mkdir(remotePath: String) {
        val conn = open(remotePath, "MKCOL")
        require(conn.responseCode in 200..299 || conn.responseCode == 201) {
            "MKCOL HTTP ${conn.responseCode}"
        }
    }

    companion object {
        /** Parser mínimo de PROPFIND (href + collection). */
        fun parsePropfind(xml: String, basePath: String): List<RemoteEntry> {
            val entries = mutableListOf<RemoteEntry>()
            val hrefRegex = Regex("""<(?:D:|d:)?href>([^<]+)</(?:D:|d:)?href>""", RegexOption.IGNORE_CASE)
            val collectionRegex = Regex("""<(?:D:|d:)?collection\s*/?>""", RegexOption.IGNORE_CASE)
            val responses = xml.split(Regex("""<(?:D:|d:)?response(?:\s|>)""", RegexOption.IGNORE_CASE))
            for (chunk in responses.drop(1)) {
                val href = hrefRegex.find(chunk)?.groupValues?.get(1) ?: continue
                val name = href.trimEnd('/').substringAfterLast('/')
                if (name.isBlank()) continue
                val isDir = collectionRegex.containsMatchIn(chunk)
                val path = if (href.startsWith("http")) {
                    // absolutos: conservar path
                    try { URL(href).path } catch (_: Exception) { href }
                } else href
                entries += RemoteEntry(
                    name = java.net.URLDecoder.decode(name, "UTF-8"),
                    path = path,
                    isDirectory = isDir
                )
            }
            return entries.filter { it.path.trimEnd('/') != basePath.trimEnd('/') }
        }
    }
}