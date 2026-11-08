package com.calmaceleste.app.data

import android.content.Context
import android.net.Uri
import com.calmaceleste.app.system.Scheduler
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Copia de seguridad en un solo archivo .zip que la persona guarda donde quiera.
 * Incluye todos tus datos y las notas de voz / videos grabados dentro de la app.
 * No incluye el PIN. Los archivos de tu galería (canciones, videos) no se copian: solo su referencia.
 */
object Backup {
    private val jsonFiles = listOf(
        "contacts.json", "mood.json", "thoughts.json", "reminders.json",
        "media.json", "good.json", "sessions.json", "love.json"
    )
    private val dirs = listOf("voice", "videos")
    private const val PREFS = "prefs.json"

    fun suggestedName(): String {
        val d = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        return "AnsiedadControl-copia-$d.zip"
    }

    /** Devuelve true si se guardó bien. */
    fun export(ctx: Context, dest: Uri): Boolean = try {
        val root = ctx.filesDir
        ctx.contentResolver.openOutputStream(dest)!!.use { raw ->
            ZipOutputStream(raw.buffered()).use { zip ->
                for (name in jsonFiles) {
                    val f = File(root, name)
                    if (f.exists()) {
                        zip.putNextEntry(ZipEntry(name))
                        f.inputStream().use { it.copyTo(zip) }
                        zip.closeEntry()
                    }
                }
                zip.putNextEntry(ZipEntry(PREFS))
                zip.write(Repo.prefs.exportJson().toByteArray(Charsets.UTF_8))
                zip.closeEntry()
                for (d in dirs) {
                    File(root, d).listFiles()?.filter { it.isFile }?.forEach { f ->
                        zip.putNextEntry(ZipEntry("$d/${f.name}"))
                        f.inputStream().use { it.copyTo(zip) }
                        zip.closeEntry()
                    }
                }
            }
        }
        true
    } catch (e: Exception) {
        false
    }

    /** Sustituye los datos actuales por los de la copia. Devuelve true si todo salió bien. */
    fun import(ctx: Context, src: Uri): Boolean {
        val root = ctx.filesDir
        val staging = File(ctx.cacheDir, "import_tmp")
        return try {
            staging.deleteRecursively()
            staging.mkdirs()
            var found = 0
            ctx.contentResolver.openInputStream(src)!!.use { raw ->
                ZipInputStream(raw.buffered()).use { zip ->
                    var e = zip.nextEntry
                    while (e != null) {
                        val name = e.name
                        val allowed = !e.isDirectory && (
                            name in jsonFiles || name == PREFS ||
                                dirs.any { d -> name.startsWith("$d/") && name.count { it == '/' } == 1 && !name.contains("..") }
                            )
                        if (allowed) {
                            val out = File(staging, name)
                            if (out.canonicalPath.startsWith(staging.canonicalPath + File.separator)) {
                                out.parentFile?.mkdirs()
                                out.outputStream().use { zip.copyTo(it) }
                                found++
                            }
                        }
                        zip.closeEntry()
                        e = zip.nextEntry
                    }
                }
            }
            if (found == 0) return false

            Repo.reminders.items.value.forEach { Scheduler.cancel(ctx, it) }
            for (name in jsonFiles) {
                val s = File(staging, name)
                if (s.exists()) s.copyTo(File(root, name), overwrite = true)
            }
            for (d in dirs) {
                val s = File(staging, d)
                if (s.exists()) {
                    val target = File(root, d).apply { mkdirs() }
                    s.listFiles()?.forEach { it.copyTo(File(target, it.name), overwrite = true) }
                }
            }
            File(staging, PREFS).takeIf { it.exists() }?.let { Repo.prefs.importJson(it.readText()) }

            Repo.contacts.reload(); Repo.mood.reload(); Repo.thoughts.reload(); Repo.reminders.reload()
            Repo.media.reload(); Repo.good.reload(); Repo.sessions.reload(); Repo.love.reload()
            Scheduler.scheduleAll(ctx)
            true
        } catch (e: Exception) {
            false
        } finally {
            staging.deleteRecursively()
        }
    }
}
