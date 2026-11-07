package com.calmaceleste.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/** Fondo degradado celeste con "manchas de luz" que flotan muy despacio. */
@Composable
fun CalmBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val dark = isSystemInDarkTheme()
    val top = if (dark) Color(0xFF0B2336) else Color(0xFFEAF7FF)
    val bottom = if (dark) Color(0xFF133A56) else Color(0xFFCDE9FA)
    val glow = if (dark) Color(0xFF4FA8D8).copy(alpha = 0.18f) else Color.White.copy(alpha = 0.65f)
    val transition = rememberInfiniteTransition(label = "bg")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Reverse),
        label = "drift"
    )
    Box(modifier.fillMaxSize().background(Brush.verticalGradient(listOf(top, bottom)))) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            fun blob(cx: Float, cy: Float, r: Float) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glow, Color.Transparent),
                        center = Offset(cx, cy),
                        radius = r
                    ),
                    radius = r,
                    center = Offset(cx, cy)
                )
            }
            blob(w * (0.15f + 0.25f * drift), h * 0.15f, w * 0.7f)
            blob(w * (0.9f - 0.3f * drift), h * 0.5f, w * 0.6f)
            blob(w * (0.3f + 0.2f * drift), h * 0.9f, w * 0.8f)
        }
        content()
    }
}

/** Pantalla estándar: fondo, cabecera con botón de volver y contenido con scroll. */
@Composable
fun ScreenScaffold(
    title: String,
    onBack: (() -> Unit)?,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    CalmBackground {
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                } else {
                    Spacer(Modifier.width(12.dp))
                }
                Text(
                    title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                actions()
            }
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                content()
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

/** Aparece suavemente (fundido + pequeño deslizamiento). Úsalo con un retraso para escalonar. */
@Composable
fun Appear(delayMs: Int = 0, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        shown = true
    }
    val a by animateFloatAsState(if (shown) 1f else 0f, tween(550), label = "appear")
    Box(modifier.graphicsLayer { alpha = a; translationY = (1f - a) * 40f }) { content() }
}

@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    tint: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val dark = isSystemInDarkTheme()
    val container = tint ?: if (dark) Color(0xFF173F5C).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.80f)
    val colors = CardDefaults.cardColors(containerColor = container)
    val shape = RoundedCornerShape(26.dp)
    val elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = shape, colors = colors, elevation = elevation) {
            Column(Modifier.padding(18.dp), content = content)
        }
    } else {
        Card(modifier = modifier.fillMaxWidth(), shape = shape, colors = colors, elevation = elevation) {
            Column(Modifier.padding(18.dp), content = content)
        }
    }
}

/** Botón grande con efecto de "rebote" suave al tocarlo. */
@Composable
fun BigButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    soft: Boolean = false,
    onClick: () -> Unit
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "press"
    )
    val colors = if (soft) ButtonDefaults.filledTonalButtonColors() else ButtonDefaults.buttonColors()
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale },
        enabled = enabled,
        shape = RoundedCornerShape(22.dp),
        colors = colors,
        interactionSource = source
    ) {
        Text(text, fontSize = 17.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
fun SoftText(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun ChipRow(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEachIndexed { i, label ->
            FilterChip(selected = i == selected, onClick = { onSelect(i) }, label = { Text(label) })
        }
    }
}

/** Etiquetas seleccionables en filas de 3. */
@Composable
fun TagGrid(options: List<String>, selected: List<String>, onToggle: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        options.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { tag ->
                    FilterChip(
                        selected = tag in selected,
                        onClick = { onToggle(tag) },
                        label = { Text(tag, maxLines = 1, fontSize = 13.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/** Deslizador de 0 a 10. */
@Composable
fun ScoreSlider(value: Int, onChange: (Int) -> Unit) {
    Slider(
        value = value.toFloat(),
        onValueChange = { onChange(it.roundToInt()) },
        valueRange = 0f..10f,
        steps = 9
    )
}

fun anxietyWord(v: Int): String = when {
    v <= 1 -> "Muy tranquilo"
    v <= 3 -> "Leve"
    v <= 5 -> "Moderada"
    v <= 7 -> "Fuerte"
    else -> "Muy fuerte"
}

/** Anillo de cuenta regresiva. Al llegar a cero muestra una marca de verificación. */
@Composable
fun CountdownRing(seconds: Int, modifier: Modifier = Modifier) {
    var left by remember(seconds) { mutableIntStateOf(seconds) }
    LaunchedEffect(seconds) {
        while (left > 0) {
            delay(1000)
            left--
        }
    }
    val progress by animateFloatAsState(
        targetValue = if (seconds > 0) left.toFloat() / seconds else 0f,
        animationSpec = tween(900, easing = LinearEasing),
        label = "ring"
    )
    val ring = MaterialTheme.colorScheme.primary
    val track = ring.copy(alpha = 0.25f)
    Box(modifier.size(120.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val sw = 12.dp.toPx()
            val inset = sw / 2f
            val arcSize = Size(size.width - sw, size.height - sw)
            drawArc(track, 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(sw))
            drawArc(ring, -90f, 360f * progress, false, Offset(inset, inset), arcSize, style = Stroke(sw, cap = StrokeCap.Round))
        }
        Text(
            if (left > 0) "%d:%02d".format(left / 60, left % 60) else "✓",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirmLabel: String = "Sí, borrar",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
