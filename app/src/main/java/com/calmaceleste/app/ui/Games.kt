package com.calmaceleste.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

// ---------------------------------------------------------------------------------------------
// Menú y contenedor de los minijuegos
// ---------------------------------------------------------------------------------------------

@Composable
fun GamesScreen(go: (String) -> Unit, onBack: () -> Unit) {
    ScreenScaffold("Minijuegos relajantes", onBack) {
        SoftText("Sin puntos, sin tiempo, sin perder. Solo para soltar la mente un rato.")
        GameCard("🫧", "Burbujas", "Toca las burbujas y suéltalas.") { go(Routes.BUBBLES) }
        GameCard("🪨", "Jardín zen", "Dibuja con el dedo en la arena.") { go(Routes.ZEN) }
        GameCard("✨", "Luciérnagas", "Toca la pantalla y mira cómo se acercan.") { go(Routes.FIREFLIES) }
    }
}

@Composable
private fun GameCard(emoji: String, title: String, desc: String, onClick: () -> Unit) {
    SoftCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 34.sp)
            Column(Modifier.padding(start = 14.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                SoftText(desc)
            }
        }
    }
}

@Composable
fun GameScreen(title: String, onBack: () -> Unit, game: @Composable (Modifier) -> Unit) {
    ScreenScaffold(title, onBack) {
        game(Modifier.fillMaxWidth().height(520.dp))
    }
}

// ---------------------------------------------------------------------------------------------
// 1) Burbujas
// ---------------------------------------------------------------------------------------------

private class Bubble(
    var x: Float,
    var y: Float,
    val r: Float,
    val speed: Float,
    val sway: Float,
    val phase: Float
) {
    var popping = false
    var pop = 0f
    fun drawX(): Float = x + sin(phase + y / 60f) * sway
}

@Composable
fun BubblesGame(modifier: Modifier = Modifier) {
    val bubbles = remember { ArrayList<Bubble>() }
    var tick by remember { mutableLongStateOf(0L) }
    var count by remember { mutableIntStateOf(0) }
    var size by remember { mutableStateOf(IntSize.Zero) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                val dt = if (last == 0L) 0.016f else ((now - last) / 1_000_000_000f).coerceAtMost(0.05f)
                last = now
                val w = size.width.toFloat()
                val h = size.height.toFloat()
                if (w > 0f) {
                    if (bubbles.size < 12 && Random.nextFloat() < 0.04f) {
                        val r = Random.nextFloat() * 28f + 26f
                        bubbles.add(
                            Bubble(
                                x = Random.nextFloat() * (w - 2 * r) + r,
                                y = h + r,
                                r = r,
                                speed = Random.nextFloat() * 40f + 35f,
                                sway = Random.nextFloat() * 20f + 8f,
                                phase = Random.nextFloat() * 6.28f
                            )
                        )
                    }
                    val iter = bubbles.iterator()
                    while (iter.hasNext()) {
                        val b = iter.next()
                        if (b.popping) {
                            b.pop += dt * 4f
                            if (b.pop >= 1f) iter.remove()
                        } else {
                            b.y -= b.speed * dt
                            if (b.y + b.r < 0f) iter.remove()
                        }
                    }
                }
                tick = now
            }
        }
    }

    val dark = isSystemInDarkTheme()
    val bgTop = if (dark) Color(0xFF133A56) else Color(0xFFDDF1FC)
    val bgBottom = if (dark) Color(0xFF0B2336) else Color(0xFFB9E0F5)
    val line = Color(0xFF5BB3E3)

    Box(
        modifier
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.verticalGradient(listOf(bgTop, bgBottom)))
            .onSizeChanged { size = it }
            .pointerInput(Unit) {
                detectTapGestures { p ->
                    val hit = bubbles.lastOrNull { b ->
                        !b.popping && hypot(b.drawX() - p.x, b.y - p.y) <= b.r * 1.15f
                    }
                    if (hit != null) {
                        hit.popping = true
                        count++
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                }
            }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            @Suppress("UNUSED_VARIABLE") val redraw = tick // fuerza a redibujar en cada cuadro
            for (b in bubbles) {
                val a = if (b.popping) 1f - b.pop else 1f
                val rr = if (b.popping) b.r * (1f + 0.5f * b.pop) else b.r
                val c = Offset(b.drawX(), b.y)
                drawCircle(Color.White.copy(alpha = 0.25f * a), rr, c)
                drawCircle(line.copy(alpha = 0.9f * a), rr, c, style = Stroke(width = 3f))
                drawCircle(Color.White.copy(alpha = 0.75f * a), rr * 0.2f, Offset(c.x - rr * 0.35f, c.y - rr * 0.35f))
            }
        }
        Text(
            if (count == 0) "Toca las burbujas" else "Burbujas soltadas: $count",
            modifier = Modifier.align(Alignment.BottomCenter).padding(14.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (dark) Color(0xFFD7EEFB) else Color(0xFF12384F)
        )
    }
}

// ---------------------------------------------------------------------------------------------
// 2) Jardín zen
// ---------------------------------------------------------------------------------------------

@Composable
fun ZenGarden(modifier: Modifier = Modifier) {
    val strokes = remember { mutableStateListOf<List<Offset>>() }
    val current = remember { mutableStateListOf<Offset>() }
    val sand = Color(0xFFF1E9D8)
    val groove = Color(0xFFD9CDB4)
    val ridge = Color(0xFFFFFBF1)

    fun toPath(points: List<Offset>): Path {
        val p = Path()
        if (points.isNotEmpty()) {
            p.moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) p.lineTo(points[i].x, points[i].y)
        }
        return p
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(sand)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { o ->
                            current.clear()
                            current.add(o)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            current.add(change.position)
                        },
                        onDragEnd = {
                            if (current.size > 1) strokes.add(current.toList())
                            current.clear()
                        },
                        onDragCancel = { current.clear() }
                    )
                }
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                // Piedras decorativas
                drawOval(Color(0xFF9BA7AD), Offset(w * 0.12f, h * 0.14f), Size(w * 0.20f, h * 0.09f))
                drawOval(Color(0xFFB5C0C5), Offset(w * 0.62f, h * 0.70f), Size(w * 0.26f, h * 0.11f))
                drawOval(Color(0xFF8E9BA2), Offset(w * 0.30f, h * 0.80f), Size(w * 0.14f, h * 0.06f))

                val all = strokes.toList() + listOf(current.toList())
                for (pts in all) {
                    if (pts.size < 2) continue
                    val path = toPath(pts)
                    drawPath(path, groove, style = Stroke(width = 22f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    drawPath(path, ridge, style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }
            Text(
                if (strokes.isEmpty() && current.isEmpty()) "Desliza el dedo para dibujar en la arena" else "",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF7A6F58)
            )
        }
        BigButton("Alisar la arena", soft = true) {
            strokes.clear()
            current.clear()
        }
    }
}

// ---------------------------------------------------------------------------------------------
// 3) Luciérnagas
// ---------------------------------------------------------------------------------------------

private class Firefly(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val phase: Float,
    val size: Float
)

@Composable
fun FirefliesGame(modifier: Modifier = Modifier) {
    val flies = remember { ArrayList<Firefly>() }
    var tick by remember { mutableLongStateOf(0L) }
    var size by remember { mutableStateOf(IntSize.Zero) }
    var touch by remember { mutableStateOf<Offset?>(null) }

    LaunchedEffect(Unit) {
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                val dt = if (last == 0L) 0.016f else ((now - last) / 1_000_000_000f).coerceAtMost(0.05f)
                last = now
                val w = size.width.toFloat()
                val h = size.height.toFloat()
                if (w > 0f && flies.isEmpty()) {
                    repeat(26) {
                        flies.add(
                            Firefly(
                                Random.nextFloat() * w, Random.nextFloat() * h,
                                0f, 0f, Random.nextFloat() * 6.28f, Random.nextFloat() * 4f + 4f
                            )
                        )
                    }
                }
                val t = touch
                for (f in flies) {
                    // Deriva suave y aleatoria
                    f.vx += (Random.nextFloat() - 0.5f) * 90f * dt
                    f.vy += (Random.nextFloat() - 0.5f) * 90f * dt
                    // Si hay un dedo, se acercan con cariño
                    if (t != null) {
                        val dx = t.x - f.x
                        val dy = t.y - f.y
                        val d = hypot(dx, dy).coerceAtLeast(30f)
                        f.vx += dx / d * 140f * dt
                        f.vy += dy / d * 140f * dt
                    }
                    f.vx *= 0.985f
                    f.vy *= 0.985f
                    f.x += f.vx * dt
                    f.y += f.vy * dt
                    if (f.x < 0f) { f.x = 0f; f.vx = -f.vx }
                    if (f.x > w) { f.x = w; f.vx = -f.vx }
                    if (f.y < 0f) { f.y = 0f; f.vy = -f.vy }
                    if (f.y > h) { f.y = h; f.vy = -f.vy }
                }
                tick = now
            }
        }
    }

    Box(
        modifier
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF0B2A44), Color(0xFF123B5C))))
            .onSizeChanged { size = it }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val p = event.changes.firstOrNull()
                        touch = if (p != null && p.pressed) p.position else null
                        event.changes.forEach { it.consume() }
                    }
                }
            }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            @Suppress("UNUSED_VARIABLE") val redraw = tick
            val time = tick / 1_000_000_000f
            for (f in flies) {
                val glow = 0.55f + 0.45f * sin(time * 1.6f + f.phase)
                val r = f.size * (3.2f + 1.6f * glow)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFF3A8).copy(alpha = 0.85f * glow), Color.Transparent),
                        center = Offset(f.x, f.y),
                        radius = r * 3f
                    ),
                    radius = r * 3f,
                    center = Offset(f.x, f.y)
                )
                drawCircle(Color(0xFFFFFBDD).copy(alpha = glow), f.size * 0.6f, Offset(f.x, f.y))
            }
        }
        Text(
            "Toca y mantén: se acercan",
            modifier = Modifier.align(Alignment.BottomCenter).padding(14.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFBFE3F7)
        )
    }
}
