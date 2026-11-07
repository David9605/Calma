package com.calmaceleste.app.ui

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
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
import androidx.compose.ui.viewinterop.AndroidView
import com.calmaceleste.app.data.MediaItem
import com.calmaceleste.app.data.Repo
import com.calmaceleste.app.system.SongController
import com.calmaceleste.app.system.displayName
import com.calmaceleste.app.system.keepReadAccess
import com.calmaceleste.app.system.openUrl

// ---------------------------------------------------------------------------------------------
// Mis canciones (archivos del propio teléfono: funcionan sin Internet)
// ---------------------------------------------------------------------------------------------

@Composable
fun SongsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val songs by Repo.media.items.collectAsState()
    val list = songs.filter { !it.isVideo }
    val controller = remember { SongController(ctx) }
    DisposableEffect(Unit) { onDispose { controller.stop() } }
    var toDelete by remember { mutableStateOf<MediaItem?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            ctx.keepReadAccess(uri)
            val item = MediaItem(Repo.newId(), ctx.displayName(uri), uri.toString(), false)
            Repo.media.update { it + item }
        }
    }

    ScreenScaffold("Mi canción favorita", onBack) {
        SoftCard {
            Text(
                "Elige canciones que ya tengas en tu teléfono. Sonarán sin Internet cuando las necesites.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        BigButton("Añadir una canción") { picker.launch(arrayOf("audio/*")) }

        if (list.isEmpty()) {
            SoftText("Todavía no has añadido ninguna. Toca el botón de arriba.")
        }
        list.forEach { item ->
            SoftCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎵", fontSize = 26.sp)
                    Text(
                        item.title,
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2
                    )
                    val isCurrent = controller.currentId == item.id
                    FilledTonalButton(onClick = {
                        if (isCurrent) controller.toggle() else controller.play(item)
                    }) { Text(if (isCurrent && controller.playing) "Pausa" else "Play") }
                    IconButton(onClick = { toDelete = item }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Quitar")
                    }
                }
            }
        }
    }

    toDelete?.let { item ->
        ConfirmDialog(
            title = "¿Quitar esta canción?",
            text = "Solo se quita de la app. El archivo sigue en tu teléfono.",
            confirmLabel = "Quitar",
            onConfirm = {
                if (controller.currentId == item.id) controller.stop()
                Repo.media.update { l -> l.filter { it.id != item.id } }
                toDelete = null
            },
            onDismiss = { toDelete = null }
        )
    }
}

/** Reproductor para usar dentro de los 10 pasos: elige una canción al azar de las guardadas. */
@Composable
fun SongPlayerCard() {
    val ctx = LocalContext.current
    val songs by Repo.media.items.collectAsState()
    val list = songs.filter { !it.isVideo }
    var current by remember { mutableStateOf(list.randomOrNull()) }
    val controller = remember { SongController(ctx) }
    DisposableEffect(Unit) { onDispose { controller.stop() } }

    val item = current
    if (item == null) {
        SoftText("Añade una canción en «Mi canción favorita» para que suene aquí.")
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        BigButton(
            if (controller.currentId == item.id && controller.playing) "Pausa" else "Reproducir"
        ) {
            if (controller.currentId == item.id) controller.toggle() else controller.play(item)
        }
        if (list.size > 1) {
            OutlinedButton(
                onClick = {
                    controller.stop()
                    current = list.filter { it.id != item.id }.randomOrNull() ?: item
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Otra canción") }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Mis videos divertidos (archivos locales; los enlaces necesitan Internet y se abren fuera de la app)
// ---------------------------------------------------------------------------------------------

@Composable
fun VideosScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val all by Repo.media.items.collectAsState()
    val list = all.filter { it.isVideo }
    var playing by remember { mutableStateOf<MediaItem?>(null) }
    var showLink by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<MediaItem?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            ctx.keepReadAccess(uri)
            val item = MediaItem(Repo.newId(), ctx.displayName(uri), uri.toString(), true)
            Repo.media.update { it + item }
        }
    }

    ScreenScaffold("Mis videos divertidos", onBack) {
        SoftCard {
            Text(
                "Guarda videos de tu galería que te hagan reír. Se ven sin Internet.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        BigButton("Añadir un video de mi teléfono") { picker.launch(arrayOf("video/*")) }
        BigButton("Añadir un enlace (necesita Internet)", soft = true) { showLink = true }

        playing?.let { item ->
            SoftCard { VideoBox(item) }
        }

        if (list.isEmpty()) SoftText("Todavía no has añadido ninguno.")
        list.forEach { item ->
            SoftCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (item.isLink) "🔗" else "🎬", fontSize = 26.sp)
                    Text(
                        item.title,
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2
                    )
                    FilledTonalButton(onClick = {
                        if (item.isLink) ctx.openUrl(item.uri) else playing = item
                    }) { Text(if (item.isLink) "Abrir" else "Ver") }
                    IconButton(onClick = { toDelete = item }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Quitar")
                    }
                }
            }
        }
    }

    if (showLink) {
        LinkDialog(
            onSave = { title, url ->
                Repo.media.update { it + MediaItem(Repo.newId(), title, url, true) }
                showLink = false
            },
            onDismiss = { showLink = false }
        )
    }
    toDelete?.let { item ->
        ConfirmDialog(
            title = "¿Quitar este video?",
            text = "Solo se quita de la app.",
            confirmLabel = "Quitar",
            onConfirm = {
                if (playing?.id == item.id) playing = null
                Repo.media.update { l -> l.filter { it.id != item.id } }
                toDelete = null
            },
            onDismiss = { toDelete = null }
        )
    }
}

@Composable
private fun LinkDialog(onSave: (String, String) -> Unit, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("https://") }
    val valid = url.startsWith("http://") || url.startsWith("https://")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir enlace") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("Nombre") }, singleLine = true)
                OutlinedTextField(url, { url = it }, label = { Text("Enlace") }, singleLine = true)
                Text(
                    "Los enlaces se abren en tu navegador y necesitan Internet.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid && url.length > 9,
                onClick = { onSave(title.ifBlank { "Mi video" }, url.trim()) }
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun VideoBox(item: MediaItem) {
    key(item.id) {
        AndroidView(
            modifier = Modifier.fillMaxWidth().height(230.dp),
            factory = { c ->
                VideoView(c).apply {
                    val mc = MediaController(c)
                    mc.setAnchorView(this)
                    setMediaController(mc)
                    setVideoURI(Uri.parse(item.uri))
                    setOnPreparedListener { seekTo(1) }
                }
            },
            onRelease = { it.stopPlayback() }
        )
    }
}

/** Para los 10 pasos: un video al azar de los guardados. */
@Composable
fun VideoPlayerCard() {
    val ctx = LocalContext.current
    val all by Repo.media.items.collectAsState()
    val list = all.filter { it.isVideo }
    var current by remember { mutableStateOf(list.randomOrNull()) }
    val item = current
    if (item == null) {
        SoftText("Añade un video en «Mis videos divertidos» para verlo aquí.")
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        if (item.isLink) {
            BigButton("Abrir enlace") { ctx.openUrl(item.uri) }
        } else {
            VideoBox(item)
        }
        if (list.size > 1) {
            OutlinedButton(
                onClick = { current = list.filter { it.id != item.id }.randomOrNull() ?: item },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Otro video") }
        }
    }
}
