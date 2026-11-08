package com.calmaceleste.app.system

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast

private fun Context.tryStart(intent: Intent) {
    try {
        if (this !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(this, "No se pudo abrir en este teléfono", Toast.LENGTH_SHORT).show()
    }
}

/** Abre el marcador con el número listo (no necesita permiso de llamadas). */
fun Context.dial(number: String) =
    tryStart(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null)))

fun Context.sms(number: String, body: String) =
    tryStart(
        Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", number, null))
            .putExtra("sms_body", body)
    )

fun Context.openUrl(url: String) = tryStart(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

fun Context.whatsapp(number: String, text: String) {
    val digits = number.filter { it.isDigit() }
    openUrl("https://wa.me/$digits?text=${Uri.encode(text)}")
}

/** Nombre legible de un archivo elegido por la persona. */
fun Context.displayName(uri: Uri): String {
    try {
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                val n = c.getString(0)
                if (!n.isNullOrBlank()) return n.substringBeforeLast('.')
            }
        }
    } catch (_: Exception) {
    }
    return "Sin título"
}

/** Guarda el permiso de lectura para que el archivo siga disponible al reabrir la app. */
fun Context.keepReadAccess(uri: Uri) {
    try {
        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    } catch (_: Exception) {
    }
}

/** Abre la ficha de la app en Google Play para valorarla. */
fun Context.rateApp() {
    val id = packageName.removeSuffix(".debug")
    try {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$id"))
        if (this !is Activity) i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(i)
    } catch (e: Exception) {
        openUrl("https://play.google.com/store/apps/details?id=$id")
    }
}
