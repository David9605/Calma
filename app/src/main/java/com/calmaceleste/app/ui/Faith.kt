package com.calmaceleste.app.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calmaceleste.app.data.LoveItem
import com.calmaceleste.app.data.MediaItem
import com.calmaceleste.app.data.Promise
import com.calmaceleste.app.data.Promises
import com.calmaceleste.app.data.Repo
import com.calmaceleste.app.system.SongController
import com.calmaceleste.app.system.displayName
import com.calmaceleste.app.system.keepReadAccess
import com.calmaceleste.app.system.openUrl

// ---------------------------------------------------------------------------------------------
// Promesas de Dios
// ---------------------------------------------------------------------------------------------

@Composable
fun PromisesScreen(onBack: () -> Unit) {
    var filter by remember { mutableIntStateOf(0) } // 0 = todas
    var featured by remember { mutableStateOf(Promises.today()) }
    val list = if (filter == 0) Promises.all else Promises.all.filter { it.category == filter - 1 }

    ScreenScaffold("Promesas de Dios", onBack) {
        SoftCard {
            Text("🕊️ Una palabra para ti ahora", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            PromiseText(featured, big = true)
            Spacer(Modifier.height(12.dp))
            BigButton("Otra promesa", soft = true) {
                featured = Promises.all.filter { it != featured }.random()
            }
        }
        SoftText("Dios está contigo, te cuida y no te deja solo. Lee despacio y respira entre cada frase.")
        ChipRow(listOf("Todas") + Promises.categories, filter) { filter = it }
        list.forEach { p ->
            SoftCard(onClick = { featured = p }) {
                Text(
                    Promises.categories[p.category],
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                PromiseText(p, big = false)
            }
        }
        SoftText("Texto adaptado de la Biblia Reina-Valera.")
    }
}

@Composable
private fun PromiseText(p: Promise, big: Boolean) {
    Text(
        "«${p.text}»",
        style = if (big) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge
    )
    Spacer(Modifier.height(4.dp))
    Text(
        p.ref,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

// ---------------------------------------------------------------------------------------------
// Biblia hablada: se escucha con los ojos cerrados
// ---------------------------------------------------------------------------------------------

private const val BIBLE_URL = "https://youtu.be/4eq1KKHUZiw?t=915" // minuto 15:15

private sealed interface BibleStart {
    data object Youtube : BibleStart
    data class Local(val item: LoveItem) : BibleStart
}

@Composable
fun BibleAudioScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val all by Repo.love.items.collectAsState()
    val mine = all.filter { it.kind == "bible" }
    val controller = remember { SongController(ctx) }
    DisposableEffect(Unit) { onDispose { controller.stop() } }
    var pending by remember { mutableStateOf<BibleStart?>(null) }
    var overlayItem by remember { mutableStateOf<LoveItem?>(null) }
    var ended by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<LoveItem?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            ctx.keepReadAccess(uri)
            Repo.love.update {
                it + LoveItem(Repo.newId(), "bible", ctx.displayName(uri), "", uri.toString(), false, System.currentTimeMillis())
            }
        }
    }

    val shown = overlayItem
    if (shown != null) {
        BackHandler { controller.stop(); overlayItem = null }
        EyesClosedOverlay(
            title = shown.title,
            playing = controller.playing,
            ended = ended,
            onToggle = { controller.toggle() },
            onExit = { controller.stop(); overlayItem = null }
        )
        return
    }

    ScreenScaffold("Biblia hablada", onBack) {
        SoftCard {
            Text("🌙 Para descansar en Dios", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(
                "Busca un lugar tranquilo, acuéstate cómodo, cierra los ojos y escucha. Respira lento. Quédate el tiempo que quieras: no hay prisa.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        BigButton("Escuchar la Biblia hablada") { pending = BibleStart.Youtube }
        SoftText("Se abre en YouTube desde el minuto 15:15 y necesita Internet. Baja el brillo de la pantalla y deja el teléfono a tu lado.")

        SectionTitle("Mi propio audio (sin Internet)")
        SoftText("Si tienes un audio de la Biblia guardado en tu teléfono, añádelo aquí y sonará dentro de la app, sin Internet.")
        BigButton("Añadir un audio de mi teléfono", soft = true) { picker.launch(arrayOf("audio/*")) }
        mine.forEach { item ->
            SoftCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📖", fontSize = 26.sp)
                    Text(
                        item.title,
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2
                    )
                    FilledTonalButton(onClick = { pending = BibleStart.Local(item) }) { Text("Escuchar") }
                    IconButton(onClick = { toDelete = item }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Quitar")
                    }
                }
            }
        }
    }

    pending?.let { start ->
        AlertDialog(
            onDismissRequest = { pending = null },
            title = { Text("Antes de empezar 🙏") },
            text = {
                Text(
                    "Acuéstate, cierra los ojos y respira lento. Escucha hasta que quieras. Dios está contigo.",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    pending = null
                    when (start) {
                        BibleStart.Youtube -> ctx.openUrl(BIBLE_URL)
                        is BibleStart.Local -> {
                            ended = false
                            overlayItem = start.item
                            controller.play(MediaItem(start.item.id, start.item.title, start.item.uri, false)) { ended = true }
                        }
                    }
                }) { Text("Estoy listo") }
            },
            dismissButton = { TextButton(onClick = { pending = null }) { Text("Ahora no") } }
        )
    }
    toDelete?.let { item ->
        ConfirmDialog(
            title = "¿Quitar este audio?",
            text = "Solo se quita de la app. El archivo sigue en tu teléfono.",
            confirmLabel = "Quitar",
            onConfirm = {
                Repo.love.update { l -> l.filter { it.id != item.id } }
                toDelete = null
            },
            onDismiss = { toDelete = null }
        )
    }
}

/** Pantalla oscura y tranquila para escuchar con los ojos cerrados. */
@Composable
private fun EyesClosedOverlay(
    title: String,
    playing: Boolean,
    ended: Boolean,
    onToggle: () -> Unit,
    onExit: () -> Unit
) {
    val t = rememberInfiniteTransition(label = "eyes")
    val glow by t.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(4500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF06141F))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("🙏", fontSize = 56.sp, modifier = Modifier.graphicsLayer { alpha = glow })
            Text(
                if (ended) "Puedes abrir los ojos cuando quieras.\nDios te cuida." else "Cierra los ojos y escucha.\nRespira despacio.",
                color = Color(0xFFBFE3F7).copy(alpha = if (ended) 1f else glow),
                fontSize = 22.sp,
                textAlign = TextAlign.Center
            )
            Text(title, color = Color(0xFF7FA9C2), fontSize = 14.sp, textAlign = TextAlign.Center, maxLines = 2)
            Spacer(Modifier.height(12.dp))
            if (!ended) {
                OutlinedButton(onClick = onToggle) { Text(if (playing) "Pausa" else "Continuar") }
            }
            TextButton(onClick = onExit) { Text("Terminar", color = Color(0xFF9CCBE6)) }
        }
    }
}

/** Tarjeta corta con una promesa de la Biblia (se muestra en cada paso de «Me siento ansioso»). */
@Composable
fun PromiseBanner(p: Promise) {
    SoftCard(tint = Color(0xFFDDF1FC).copy(alpha = 0.92f)) {
        Text("📖 ${p.ref}", style = MaterialTheme.typography.labelLarge, color = Color(0xFF0B3954), fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text("«${p.text}»", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF0B3954))
    }
}

/** Contenido del paso 7 (primera ronda): Biblia hablada con los ojos cerrados. */
@Composable
fun BibleStepContent() {
    val ctx = LocalContext.current
    val all by Repo.love.items.collectAsState()
    val item = all.firstOrNull { it.kind == "bible" }
    val controller = remember { SongController(ctx) }
    DisposableEffect(Unit) { onDispose { controller.stop() } }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "Acuéstate o ponte cómodo, cierra los ojos y escucha todo el tiempo que quieras. Respira lento. No hay prisa.",
            style = MaterialTheme.typography.bodyLarge
        )
        if (item != null) {
            SoftText(item.title)
            BigButton(if (controller.currentId == item.id && controller.playing) "Pausa" else "▶ Escuchar mi audio") {
                if (controller.currentId == item.id) controller.toggle()
                else controller.play(MediaItem(item.id, item.title, item.uri, false))
            }
        }
        BigButton("Escuchar en YouTube (desde 15:15)", soft = item != null) { ctx.openUrl(BIBLE_URL) }
        SoftText("YouTube necesita Internet. Para escuchar sin Internet, añade tu audio en «Biblia hablada».")
    }
}
