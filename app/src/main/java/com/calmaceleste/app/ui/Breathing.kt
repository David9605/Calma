package com.calmaceleste.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calmaceleste.app.data.BREATH_PATTERNS
import com.calmaceleste.app.data.BreathPattern
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Círculo que crece al inhalar y se encoge al exhalar, con cuenta en segundos y vibración suave. */
@Composable
fun BreathingCircle(
    pattern: BreathPattern,
    running: Boolean,
    modifier: Modifier = Modifier,
    onCycle: () -> Unit = {}
) {
    val scale = remember { Animatable(0.55f) }
    var label by remember { mutableStateOf("Prepárate") }
    var left by remember { mutableIntStateOf(0) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(pattern, running) {
        if (!running) {
            label = "En pausa"
            return@LaunchedEffect
        }
        delay(300)
        suspend fun phase(text: String, secs: Int, target: Float?) {
            label = text
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            coroutineScope {
                if (target != null) {
                    launch { scale.animateTo(target, tween(secs * 1000, easing = FastOutSlowInEasing)) }
                }
                for (s in secs downTo 1) {
                    left = s
                    delay(1000)
                }
            }
        }
        while (true) {
            phase("Inhala", pattern.inhale, 1f)
            if (pattern.hold1 > 0) phase("Sostén", pattern.hold1, null)
            phase("Exhala", pattern.exhale, 0.55f)
            if (pattern.hold2 > 0) phase("Sostén", pattern.hold2, null)
            onCycle()
        }
    }

    val primary = MaterialTheme.colorScheme.primary
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val base = size.minDimension / 2f
            val r = base * scale.value
            drawCircle(primary.copy(alpha = 0.15f), radius = (r * 1.18f).coerceAtMost(base))
            drawCircle(primary.copy(alpha = 0.35f), radius = r)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.9f), primary),
                    center = center,
                    radius = r * 0.75f
                ),
                radius = r * 0.7f
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0B3954))
            if (running && left > 0) {
                Text("$left", fontSize = 30.sp, fontWeight = FontWeight.Light, color = Color(0xFF0B3954))
            }
        }
    }
}

@Composable
fun BreathingScreen(onBack: () -> Unit) {
    var selected by remember { mutableIntStateOf(0) }
    var running by remember { mutableStateOf(false) }
    var cycles by remember { mutableIntStateOf(0) }
    val pattern = BREATH_PATTERNS[selected]

    ScreenScaffold("Respiración guiada", onBack) {
        ChipRow(BREATH_PATTERNS.map { it.name }, selected) {
            selected = it
            cycles = 0
        }
        SoftCard {
            Text(pattern.desc, style = MaterialTheme.typography.bodyLarge)
        }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            BreathingCircle(
                pattern = pattern,
                running = running,
                modifier = Modifier.size(290.dp),
                onCycle = { cycles++ }
            )
        }
        Text(
            if (cycles == 0) "Ciclos completados: 0" else "Ciclos completados: $cycles 💙",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        BigButton(if (running) "Pausar" else "Empezar") { running = !running }
    }
}

private data class GroundStage(val count: Int, val text: String, val emoji: String)

private val groundStages = listOf(
    GroundStage(5, "cosas que puedes VER", "👀"),
    GroundStage(4, "cosas que puedes TOCAR", "🖐️"),
    GroundStage(3, "sonidos que puedes ESCUCHAR", "👂"),
    GroundStage(2, "olores que puedes SENTIR", "👃"),
    GroundStage(1, "sabor que puedes NOTAR", "👅")
)

/** Técnica 5-4-3-2-1: se toca el círculo cada vez que la persona encuentra una cosa. */
@Composable
fun GroundingContent(onDone: () -> Unit) {
    var stage by remember { mutableIntStateOf(0) }
    var count by remember { mutableIntStateOf(0) }

    AnimatedContent(
        targetState = stage,
        transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(250)) },
        label = "ground"
    ) { st ->
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (st >= groundStages.size) {
                Text("🌿", fontSize = 44.sp)
                Text(
                    "Muy bien. Estás aquí, en el presente. Respira hondo una vez más.",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            } else {
                val g = groundStages[st]
                Text(g.emoji, fontSize = 44.sp)
                Text(
                    "Encuentra ${g.count} ${g.text}",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                SoftText("Toca el círculo cada vez que encuentres una.")
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable {
                            count++
                            if (count >= g.count) {
                                count = 0
                                stage++
                                if (stage >= groundStages.size) onDone()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "$count / ${g.count}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun GroundingScreen(onBack: () -> Unit) {
    ScreenScaffold("Anclaje 5-4-3-2-1", onBack) {
        SoftCard {
            Text(
                "Cuando la mente va muy rápido, tus sentidos te devuelven al presente. Ve despacio, sin prisa.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        SoftCard { GroundingContent(onDone = {}) }
    }
}
