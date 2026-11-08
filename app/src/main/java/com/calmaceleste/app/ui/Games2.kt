package com.calmaceleste.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.atan2
import kotlin.math.hypot

// ---------------------------------------------------------------------------------------------
// 4) Ondas en el agua
// ---------------------------------------------------------------------------------------------

private class Ripple(val x: Float, val y: Float) {
    var age = 0f
}

@Composable
fun RipplesGame(modifier: Modifier = Modifier) {
    val ripples = remember { ArrayList<Ripple>() }
    var tick by remember { mutableLongStateOf(0L) }
    var touched by remember { mutableIntStateOf(0) }
    val dark = isSystemInDarkTheme()

    LaunchedEffect(Unit) {
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                val dt = if (last == 0L) 0.016f else ((now - last) / 1_000_000_000f).coerceAtMost(0.05f)
                last = now
                val iter = ripples.iterator()
                while (iter.hasNext()) {
                    val r = iter.next()
                    r.age += dt
                    if (r.age > 4.5f) iter.remove()
                }
                tick = now
            }
        }
    }

    val top = if (dark) Color(0xFF14465F) else Color(0xFF9AD6F2)
    val bottom = if (dark) Color(0xFF0A2638) else Color(0xFF4A9BD0)
    Box(
        modifier
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.verticalGradient(listOf(top, bottom)))
            .pointerInput(Unit) {
                detectTapGestures { p ->
                    ripples.add(Ripple(p.x, p.y))
                    touched++
                }
            }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            @Suppress("UNUSED_VARIABLE") val redraw = tick
            for (r in ripples) {
                for (k in 0..2) {
                    val a = r.age - k * 0.4f
                    if (a <= 0f) continue
                    val p = a / 3.6f
                    if (p >= 1f) continue
                    drawCircle(
                        Color.White.copy(alpha = (1f - p) * 0.7f),
                        radius = 14f + p * 300f,
                        center = Offset(r.x, r.y),
                        style = Stroke(width = 4f)
                    )
                }
            }
        }
        Text(
            if (touched == 0) "Toca el agua" else "Respira mientras las ondas se expanden",
            modifier = Modifier.align(Alignment.BottomCenter).padding(14.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFEAF7FF)
        )
    }
}

// ---------------------------------------------------------------------------------------------
// 5) Memoria de parejas (sin tiempo, sin puntos)
// ---------------------------------------------------------------------------------------------

private val memorySymbols = listOf("🌸", "🐚", "⭐", "🦋", "🌙", "🍃")

@Composable
fun MemoryGame(modifier: Modifier = Modifier) {
    var deck by remember { mutableStateOf((memorySymbols + memorySymbols).shuffled()) }
    var open by remember { mutableStateOf(listOf<Int>()) }
    var matched by remember { mutableStateOf(setOf<Int>()) }
    val done = matched.size == deck.size

    LaunchedEffect(open) {
        if (open.size == 2) {
            delay(800)
            if (deck[open[0]] == deck[open[1]]) matched = matched + open
            open = emptyList()
        }
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in 0 until 3) {
            Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (col in 0 until 4) {
                    val i = row * 4 + col
                    val faceUp = i in open || i in matched
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                when {
                                    i in matched -> Color(0xFFD5F0E3)
                                    faceUp -> Color.White
                                    else -> Color(0xFF8FD0F0)
                                }
                            )
                            .clickable(enabled = !faceUp && open.size < 2) { open = open + i },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (faceUp) deck[i] else "💙", fontSize = 28.sp)
                    }
                }
            }
        }
        if (done) {
            Text(
                "¡Muy bien! Encontraste todas las parejas 💙",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )
            BigButton("Jugar otra vez", soft = true) {
                deck = (memorySymbols + memorySymbols).shuffled()
                open = emptyList()
                matched = emptySet()
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// 6) Puzzle deslizante
// ---------------------------------------------------------------------------------------------

private val solvedBoard = listOf(1, 2, 3, 4, 5, 6, 7, 8, 0)

private fun neighborsOf(i: Int): List<Int> {
    val r = i / 3
    val c = i % 3
    return buildList {
        if (r > 0) add(i - 3)
        if (r < 2) add(i + 3)
        if (c > 0) add(i - 1)
        if (c < 2) add(i + 1)
    }
}

/** Mezcla con movimientos válidos desde el tablero resuelto: siempre tiene solución. */
private fun shuffledBoard(): List<Int> {
    val b = solvedBoard.toMutableList()
    var blank = 8
    var prev = -1
    repeat(100) {
        val options = neighborsOf(blank).filter { it != prev }
        val n = options.random()
        b[blank] = b[n]
        b[n] = 0
        prev = blank
        blank = n
    }
    return if (b == solvedBoard) shuffledBoard() else b
}

@Composable
fun SlidePuzzle(modifier: Modifier = Modifier) {
    var board by remember { mutableStateOf(shuffledBoard()) }
    val done = board == solvedBoard

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in 0 until 3) {
            Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (col in 0 until 3) {
                    val i = row * 3 + col
                    val n = board[i]
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (n == 0) Color.Transparent else Color(0xFF8FD0F0))
                            .clickable(enabled = n != 0 && !done) {
                                val blank = board.indexOf(0)
                                if (blank in neighborsOf(i)) {
                                    board = board.toMutableList().also { it[blank] = n; it[i] = 0 }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (n != 0) Text("$n", fontSize = 30.sp, color = Color(0xFF0B3954))
                    }
                }
            }
        }
        if (done) {
            Text(
                "¡Lo lograste! 💙",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )
        }
        BigButton(if (done) "Jugar otra vez" else "Mezclar de nuevo", soft = true) { board = shuffledBoard() }
    }
}

// ---------------------------------------------------------------------------------------------
// 7) Colorear mandalas
// ---------------------------------------------------------------------------------------------

private val paintColors = listOf(
    Color(0xFF8FD0F0), Color(0xFF3E8FC4), Color(0xFFB8E6D5), Color(0xFFF9D5E5),
    Color(0xFFFFE9A8), Color(0xFFFFB997), Color(0xFFC9B6F0), Color(0xFF4A5E6D)
)

private val canvasPaper = Color(0xFFF6FBFF)
private val outlineColor = Color(0xFF3E6F8F)

@Composable
private fun PaletteRow(selected: Int, onSelect: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        paintColors.forEachIndexed { i, c ->
            Box(
                Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(c)
                    .border(
                        if (i == selected) 3.dp else 1.dp,
                        if (i == selected) Color(0xFF0B3954) else Color(0x33000000),
                        CircleShape
                    )
                    .clickable { onSelect(i) }
            )
        }
    }
}

/** Cantidad de pétalos de cada anillo, de adentro hacia afuera. El centro es un círculo. */
private val mandalaPatterns = listOf(listOf(8, 16, 16), listOf(6, 12, 18), listOf(12, 12, 24))

private const val CENTER_FRACTION = 0.16f

@Composable
fun MandalaColoring(modifier: Modifier = Modifier) {
    var pattern by remember { mutableIntStateOf(0) }
    var selected by remember { mutableIntStateOf(0) }
    var box by remember { mutableStateOf(IntSize.Zero) }
    val fills = remember { mutableStateMapOf<Int, Color>() }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ChipRow(listOf("Mandala 1", "Mandala 2", "Mandala 3"), pattern) {
            pattern = it
            fills.clear()
        }
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(canvasPaper)
                .onSizeChanged { box = it }
                .pointerInput(pattern) {
                    detectTapGestures { p ->
                        val rings = mandalaPatterns[pattern]
                        val cx = box.width / 2f
                        val cy = box.height / 2f
                        val radius = minOf(box.width, box.height) / 2f * 0.96f
                        val dx = p.x - cx
                        val dy = p.y - cy
                        val d = hypot(dx, dy)
                        if (d <= radius * CENTER_FRACTION) {
                            fills[-1] = paintColors[selected]
                        } else if (d <= radius) {
                            val band = radius * (1f - CENTER_FRACTION) / rings.size
                            val ring = ((d - radius * CENTER_FRACTION) / band).toInt().coerceIn(0, rings.lastIndex)
                            var deg = Math.toDegrees(atan2(dy, dx).toDouble()).toFloat()
                            if (deg < 0f) deg += 360f
                            val sector = (deg / (360f / rings[ring])).toInt().coerceIn(0, rings[ring] - 1)
                            fills[ring * 100 + sector] = paintColors[selected]
                        }
                    }
                }
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val rings = mandalaPatterns[pattern]
                val cx = size.width / 2f
                val cy = size.height / 2f
                val radius = minOf(size.width, size.height) / 2f * 0.96f
                val band = radius * (1f - CENTER_FRACTION) / rings.size
                val center = Offset(cx, cy)

                drawCircle(fills[-1] ?: Color.White, radius * CENTER_FRACTION, center)
                drawCircle(outlineColor, radius * CENTER_FRACTION, center, style = Stroke(width = 3f))

                rings.forEachIndexed { ring, count ->
                    val r0 = radius * CENTER_FRACTION + ring * band
                    val r1 = r0 + band
                    val sweep = 360f / count
                    for (s in 0 until count) {
                        val start = s * sweep
                        val path = Path().apply {
                            arcTo(Rect(cx - r1, cy - r1, cx + r1, cy + r1), start, sweep, true)
                            arcTo(Rect(cx - r0, cy - r0, cx + r0, cy + r0), start + sweep, -sweep, false)
                            close()
                        }
                        drawPath(path, fills[ring * 100 + s] ?: Color.White)
                        drawPath(path, outlineColor, style = Stroke(width = 3f, join = StrokeJoin.Round))
                    }
                }
            }
        }
        PaletteRow(selected) { selected = it }
        TextButton(onClick = { fills.clear() }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Borrar colores")
        }
    }
}

// ---------------------------------------------------------------------------------------------
// 8) Dibujo libre con colores
// ---------------------------------------------------------------------------------------------

private class DrawStroke(val color: Color) {
    val points = mutableStateListOf<Offset>()
}

@Composable
fun FreeDrawing(modifier: Modifier = Modifier) {
    val strokes = remember { mutableStateListOf<DrawStroke>() }
    var selected by remember { mutableIntStateOf(1) }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(canvasPaper)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { o ->
                            val s = DrawStroke(paintColors[selected])
                            s.points.add(o)
                            strokes.add(s)
                        },
                        onDrag = { change, _ -> strokes.lastOrNull()?.points?.add(change.position) }
                    )
                }
        ) {
            Canvas(Modifier.fillMaxSize()) {
                for (s in strokes) {
                    val pts = s.points
                    if (pts.size < 2) {
                        if (pts.isNotEmpty()) drawCircle(s.color, 7f, pts[0])
                    } else {
                        val path = Path()
                        path.moveTo(pts[0].x, pts[0].y)
                        for (k in 1 until pts.size) path.lineTo(pts[k].x, pts[k].y)
                        drawPath(path, s.color, style = Stroke(width = 14f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                }
            }
            if (strokes.isEmpty()) {
                Text(
                    "Dibuja con el dedo",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge,
                    color = outlineColor
                )
            }
        }
        PaletteRow(selected) { selected = it }
        Row(Modifier.align(Alignment.CenterHorizontally), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { if (strokes.isNotEmpty()) strokes.removeAt(strokes.lastIndex) }) { Text("Deshacer") }
            TextButton(onClick = { strokes.clear() }) { Text("Borrar todo") }
        }
    }
}
