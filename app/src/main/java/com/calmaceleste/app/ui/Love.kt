package com.calmaceleste.app.ui

import android.Manifest
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.calmaceleste.app.data.LoveItem
import com.calmaceleste.app.data.MediaItem
import com.calmaceleste.app.data.Repo
import com.calmaceleste.app.system.SongController
import com.calmaceleste.app.system.displayName
import com.calmaceleste.app.system.keepReadAccess
import kotlinx.coroutines.delay
import java.io.File

private enum class LoveAction { RECORD_VOICE, PICK_VOICE, RECORD_VIDEO, PICK_VIDEO }

@Suppress("DEPRECATION")
private fun newRecorder(ctx: android.content.Context): MediaRecorder =
    if (Build.VERSION.SDK_INT >= 31) MediaRecorder(ctx) else MediaRecorder()

private fun fileOf(uri: String): File? = Uri.parse(uri).path?.let { File(it) }

// ---------------------------------------------------------------------------------------------
// Mensajes de amor: notas de voz y videos de familiares (o tuyos)
// ---------------------------------------------------------------------------------------------

@Composable
fun LoveScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val all by Repo.love.items.collectAsState()
    val list = all.filter { it.kind == "voice" || it.kind == "video" }.sortedByDescending { it.time }
    val controller = remember { SongController(ctx) }
    DisposableEffect(Unit) { onDispose { controller.stop() } }

    var action by remember { mutableStateOf<LoveAction?>(null) } // pide el nombre antes
    var from by remember { mutableStateOf("") }
    var recording by remember { mutableStateOf(false) }
    var watching by remember { mutableStateOf<LoveItem?>(null) }
    var toDelete by remember { mutableStateOf<LoveItem?>(null) }
    var captureFile by remember { mutableStateOf<File?>(null) }
    var pendingPick by remember { mutableStateOf<LoveAction?>(null) }

    fun add(kind: String, title: String, uri: String, inApp: Boolean) {
        Repo.love.update { it + LoveItem(Repo.newId(), kind, title, from, uri, inApp, System.currentTimeMillis()) }
    }

    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        val what = pendingPick
        if (uri != null && what != null) {
            ctx.keepReadAccess(uri)
            add(if (what == LoveAction.PICK_VIDEO) "video" else "voice", ctx.displayName(uri), uri.toString(), false)
        }
        pendingPick = null
    }
    val capture = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { ok: Boolean ->
        val f = captureFile
        if (ok && f != null && f.exists() && f.length() > 0) {
            add("video", from.ifBlank { "Mi video" }, Uri.fromFile(f).toString(), true)
        } else {
            f?.delete()
        }
        captureFile = null
    }
    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) recording = true
        else Toast.makeText(ctx, "Sin permiso del micrófono no se puede grabar", Toast.LENGTH_LONG).show()
    }

    fun run(a: LoveAction) {
        when (a) {
            LoveAction.RECORD_VOICE -> micPermission.launch(Manifest.permission.RECORD_AUDIO)
            LoveAction.PICK_VOICE -> { pendingPick = a; pickMedia.launch(arrayOf("audio/*")) }
            LoveAction.PICK_VIDEO -> { pendingPick = a; pickMedia.launch(arrayOf("video/*")) }
            LoveAction.RECORD_VIDEO -> {
                try {
                    val dir = File(ctx.filesDir, "videos").apply { mkdirs() }
                    val f = File(dir, "v${Repo.newId()}.mp4")
                    captureFile = f
                    capture.launch(FileProvider.getUriForFile(ctx, ctx.packageName + ".fileprovider", f))
                } catch (e: Exception) {
                    captureFile = null
                    Toast.makeText(ctx, "No se pudo abrir la cámara", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    ScreenScaffold("Mensajes de amor", onBack) {
        SoftCard {
            Text(
                "Guarda aquí la voz y los videos de las personas que amas (o los tuyos) para escucharlos cuando necesites sentirte acompañado.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(6.dp))
            SoftText("Todo se queda solo en tu teléfono.")
        }
        BigButton("🎙️ Grabar una nota de voz") { from = ""; action = LoveAction.RECORD_VOICE }
        BigButton("🎬 Grabar un video", soft = true) { from = ""; action = LoveAction.RECORD_VIDEO }
        OutlinedButton(onClick = { from = ""; action = LoveAction.PICK_VOICE }, modifier = Modifier.fillMaxWidth()) {
            Text("Añadir un audio que ya tengo (por ejemplo de WhatsApp)")
        }
        OutlinedButton(onClick = { from = ""; action = LoveAction.PICK_VIDEO }, modifier = Modifier.fillMaxWidth()) {
            Text("Añadir un video de mi galería")
        }

        watching?.let { item ->
            SoftCard {
                VideoBox(MediaItem(item.id, item.title, item.uri, true))
                TextButton(onClick = { watching = null }) { Text("Cerrar video") }
            }
        }

        if (list.isEmpty()) SoftText("Todavía no hay mensajes. Graba el primero o añade uno.")
        list.forEach { item ->
            SoftCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (item.isVideo) "🎬" else "🎙️", fontSize = 26.sp)
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text(
                            item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2
                        )
                        if (item.from.isNotBlank()) SoftText("De: ${item.from}")
                    }
                    if (item.isVideo) {
                        FilledTonalButton(onClick = { controller.stop(); watching = item }) { Text("Ver") }
                    } else {
                        val current = controller.currentId == item.id
                        FilledTonalButton(onClick = {
                            if (current) controller.toggle()
                            else controller.play(MediaItem(item.id, item.title, item.uri, false))
                        }) { Text(if (current && controller.playing) "Pausa" else "Escuchar") }
                    }
                    IconButton(onClick = { toDelete = item }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Quitar")
                    }
                }
            }
        }
    }

    action?.let { a ->
        AlertDialog(
            onDismissRequest = { action = null },
            title = { Text("¿De quién es este mensaje?") },
            text = {
                OutlinedTextField(
                    from, { from = it.take(40) },
                    label = { Text("Nombre (mamá, abuela, yo...)") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    action = null
                    run(a)
                }) { Text("Continuar") }
            },
            dismissButton = { TextButton(onClick = { action = null }) { Text("Cancelar") } }
        )
    }

    if (recording) {
        RecorderDialog(
            onSaved = { f ->
                recording = false
                add("voice", from.ifBlank { "Mi nota de voz" }, Uri.fromFile(f).toString(), true)
            },
            onCancel = { recording = false }
        )
    }

    toDelete?.let { item ->
        ConfirmDialog(
            title = "¿Quitar este mensaje?",
            text = if (item.inApp) "Se borrará de la app y del teléfono." else "Solo se quita de la app. El archivo sigue en tu teléfono.",
            confirmLabel = "Quitar",
            onConfirm = {
                if (controller.currentId == item.id) controller.stop()
                if (watching?.id == item.id) watching = null
                if (item.inApp) fileOf(item.uri)?.delete()
                Repo.love.update { l -> l.filter { it.id != item.id } }
                toDelete = null
            },
            onDismiss = { toDelete = null }
        )
    }
}

@Composable
private fun RecorderDialog(onSaved: (File) -> Unit, onCancel: () -> Unit) {
    val ctx = LocalContext.current
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var file by remember { mutableStateOf<File?>(null) }
    var seconds by remember { mutableIntStateOf(0) }
    val isRecording = recorder != null

    fun release(delete: Boolean) {
        try { recorder?.stop() } catch (_: Exception) { }
        try { recorder?.release() } catch (_: Exception) { }
        recorder = null
        if (delete) file?.delete()
    }

    DisposableEffect(Unit) { onDispose { if (recorder != null) release(true) } }
    LaunchedEffect(isRecording) {
        seconds = 0
        while (isRecording) {
            delay(1000)
            seconds++
        }
    }

    fun start() {
        try {
            val dir = File(ctx.filesDir, "voice").apply { mkdirs() }
            val f = File(dir, "n${Repo.newId()}.m4a")
            val r = newRecorder(ctx)
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setAudioEncodingBitRate(96000)
            r.setAudioSamplingRate(44100)
            r.setOutputFile(f.absolutePath)
            r.prepare()
            r.start()
            file = f
            recorder = r
        } catch (e: Exception) {
            Toast.makeText(ctx, "No se pudo empezar a grabar", Toast.LENGTH_LONG).show()
        }
    }

    AlertDialog(
        onDismissRequest = { release(true); onCancel() },
        title = { Text(if (isRecording) "Grabando…" else "Nota de voz") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(if (isRecording) "🔴" else "🎙️", fontSize = 44.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "%d:%02d".format(seconds / 60, seconds % 60),
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    if (isRecording) "Habla con calma. Toca «Parar y guardar» cuando termines." else "Toca «Empezar» y habla cerca del teléfono.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            if (isRecording) {
                TextButton(onClick = {
                    val f = file
                    release(false)
                    if (f != null && f.exists() && f.length() > 0) onSaved(f) else onCancel()
                }) { Text("Parar y guardar") }
            } else {
                TextButton(onClick = { start() }) { Text("Empezar") }
            }
        },
        dismissButton = { TextButton(onClick = { release(true); onCancel() }) { Text("Cancelar") } }
    )
}
