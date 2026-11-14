package com.chitram.tv.updates

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.chitram.tv.BuildConfig
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class AvailableUpdate(val versionCode: Int, val downloadUrl: String, val sha256: String, val size: Long)
class NoPublishedRelease : Exception()

internal fun parseRelease(json: String, installedVersion: Int): AvailableUpdate? {
    val release = JSONObject(json)
    val version = release.optString("tag_name").removePrefix("v").toIntOrNull()
        ?: throw IllegalArgumentException("Release version is invalid")
    if (version <= installedVersion) return null
    val asset = release.getJSONArray("assets").let { assets ->
        (0 until assets.length()).map { assets.getJSONObject(it) }
            .firstOrNull { it.optString("name") == "chitram-tv.apk" }
    } ?: throw IllegalArgumentException("Release has no TV APK")
    val url = asset.getString("browser_download_url")
    val expected = "https://github.com/jjperin/Chitram/releases/download/v$version/chitram-tv.apk"
    if (url != expected) throw IllegalArgumentException("Release download address is invalid")
    val digest = asset.optString("digest")
    if (!digest.matches(Regex("sha256:[0-9a-fA-F]{64}")))
        throw IllegalArgumentException("Release has no valid SHA-256 digest")
    val size = asset.getLong("size")
    if (size !in 1..100_000_000) throw IllegalArgumentException("Release APK size is invalid")
    return AvailableUpdate(version, url, digest.removePrefix("sha256:").lowercase(), size)
}

class GitHubUpdates(private val context: Context) {
    suspend fun check(): AvailableUpdate? = withContext(Dispatchers.IO) {
        val connection = (URL("https://api.github.com/repos/jjperin/Chitram/releases/latest")
            .openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "Chitram-Android-TV")
        }
        try {
            if (connection.responseCode == 404) throw NoPublishedRelease()
            if (connection.responseCode != 200) throw IllegalStateException("GitHub returned ${connection.responseCode}")
            val json = connection.inputStream.bufferedReader().use { it.readText() }
            if (json.length > 1_000_000) throw IllegalStateException("Release response is too large")
            parseRelease(json, BuildConfig.VERSION_CODE)
        } finally { connection.disconnect() }
    }

    suspend fun download(update: AvailableUpdate): File = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "updates").apply { mkdirs() }
        val target = File(dir, "chitram-v${update.versionCode}.apk")
        val part = File(dir, "download.part")
        if (target.isFile && verify(target, update)) return@withContext target
        target.delete()
        part.delete()
        val connection = (URL(update.downloadUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 30_000
            setRequestProperty("User-Agent", "Chitram-Android-TV")
        }
        try {
            if (connection.responseCode != 200) throw IllegalStateException("Download failed (${connection.responseCode})")
            connection.inputStream.use { input -> part.outputStream().use { output ->
                val buffer = ByteArray(8192)
                var received = 0L
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    received += count
                    if (received > update.size) throw IllegalStateException("APK is larger than advertised")
                    output.write(buffer, 0, count)
                }
            } }
            if (!verify(part, update)) throw IllegalStateException("Downloaded APK failed verification")
            val archive = context.packageManager.getPackageArchiveInfo(part.absolutePath, 0)
            if (archive?.packageName != context.packageName || archive?.versionCode != update.versionCode)
                throw IllegalStateException("APK package or version does not match")
            if (!part.renameTo(target)) throw IllegalStateException("Could not save update")
            target
        } finally { connection.disconnect(); part.delete() }
    }

    private fun verify(file: File, update: AvailableUpdate): Boolean {
        if (file.length() != update.size) return false
        val hash = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                hash.update(buffer, 0, read)
            }
        }
        return hash.digest().joinToString("") { "%02x".format(it) } == update.sha256
    }

    fun install(file: File) {
        if (Build.VERSION.SDK_INT >= 26 && !context.packageManager.canRequestPackageInstalls()) {
            context.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")))
            return
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.updates", file)
        context.startActivity(Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}
